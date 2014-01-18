package org.yinwang.yin.parser;


import org.jetbrains.annotations.Nullable;
import org.yinwang.yin._;

import java.util.*;


public class Parser {

    public String file;
    public String text;
    public int position;
    public int line;
    public int col;
    public Token.TokenType context;
    public final Set<String> allDelims = new HashSet<>();
    public final Map<String, String> match = new HashMap<>();


    public Parser(String file) {
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


    public boolean isDelimiter(String c) {
        return allDelims.contains(c);
    }


    public boolean isDelimiter(char c) {
        if (c == '.' && context == Token.TokenType.NUMBER) {
            return false;
        } else {
            return allDelims.contains(Character.toString(c));
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
        if (text.charAt(position) == '"') {
            forward();   // skip "
            int start = position;
            int startLine = line;
            int startCol = col;

            while (position < text.length() &&
                    !(text.charAt(position) == '"' && text.charAt(position - 1) != '\\'))
            {
                forward();
            }

            if (position >= text.length()) {
                _.abort(file + ":" + startLine + ":" + startCol + ": runaway string");
            }

            int end = position;
            forward(); // skip "

            String content = text.substring(start, end);
            return new Token(Token.TokenType.STRING, content, file, start, end, startLine, startCol);
        }


        // find consequtive token
        int start = position;
        int startLine = line;
        int startCol = col;
        if (Character.isDigit(text.charAt(start))) {
            context = Token.TokenType.NUMBER;
        } else {
            context = Token.TokenType.IDENT;
        }

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
        Parser p = new Parser(args[0]);
        _.msg("tree: " + p.parse());
    }
}
