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
        Value fun = this.func.interp(s);
        if (fun instanceof Closure) {
            Closure closure = (Closure) fun;
            Scope funScope = new Scope(closure.env);
            List<Name> params = closure.fun.params;

            if (closure.properties != null) {
                Declare.mergeProperties(closure.properties, funScope);
            }

            if (!args.positional.isEmpty() && args.keywords.isEmpty()) {
                // positional
                if (args.positional.size() != params.size()) {
                    _.abort(this.func,
                            "calling function with wrong number of arguments. expected: " + params.size()
                                    + " actual: " + args.positional.size());
                }

                for (int i = 0; i < args.positional.size(); i++) {
                    Value value = args.positional.get(i).interp(s);
                    Binder.define(params.get(i), value, funScope);
                }
                return closure.fun.body.interp(funScope);
            } else {
                // keywords
                Set<String> seen = new HashSet<>();

                // try to bind all arguments
                for (Name param : params) {
                    Node actual = args.keywords.get(param.id);
                    if (actual != null) {
                        seen.add(param.id);
                        Value value = actual.interp(funScope);
                        funScope.putValue(param.id, value);
                    }
//                    else {
//                        _.abort(param, "argument not supplied for: " + param);
//                        return Value.VOID;
//                    }
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
        } else if (fun instanceof RecordType) {
            RecordType template = (RecordType) fun;
            Scope values = new Scope();

            // set default values for fields
            Declare.mergeProperties(template.properties, values);

            // set actual values, overwrite defaults if any
            for (Map.Entry<String, Node> e : args.keywords.entrySet()) {
                if (!template.properties.keySet().contains(e.getKey())) {
                    _.abort(this, "extra keyword argument: " + e.getKey());
                } else {
                    values.putValue(e.getKey(), e.getValue().interp(s));
                }
            }

            // check uninitialized fields
            for (String field : template.properties.keySet()) {
                if (!values.containsKey(field)) {
                    _.abort(this, "field is not initialized: " + field);
                }
            }

            // instantiate
            return new RecordValue(template.name, template, values);
        } else if (fun instanceof PrimFun) {
            PrimFun prim = (PrimFun) fun;
            if (args.positional.size() != prim.arity) {
                _.abort(this, "incorrect number of arguments for primitive " +
                        prim.name + ", expecting " + prim.arity + ", but got " + args.positional.size());
                return null;
            } else {
                List<Value> args = Node.interpList(this.args.positional, s);
                return prim.apply(args, this);
            }
        } else {
            _.abort(this.func, "calling non-function: " + fun);
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
