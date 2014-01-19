package org.yinwang.yin;


import org.yinwang.yin.value.Value;

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

}
