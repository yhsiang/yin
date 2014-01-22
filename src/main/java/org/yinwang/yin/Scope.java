package org.yinwang.yin;


import org.yinwang.yin.value.BoolType;
import org.yinwang.yin.value.BoolValue;
import org.yinwang.yin.value.IntType;
import org.yinwang.yin.value.Value;
import org.yinwang.yin.value.primitives.*;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    public Map<String, Value> table = new HashMap<>();
    public Scope parent;


    public Scope() {
        this.parent = null;
    }


    public Scope(Scope parent) {
        this.parent = parent;
    }


    public Value lookupLocal(String name) {
        return table.get(name);
    }


    public Value lookup(String name) {
        Value v = table.get(name);
        if (v != null) {
            return v;
        } else if (parent != null) {
            return parent.lookup(name);
        } else {
            return null;
        }
    }


    public Scope findDefiningScope(String name) {
        Value v = table.get(name);
        if (v != null) {
            return this;
        } else if (parent != null) {
            return parent.findDefiningScope(name);
        } else {
            return null;
        }
    }


    public static Scope buildInitScope() {
        Scope init = new Scope();

        init.put("+", new Add());
        init.put("-", new Sub());
        init.put("*", new Mult());
        init.put("/", new Div());
        init.put("<", new Lt());
        init.put("<=", new LtE());
        init.put(">", new Gt());
        init.put(">=", new GtE());
        init.put("=", new Eq());
        init.put("true", new BoolValue(true));
        init.put("false", new BoolValue(false));
        init.put("Int", new IntType());
        init.put("Bool", new BoolType());

        return init;
    }


    public void put(String name, Value value) {
        table.put(name, value);
    }

}
