package org.yinwang.yin.ast;

import java.util.ArrayList;
import java.util.List;


class Tuple extends Sexp {
    public List<Sexp> tokens = new ArrayList<>();
    public String open;
    public String close;


    Tuple(List<Sexp> tokens, String open, String close, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.tokens = tokens;
        this.open = open;
        this.close = close;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {
            sb.append(tokens.get(i).toString());
            if (i != tokens.size() - 1) {
                sb.append(" ");
            }
        }

        return open + sb.toString() + close;
    }
}
