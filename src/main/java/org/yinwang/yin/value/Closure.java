package org.yinwang.yin.value;


import org.yinwang.yin.Scope;
import org.yinwang.yin.ast.Fun;

import java.util.HashMap;
import java.util.Map;

public class Closure extends Value {

    public Fun fun;
    public Map<String, Value> defaults;
    public Scope env;


    public Closure(Fun fun, Map<String, Value> defaults, Scope env) {
        this.fun = fun;
        this.defaults = defaults;
        this.env = env;
    }

}
