package org.yinwang.yin.ast;


import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin.value.Closure;
import org.yinwang.yin.value.Value;

import java.util.HashMap;
import java.util.Map;

public class Fun extends Node {
    public Parameter params;
    public Node body;


    public Fun(Parameter params, Node body, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.params = params;
        this.body = body;
    }


    public Value interp(Scope s) {
        Map<String, Value> defaults = new HashMap<>();
        for (Map.Entry<String, Node> e : params.valueMap.entrySet()) {
            Value v = e.getValue().interp(s);
            defaults.put(e.getKey(), v);
        }
        return new Closure(this, defaults, s);
    }


    public String toString() {
        return "(" + Constants.FUN_KEYWORD + " (" + params + ") " + body + ")";
    }

}
