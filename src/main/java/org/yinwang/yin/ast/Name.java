package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.PrimFun;
import org.yinwang.yin.value.Value;

public class Name extends Node {
    public String id;


    public Name(String id, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.id = id;
    }


    public Value interp(Scope s) {
        Value v = s.lookup(id);
        if (v != null) {
            return v;
        } else if (PrimFun.isOp(this)) {
            return new PrimFun(id);
        } else {
            _.abort(this, "unbound variable: " + id);
            return Value.VOID;
        }
    }


    public String toString() {
        return id;
    }
}
