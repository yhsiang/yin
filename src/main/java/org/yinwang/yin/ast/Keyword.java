package org.yinwang.yin.ast;


public class Keyword extends Node {
    public String id;


    public Keyword(String id, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.id = id;
    }


    public String toString() {
        return ":" + id;
    }
}
