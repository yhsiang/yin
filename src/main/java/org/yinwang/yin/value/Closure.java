package org.yinwang.yin.value;


import org.yinwang.yin.Scope;
import org.yinwang.yin.ast.Fun;

public class Closure extends Value {

    public Fun fun;
    public Scope env;


    public Closure(Fun fun, Scope env) {
        this.fun = fun;
        this.env = env;
    }

}
