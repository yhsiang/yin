package org.yinwang.yin.parser;


import org.jetbrains.annotations.Nullable;
import org.yinwang.yin._;

import java.util.*;


public class SexpParser {

    public static final char RADIX_PREFIX = '#';

    public String file;
    public String text;
    public int position;
    public int line;
    public int col;
    public Token.TokenType context;
    public final Set<String> allDelims = new HashSet<>();
    public final Map<String, String> match = new HashMap<>();
    public final Set<String> specialChar = new HashSet<>();


    public SexpParser(String file) {
        this.file = _.unifyPath(file);
        this.text = _.readFile(file);
        this.position = 0;
        this.line = 0;
        this.col = 0;

        if (text == null) {
            _.abort("failed to read file: " + file);
        }

        addDelimiterPair("(", ")");
        addDelimiterPair("{", "}");
        addDelimiterPair("[", "]");
        addDelimiter(".");
        addSpecialChar("\\");
        addSpecialChar("\"");
    }


    public void forward() {
        if (text.charAt(position) == '\n') {
            line++;
            col = 0;
            position++;
        } else {
            col++;
            position++;
        }
    }


    public void addDelimiterPair(String open, String close) {
        allDelims.add(open);
        allDelims.add(close);
        match.put(open, close);
    }


    public void addDelimiter(String delim) {
        allDelims.add(delim);
    }


    public boolean isDelimiter(char c) {
        if (c == '.' && context == Token.TokenType.NUMBER) {
            return false;
        } else {
            return allDelims.contains(Character.toString(c));
        }
    }


    public void addSpecialChar(String c) {
        specialChar.add(c);
    }


    public boolean isLegalChar(char c, Token.TokenType context) {
        if (context == Token.TokenType.NUMBER) {
            return Character.toString(c).matches("[0-9e\\+\\-]");
        } else if (context == Token.TokenType.IDENT) {
            return !specialChar.contains(Character.toString(c));
        } else {
            _.abort("illegal context: " + context);
            return false;
        }
    }


    public boolean isOpen(String c) {
        return match.keySet().contains(c);
    }


    public boolean isClose(String c) {
        return match.values().contains(c);
    }


    public String matchDelim(String open) {
        return match.get(open);
    }


    public boolean matchString(String open, String close) {
        String matched = match.get(open);
        if (matched != null && matched.equals(close)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean matchDelim(Sexp open, Sexp close) {
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
    private Token nextToken() {
        // skip spaces
        while (position < text.length() &&
                Character.isWhitespace(text.charAt(position)))
        {
            forward();
        }

        // end of file
        if (position >= text.length()) {
            return null;
        }

        char cur = text.charAt(position);

        // delimiters
        if (isDelimiter(cur)) {
            Token ret = new Token(Token.TokenType.DELIMITER, Character.toString(cur), file, position, position + 1,
                    line, col);
            forward();
            return ret;
        }

        // string
        if (text.charAt(position) == '"' && (position == 0 || text.charAt(position - 1) != '\\')) {
            int start = position;
            int startLine = line;
            int startCol = col;
            forward();   // skip "

            while (position < text.length() &&
                    !(text.charAt(position) == '"' && text.charAt(position - 1) != '\\'))
            {
                if (text.charAt(position) == '\n') {
                    _.abort(file + ":" + startLine + ":" + startCol + ": runaway string");
                }
                forward();
            }

            if (position >= text.length()) {
                _.abort(file + ":" + startLine + ":" + startCol + ": runaway string");
            }

            forward(); // skip "
            int end = position;

            String content = text.substring(start + 1, end - 1);
            return new Token(Token.TokenType.STRING, content, file, start, end, startLine, startCol);
        }


        // find consequtive token
        int start = position;
        int startLine = line;
        int startCol = col;

        if (isNumberPrefix(text.charAt(start)) ||
                ((text.charAt(start) == '+' || text.charAt(start) == '-')
                        && isNumberPrefix(text.charAt(start + 1))))
        {
            while (position < text.length() &&
                    !Character.isWhitespace(cur) &&
                    !(isDelimiter(cur) && cur != '.'))
            {
                forward();
                if (position < text.length()) {
                    cur = text.charAt(position);
                }
            }

            String content = text.substring(start, position);
            IntNum n = IntNum.parse(content, file, start, position, startLine, startCol);

            if (n != null) {
                return n;
            } else {
                FloatNum n2 = FloatNum.parse(content, file, start, position, startLine, startCol);
                if (n2 != null) {
                    return n2;
                } else {
                    _.abort("illegal number format: " + content);
                    return null;
                }
            }
        } else {
            while (position < text.length() &&
                    !Character.isWhitespace(cur) &&
                    !isDelimiter(cur))
            {
                forward();
                if (position < text.length()) {
                    cur = text.charAt(position);
                }
            }

            String content = text.substring(start, position);
            return new Token(context, content, file, start, position, startLine, startCol);
        }
    }


    /**
     * parser
     *
     * @return a Sexp or null if file ends
     */
    public Sexp nextSexp(int depth) {
        Token begin = nextToken();

        // end of file
        if (begin == null) {
            return null;
        }

        if (depth == 0 && isClose(begin.content)) {
            _.abort(begin.getFileLineCol() + " unmatched closing delimeter " + begin.content);
            return null;
        } else if (isOpen(begin.content)) {   // try to get matched (...)
            List<Sexp> tokens = new ArrayList<>();
            Sexp iter = nextSexp(depth + 1);

            while (!matchDelim(begin, iter)) {
                if (iter == null) {
                    _.abort(begin.getFileLineCol() + ": unclosed delimeter " + begin.content);
                    return null;
                } else if (iter instanceof Token && isClose(((Token) iter).content)) {
                    _.abort(((Token) iter).getFileLineCol() + " unmatched closing delimeter " + ((Token) iter).content);
                    return null;
                } else {
                    tokens.add(iter);
                    iter = nextSexp(depth + 1);
                }
            }
            return new Tuple(tokens, begin, ((Token) iter), begin.file, begin.start, iter.end,
                    begin.line, begin.col);
        } else {
            return begin;
        }
    }


    // wrapper for the actual parser
    public Sexp nextSexp() {
        return nextSexp(0);
    }


    // parse file into a Sexp
    public Sexp parse() {
        List<Sexp> elements = new ArrayList<>();
        Sexp s = nextSexp();
        while (s != null) {
            elements.add(s);
            s = nextSexp();
        }
        return new Tuple(elements, null, null, file, 0, text.length(), 0, 0);
    }


    public static void main(String[] args) {
        SexpParser p = new SexpParser(args[0]);
        _.msg("tree: " + p.parse());
    }
}
