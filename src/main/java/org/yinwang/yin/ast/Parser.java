package org.yinwang.yin.ast;


import org.jetbrains.annotations.Nullable;
import org.yinwang.yin._;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Parser {

    public String file;
    public String text;
    public int position;
    public Set<Character> delims = new HashSet<>();


    public Parser(String file) {
        this.file = file;
        this.text = _.readFile(file);
        this.position = 0;

        delims.add('(');
        delims.add(')');
        delims.add('{');
        delims.add('}');
        delims.add('[');
        delims.add(']');
    }


    private boolean isDelimiter(char c) {
        return delims.contains(c);
    }


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
            return new Token(Token.TokenType.OPENPAREN, Character.toString(cur), file, position - 1, position);
        }

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
            cur = text.charAt(position);
        }

        String content = text.substring(start, position);
        return new Token(Token.TokenType.IDENT, content, file, start, position);
    }


    public Sexp nextSexp() {
        Token startToken = nextToken();
        if (startToken == null) {
            return null;
        }

        if (startToken.content.equals("(")) {
            String file = startToken.file;

            List<Sexp> tokens = new ArrayList<>();
            Sexp nextToken = nextSexp();
            while (!(nextToken instanceof Token && ((Token) nextToken).content.equals(")"))) {
                if (nextToken == null) {
                    _.abort("unclosed paren at: " + startToken.start);
                } else {
                    tokens.add(nextToken);
                    nextToken = nextSexp();
                }
            }
            return new Tuple(tokens, file, startToken.start, nextToken.end);

        } else {
            return startToken;
        }

    }


    public static void main(String[] args) {
        Parser p = new Parser(args[0]);
        _.msg(p.text);

//        Token t = p.nextToken();
//        while (t != null) {
//            _.msg("token: " + t);
//            t = p.nextToken();
//        }

        Sexp s = p.nextSexp();
        while (s != null) {
            _.msg("sexp: " + s);
            s = p.nextSexp();
        }

    }
}
