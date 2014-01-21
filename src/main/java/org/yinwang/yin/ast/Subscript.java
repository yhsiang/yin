package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.IntValue;
import org.yinwang.yin.value.Value;
import org.yinwang.yin.value.Vector;

import java.util.List;

public class Subscript extends Node {
    public Node value;
    public Node index;


    public Subscript(Node value, Node index, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.value = value;
        this.index = index;
    }


    @Override
    public Value interp(Scope s) {
        Value vector = value.interp(s);
        Value indexValue = index.interp(s);

        if (!(vector instanceof Vector)) {
            _.abort(value, "subscripting non-vector: " + vector);
            return null;
        }

        if (!(indexValue instanceof IntValue)) {
            _.abort(value, "subscript " + index + " is not an integer: " + indexValue);
            return null;
        }

        List<Value> values = ((Vector) vector).values;
        int i = ((IntValue) indexValue).value;

        if (i < values.size()) {
            return values.get(i);
        } else {
            _.abort(this, "subscript " + i + " out of bound: " + (values.size() - 1));
            return null;
        }
    }


    public String toString() {
        return value + "." + index;
    }

}
