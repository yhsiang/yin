package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin.value.Value;

public class Attr extends Node {
    public Node value;
    public Node attr;


    public Attr(Node value, Node attr, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.value = value;
        this.attr = attr;
    }


    @Override
    public Value interp(Scope s) {
        return null;
    }


    public String toString() {
        return value + "." + attr;
    }

}
