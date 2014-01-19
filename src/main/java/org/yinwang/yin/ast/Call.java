package org.yinwang.yin.ast;


import org.yinwang.yin.Binder;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.Closure;
import org.yinwang.yin.value.Value;

import java.util.HashSet;
import java.util.Set;

public class Call extends Node {
    public Node func;
    public Argument args;


    public Call(Node func, Argument args, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.func = func;
        this.args = args;
    }


    public Value interp(Scope s) {
        Value func = this.func.interp(s);

        if (func instanceof Closure) {
            Closure closure = (Closure) func;
            Scope funScope = new Scope(closure.env);
            Parameter params = closure.fun.params;

            if (!args.positional.isEmpty() && args.keywords.isEmpty()) {
                // positional
                if (args.positional.size() == params.positional.size()) {
                    for (int i = 0; i < args.positional.size(); i++) {
                        Value value = args.positional.get(i).interp(funScope);
                        Binder.def(params.positional.get(i), value, funScope);
                    }
                    return closure.fun.body.interp(funScope);
                } else {
                    _.abort(this.func, "calling function with wrong number of arguments: " + args.positional.size());
                    return Value.VOID;
                }
            } else {
                // keywords
                Set<String> seen = new HashSet<>();

                // try to bind all arguments
                for (Name param : params.positional) {
                    seen.add(param.id);

                    Node actual = args.keywords.get(param.id);
                    if (actual != null) {
                        Value value = actual.interp(funScope);
                        funScope.put(param.id, value);
                    } else {
                        Value defaultValue = closure.defaults.get(param.id);
                        if (defaultValue != null) {
                            funScope.put(param.id, defaultValue);
                        } else {
                            _.abort(param, "argument not supplied for: " + param);
                            return Value.VOID;
                        }
                    }
                }

                // not allow extra arguments
                for (String id : params.keywords.keySet()) {
                    if (!seen.contains(id)) {
                        _.abort(this, "extra keyword argument supplied: " + id);
                        return Value.VOID;
                    }
                }
                return closure.fun.body.interp(funScope);
            }
        } else {
            _.abort(this.func, "calling non-function");
            return Value.VOID;
        }
    }


    public String toString() {
        return "(" + func + " " + args + ")";
    }

}
