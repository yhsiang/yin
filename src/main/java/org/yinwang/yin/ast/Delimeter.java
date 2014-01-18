package org.yinwang.yin.ast;


public class Delimeter extends Node {
    public String shape;


    public Delimeter(String shape, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.shape = shape;
    }


    public String toString() {
        return shape;
    }
}

