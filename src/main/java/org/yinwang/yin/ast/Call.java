package org.yinwang.yin.ast;


import org.yinwang.yin.Binder;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.*;

import java.util.*;

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

                    Node actual = args.keywords.get(param.id);
                    if (actual != null) {
                        seen.add(param.id);
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

                // detect extra arguments
                List<String> extra = new ArrayList<>();
                for (String id : args.keywords.keySet()) {
                    if (!seen.contains(id)) {
                        extra.add(id);
                    }
                }

                if (!extra.isEmpty()) {
                    _.abort(this, "extra keyword arguments: " + extra);
                    return Value.VOID;
                } else {
                    return closure.fun.body.interp(funScope);
                }
            }
        } else if (func instanceof Record) {
            Record template = (Record) func;
            Record copy = template.copy();

            for (Map.Entry<String, Node> e : args.keywords.entrySet()) {
                copy.values.put(e.getKey(), e.getValue().interp(s));
            }

            // instantiate
            return copy;
        } else if (func instanceof PrimFun) {
            String name = ((PrimFun) func).name;
            List<Value> values = Node.interpList(args.positional, s);

            if (name.equals("+")) {
                if (values.size() == 2) {
                    Value v1 = values.get(0);
                    Value v2 = values.get(1);
                    if (v1 instanceof IntValue && v2 instanceof IntValue) {
                        return new IntValue(((IntValue) v1).value + ((IntValue) v2).value);
                    }
                    if (v1 instanceof FloatValue && v2 instanceof FloatValue) {
                        return new FloatValue(((FloatValue) v1).value + ((FloatValue) v2).value);
                    }
                    if (v1 instanceof FloatValue && v2 instanceof IntValue) {
                        return new FloatValue(((FloatValue) v1).value + ((IntValue) v2).value);
                    }
                    if (v1 instanceof IntValue && v2 instanceof FloatValue) {
                        return new FloatValue(((IntValue) v1).value + ((FloatValue) v2).value);
                    }
                } else {
                    _.abort(this, "wrong number of arguments to +: " + values.size());
                    return null;
                }
            }

            _.abort(this.func, "unrecognized operator: " + func);
            return null;
        } else {
            _.abort(this.func, "calling non-function: " + func);
            return Value.VOID;
        }
    }


    public String toString() {
        if (args.positional.size() != 0) {
            return "(" + func + " " + args + ")";
        } else {
            return "(" + func + ")";
        }
    }

}
