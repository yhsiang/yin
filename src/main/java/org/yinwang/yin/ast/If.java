package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin.value.*;

public class If extends Node {
    public Node test;
    public Node then;
    public Node orelse;


    public If(Node test, Node then, Node orelse, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.test = test;
        this.then = then;
        this.orelse = orelse;
    }


    public YinValue interp(Scope s) {
        return null;
    }


    public String toString() {
        return "(" + Constants.IF_KEYWORD + " " + test + " " + then + " " + orelse + ")";
    }

}
