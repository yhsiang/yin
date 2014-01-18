package org.yinwang.yin.parser;

import java.util.ArrayList;
import java.util.List;


class Tuple extends Node {
    public List<Node> tokens = new ArrayList<>();
    public Node open;
    public Node close;


    public Tuple(List<Node> tokens, Node open, Node close, String file, int start, int end, int line, int col) {
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

        return (open == null ? "" : open) + sb.toString() + (close == null ? "" : close);
    }
}
