package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;

public class Assign extends Node {
    public Node pattern;
    public Node value;


    public Assign(Node pattern, Node value, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.pattern = pattern;
        this.value = value;
    }


    public String toString() {
        return "(" + Constants.ASSIGN_KEYWORD + " " + pattern + " " + value + ")";
    }

}
