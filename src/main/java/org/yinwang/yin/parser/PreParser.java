package org.yinwang.yin.parser;


import org.jetbrains.annotations.Nullable;
import org.yinwang.yin.Constants;
import org.yinwang.yin.GeneralError;
import org.yinwang.yin._;
import org.yinwang.yin.ast.*;

import java.util.*;


/**
 * first phase parser
 * parse into S-expression like format
 */
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


    public PreParser(String file) throws GeneralError {
        this.file = _.unifyPath(file);
        this.text = _.readFile(file);
        this.offset = 0;
        this.line = 0;
        this.col = 0;

        if (text == null) {
            throw new GeneralError("failed to read file: " + file);
        }

        addDelimiterPair(Constants.TUPLE_BEGIN, Constants.TUPLE_END);
        addDelimiterPair(Constants.RECORD_BEGIN, Constants.RECORD_END);
        addDelimiterPair(Constants.ARRAY_BEGIN, Constants.ARRAY_END);

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
        if (c instanceof Delimeter) {
            return delimMap.keySet().contains(((Delimeter) c).shape);
        } else {
            return false;
        }
    }


    public boolean delimType(Node c, String d) {
        if (c instanceof Delimeter) {
            return ((Delimeter) c).shape.equals(d);
        } else {
            return false;
        }
    }


    public boolean isClose(Node c) {
        if (c instanceof Delimeter) {
            return delimMap.values().contains(((Delimeter) c).shape);
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
        return (open instanceof Delimeter &&
                close instanceof Delimeter &&
                matchString(((Delimeter) open).shape, ((Delimeter) close).shape));
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
    private Node nextToken() throws GeneralError {
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
            Node ret = new Delimeter(Character.toString(cur), file, offset, offset + 1, line, col);
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
                    throw new GeneralError(file + ":" + startLine + ":" + startCol + ": runaway string");
                }
                forward();
            }

            if (offset >= text.length()) {
                throw new GeneralError(file + ":" + startLine + ":" + startCol + ": runaway string");
            }

            forward(); // skip "
            int end = offset;

            String content = text.substring(start + 1, end - 1);
            return new Str(content, file, start, end, startLine, startCol);
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

            try {
                return new IntNum(content, file, start, offset, startLine, startCol);
            } catch (GeneralError e) {
                return new FloatNum(content, file, start, offset, startLine, startCol);
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
            if (content.matches(":\\w.*")) {
                return new Keyword(content.substring(1), file, start, offset, startLine, startCol);
            } else {
                return new Name(content, file, start, offset, startLine, startCol);
            }
        }
    }


    /**
     * parser
     *
     * @return a Node or null if file ends
     */
    public Node nextNode(int depth) throws GeneralError {
        Node begin = nextToken();

        // end of file
        if (begin == null) {
            return null;
        }

        if (depth == 0 && isClose(begin)) {
            throw new GeneralError(begin, "unmatched closing delimeter: " + begin);
        } else if (isOpen(begin)) {   // try to get matched (...)
            List<Node> elements = new ArrayList<>();
            Node iter = nextNode(depth + 1);

            while (!matchDelim(begin, iter)) {
                if (iter == null) {
                    throw new GeneralError(begin, "unclosed delimeter: " + begin);
                } else if (isClose(iter)) {
                    throw new GeneralError(iter, "unmatched closing delimeter: " + iter);
                } else {
                    elements.add(iter);
                    iter = nextNode(depth + 1);
                }
            }
            if (delimType(begin, Constants.TUPLE_BEGIN)) {
                return new Tuple(elements, begin, iter, begin.file, begin.start, iter.end, begin.line, begin.col);
            }
            if (delimType(begin, Constants.RECORD_BEGIN)) {
                return new RecordLiteral(elements, begin.file, begin.start, iter.end, begin.line, begin.col);
            }
            return null;
        } else {
            return begin;
        }
    }


    // wrapper for the actual parser
    public Node nextSexp() throws GeneralError {
        return nextNode(0);
    }


    // parse file into a Node
    public Node parse() throws GeneralError {
        List<Node> elements = new ArrayList<>();
        Node s = nextSexp();
        while (s != null) {
            elements.add(s);
            s = nextSexp();
        }
        return new Block(elements, file, 0, text.length(), 0, 0);
    }


    public static void main(String[] args) throws GeneralError {
        PreParser p = new PreParser(args[0]);
        _.msg("tree: " + p.parse());
    }
}
