package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin.value.Value;

public class Str extends Node {
    public String value;


    public Str(String value, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.value = value;
    }


    public Value interp(Scope s) {
        return null;
    }


    public String toString() {
        return "\"" + value + "\"";
    }

}
