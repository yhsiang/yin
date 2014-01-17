package org.yinwang.yin.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinwang on 1/17/14.
 */
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
