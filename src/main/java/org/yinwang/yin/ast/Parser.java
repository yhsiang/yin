package org.yinwang.yin.ast;


import org.jetbrains.annotations.Nullable;
import org.yinwang.yin._;

import java.util.*;


public class Parser {

    public String file;
    public String text;
    public int position;
    public final Set<String> allDelims = new HashSet<>();
    public final Map<String, String> match = new HashMap<>();


    public Parser(String file) {
        this.file = file;
        this.text = _.readFile(file);
        this.position = 0;

        addDelimiterPair("(", ")");
        addDelimiterPair("{", "}");
        addDelimiterPair("[", "]");
    }


    public void addDelimiterPair(String open, String close) {
        allDelims.add(open);
        allDelims.add(close);
        match.put(open, close);
    }


    public boolean isDelimiter(String c) {
        return allDelims.contains(c);
    }


    public boolean isDelimiter(char c) {
        return allDelims.contains(Character.toString(c));
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
            position++;
        }

        // end of file
        if (position >= text.length()) {
            return null;
        }

        char cur = text.charAt(position);

        // delimiters
        if (isDelimiter(cur)) {
            position++;
            return new Token(Token.TokenType.DELIMITER, Character.toString(cur), file, position - 1, position);
        }

        // string
        if (text.charAt(position) == '"') {
            position++; // skip "
            int start = position;

            while (position < text.length() &&
                    !(text.charAt(position) == '"' && text.charAt(position - 1) != '\\'))
            {
                position++;
            }

            if (position >= text.length()) {
                _.abort("runaway string from: " + start);
            }

            int end = position;
            position++; // skip "

            String content = text.substring(start, end);
            return new Token(Token.TokenType.STRING, content, file, start, end);
        }


        // find consequtive token
        int start = position;
        while (position < text.length() &&
                !Character.isWhitespace(cur) &&
                !isDelimiter(cur))
        {
            position++;
            if (position < text.length()) {
                cur = text.charAt(position);
            }
        }

        String content = text.substring(start, position);
        return new Token(Token.TokenType.IDENT, content, file, start, position);
    }


    /**
     * parser
     *
     * @return a Sexp or null if file ends
     */
    public Sexp nextSexp() {
        Token startToken = nextToken();

        // end of file
        if (startToken == null) {
            return null;
        }

        // try to get matched (...)
        if (isOpen(startToken.content)) {
            String file = startToken.file;
            List<Sexp> tokens = new ArrayList<>();
            Sexp next = nextSexp();
            while (!matchDelim(startToken, next)) {
                if (next == null) {
                    _.abort("unclosed paren at: " + startToken.start);
                } else {
                    tokens.add(next);
                    next = nextSexp();
                }
            }
            return new Tuple(tokens, startToken.content, ((Token) next).content, file, startToken.start, next.end);
        } else {
            return startToken;
        }
    }


    public static void main(String[] args) {
        Parser p = new Parser(args[0]);

        Sexp s = p.nextSexp();
        while (s != null) {
            _.msg("sexp: " + s);
            s = p.nextSexp();
        }
    }
}
