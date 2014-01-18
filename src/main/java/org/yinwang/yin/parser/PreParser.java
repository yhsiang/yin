package org.yinwang.yin.parser;


import org.jetbrains.annotations.Nullable;
import org.yinwang.yin._;

import java.util.*;


public class PreParser {

    public static final char RADIX_PREFIX = '#';

    public String file;
    public String text;

    // current offset indicators
    public int offset;
    public int line;
    public int col;

    // all delimeters
    public final Set<String> delims = new HashSet<>();
    // map open delimeters to their matched closing ones
    public final Map<String, String> delimMap = new HashMap<>();


    public PreParser(String file) {
        this.file = _.unifyPath(file);
        this.text = _.readFile(file);
        this.offset = 0;
        this.line = 0;
        this.col = 0;

        if (text == null) {
            _.abort("failed to read file: " + file);
        }

        addDelimiterPair("(", ")");
        addDelimiterPair("{", "}");
        addDelimiterPair("[", "]");
        addDelimiter(".");
    }


    public void forward() {
        if (text.charAt(offset) == '\n') {
            line++;
            col = 0;
            offset++;
        } else {
            col++;
            offset++;
        }
    }


    public void addDelimiterPair(String open, String close) {
        delims.add(open);
        delims.add(close);
        delimMap.put(open, close);
    }


    public void addDelimiter(String delim) {
        delims.add(delim);
    }


    public boolean isDelimiter(char c) {
        return delims.contains(Character.toString(c));
    }


    public boolean isOpen(Node c) {
        if (c instanceof Token) {
            return delimMap.keySet().contains(((Token) c).content);
        } else {
            return false;
        }
    }


    public boolean isClose(Node c) {
        if (c instanceof Token) {
            return delimMap.values().contains(((Token) c).content);
        } else {
            return false;
        }
    }


    public boolean matchString(String open, String close) {
        String matched = delimMap.get(open);
        if (matched != null && matched.equals(close)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean matchDelim(Node open, Node close) {
        return (open instanceof Token &&
                close instanceof Token &&
                matchString(((Token) open).content, ((Token) close).content));
    }


    public boolean isNumberPrefix(char c) {
        return Character.isDigit(c) || c == RADIX_PREFIX;
    }


    /**
     * lexer
     *
     * @return a token or null if file ends
     */
    @Nullable
    private Node nextToken() {
        // skip spaces
        while (offset < text.length() &&
                Character.isWhitespace(text.charAt(offset)))
        {
            forward();
        }

        // end of file
        if (offset >= text.length()) {
            return null;
        }

        char cur = text.charAt(offset);

        // delimiters
        if (isDelimiter(cur)) {
            Token ret = new Token(TokenType.DELIMITER, Character.toString(cur), file, offset, offset + 1,
                    line, col);
            forward();
            return ret;
        }

        // string
        if (text.charAt(offset) == '"' && (offset == 0 || text.charAt(offset - 1) != '\\')) {
            int start = offset;
            int startLine = line;
            int startCol = col;
            forward();   // skip "

            while (offset < text.length() &&
                    !(text.charAt(offset) == '"' && text.charAt(offset - 1) != '\\'))
            {
                if (text.charAt(offset) == '\n') {
                    _.abort(file + ":" + startLine + ":" + startCol + ": runaway string");
                }
                forward();
            }

            if (offset >= text.length()) {
                _.abort(file + ":" + startLine + ":" + startCol + ": runaway string");
            }

            forward(); // skip "
            int end = offset;

            String content = text.substring(start + 1, end - 1);
            return new Token(TokenType.STRING, content, file, start, end, startLine, startCol);
        }


        // find consequtive token
        int start = offset;
        int startLine = line;
        int startCol = col;

        if (isNumberPrefix(text.charAt(start)) ||
                ((text.charAt(start) == '+' || text.charAt(start) == '-')
                        && isNumberPrefix(text.charAt(start + 1))))
        {
            while (offset < text.length() &&
                    !Character.isWhitespace(cur) &&
                    !(isDelimiter(cur) && cur != '.'))
            {
                forward();
                if (offset < text.length()) {
                    cur = text.charAt(offset);
                }
            }

            String content = text.substring(start, offset);
            IntNum n = IntNum.parse(content, file, start, offset, startLine, startCol);

            if (n != null) {
                return n;
            } else {
                FloatNum n2 = FloatNum.parse(content, file, start, offset, startLine, startCol);
                if (n2 != null) {
                    return n2;
                } else {
                    _.abort("illegal number format: " + content);
                    return null;
                }
            }
        } else {
            while (offset < text.length() &&
                    !Character.isWhitespace(cur) &&
                    !isDelimiter(cur))
            {
                forward();
                if (offset < text.length()) {
                    cur = text.charAt(offset);
                }
            }

            String content = text.substring(start, offset);
            return new Token(TokenType.IDENT, content, file, start, offset, startLine, startCol);
        }
    }


    /**
     * parser
     *
     * @return a Node or null if file ends
     */
    public Node nextNode(int depth) {
        Node begin = nextToken();

        // end of file
        if (begin == null) {
            return null;
        }

        if (depth == 0 && isClose(begin)) {
            _.abort(begin.getFileLineCol() + " unmatched closing delimeter " + begin);
            return null;
        } else if (isOpen(begin)) {   // try to get matched (...)
            List<Node> tokens = new ArrayList<>();
            Node iter = nextNode(depth + 1);

            while (!matchDelim(begin, iter)) {
                if (iter == null) {
                    _.abort(begin.getFileLineCol() + ": unclosed delimeter " + begin);
                    return null;
                } else if (isClose(iter)) {
                    _.abort(iter.getFileLineCol() + " unmatched closing delimeter " + iter);
                    return null;
                } else {
                    tokens.add(iter);
                    iter = nextNode(depth + 1);
                }
            }
            return new Tuple(tokens, begin, iter, begin.file, begin.start, iter.end, begin.line, begin.col);
        } else {
            return begin;
        }
    }


    // wrapper for the actual parser
    public Node nextSexp() {
        return nextNode(0);
    }


    // parse file into a Node
    public Node parse() {
        List<Node> elements = new ArrayList<>();
        Node s = nextSexp();
        while (s != null) {
            elements.add(s);
            s = nextSexp();
        }
        return new Tuple(elements, null, null, file, 0, text.length(), 0, 0);
    }


    public static void main(String[] args) {
        PreParser p = new PreParser(args[0]);
        _.msg("tree: " + p.parse());
    }
}
