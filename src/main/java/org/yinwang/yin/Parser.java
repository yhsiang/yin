package org.yinwang.yin;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {

    abstract class Sexp {
        public String file;
        public int start;
        public int end;


        protected Sexp(String file, int start, int end) {
            this.file = file;
            this.start = start;
            this.end = end;
        }
    }

    class Token extends Sexp {
        public String content;


        public Token(String content, String file, int start, int end) {
            super(file, start, end);
            this.content = content;
        }


        public String toString() {
            return content;
        }

    }


    class Tuple extends Sexp {
        public List<Sexp> tokens = new ArrayList<>();


        Tuple(List<Sexp> tokens, String file, int start, int end) {
            super(file, start, end);
            this.tokens = tokens;
        }


        public String toString() {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < tokens.size(); i++) {
                sb.append(tokens.get(i).toString());
                if (i != tokens.size() - 1) {
                    sb.append(" ");
                }
            }

            return "(" + sb.toString() + ")";
        }
    }


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
            return new Token(Character.toString(cur), file, position - 1, position);
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
        return new Token(content, file, start, position);
    }


    public Sexp nextSexp() {

        Token token = nextToken();
        if (token == null) {
            return null;
        }

        if (token.content.equals("(")) {
            String file = token.file;
            int start = token.start;
            int end = token.end;

            List<Sexp> tokens = new ArrayList<>();
            Sexp o = nextSexp();
            while (!(o instanceof Token && ((Token) o).content.equals(")"))) {
                if (o == null) {
                    _.abort("unclosed paranthese at: " + token);
                } else {
                    tokens.add(o);
                    end = o.end;
                    o = nextSexp();
                }
            }
            return new Tuple(tokens, file, start, end);

        } else {
            return token;
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
