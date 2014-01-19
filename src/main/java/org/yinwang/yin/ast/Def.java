package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;

public class Def extends Node {
    public Node pattern;
    public Node value;


    public Def(Node pattern, Node value, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.pattern = pattern;
        this.value = value;
    }


    public String toString() {
        return "(" + Constants.DEF_KEYWORD + " " + pattern + " " + value + ")";
    }

}
