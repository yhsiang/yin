package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.IntValue;
import org.yinwang.yin.value.Value;
import org.yinwang.yin.value.Vector;

import java.util.List;

public class Subscript extends Node {
    public Node value;
    public IntNum sub;


    public Subscript(Node value, IntNum sub, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.value = value;
        this.sub = sub;
    }


    @Override
    public Value interp(Scope s) {
        Value vector = value.interp(s);
        Value subValue = sub.interp(s);

        if (!(vector instanceof Vector)) {
            _.abort(value, "subscripting non-vector: " + vector);
            return null;
        }

        if (!(subValue instanceof IntValue)) {
            _.abort(value, "subscript " + sub + " is not an integer: " + subValue);
            return null;
        }

        List<Value> values = ((Vector) vector).values;
        int subInt = ((IntValue) subValue).value;

        if (subInt < values.size()) {
            return values.get(subInt);
        } else {
            _.abort(this, "subscript " + subInt + " out of bound: " + (values.size() - 1));
            return null;
        }
    }


    public String toString() {
        return value + "." + sub;
    }

}
