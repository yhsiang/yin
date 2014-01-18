package org.yinwang.yin.ast;


public class Str extends Node {
    public String value;


    public Str(String value, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.value = value;
    }


    public String toString() {
        return "\"" + value + "\"";
    }

}
