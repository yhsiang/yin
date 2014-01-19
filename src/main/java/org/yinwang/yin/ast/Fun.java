package org.yinwang.yin.ast;


import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin.value.Closure;
import org.yinwang.yin.value.Value;

public class Fun extends Node {
    public Parameter params;
    public Node body;


    public Fun(Parameter params, Node body, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.params = params;
        this.body = body;
    }


    public Value interp(Scope s) {
        return new Closure(this, s);
    }


    public String toString() {
        return "(" + Constants.FUN_KEYWORD + " (" + params + ") " + body + ")";
    }

}
