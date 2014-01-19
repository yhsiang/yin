package org.yinwang.yin.ast;


public class Call extends Node {
    public Node func;
    public Parameter args;


    public Call(Node func, Parameter args, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.func = func;
        this.args = args;
    }


    public String toString() {
        return "(" + func + " " + args + ")";
    }

}
