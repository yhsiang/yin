package org.yinwang.yin;


import org.yinwang.yin.ast.*;
import org.yinwang.yin.value.Value;
import org.yinwang.yin.value.Vector;

import java.util.List;

public class Binder {

    public static void define(Node pattern, Value value, Scope env) {
        if (pattern instanceof Name) {
            String id = ((Name) pattern).id;
            Value v = env.lookupLocal(id);
            if (v != null) {
                _.abort(pattern, "trying to redefine name: " + id);
            } else {
                env.put(id, value);
            }
        } else if (pattern instanceof VectorLiteral) {
            if (value instanceof Vector) {
                List<Node> elms1 = ((VectorLiteral) pattern).elements;
                List<Value> elms2 = ((Vector) value).values;
                if (elms1.size() == elms2.size()) {
                    for (int i = 0; i < elms1.size(); i++) {
                        define(elms1.get(i), elms2.get(i), env);
                    }
                } else {
                    _.abort(pattern,
                            "define with vectors of different sizes: " + elms1.size() + " v.s. " + elms2.size());
                }
            }
        } else {
            _.abort(pattern, "unsupported pattern of define: " + pattern);
        }
    }


    public static void assign(Node pattern, Value value, Scope env) {
        if (pattern instanceof Name) {
            String id = ((Name) pattern).id;
            Scope d = env.findDefiningScope(id);

            if (d == null) {
                _.abort(pattern, "assigned name was not defined: " + id);
            } else {
                d.put(id, value);
            }
        } else if (pattern instanceof Subscript) {
            ((Subscript) pattern).set(value, env);
        } else if (pattern instanceof Attr) {
            ((Attr) pattern).set(value, env);
        } else if (pattern instanceof VectorLiteral) {
            if (value instanceof Vector) {
                List<Node> elms1 = ((VectorLiteral) pattern).elements;
                List<Value> elms2 = ((Vector) value).values;
                if (elms1.size() == elms2.size()) {
                    for (int i = 0; i < elms1.size(); i++) {
                        assign(elms1.get(i), elms2.get(i), env);
                    }
                } else {
                    _.abort(pattern, "assign vectors of different sizes: " + elms1.size() + " v.s. " + elms2.size());
                }
            }
        } else {
            _.abort(pattern, "unsupported pattern of assign: " + pattern);
        }
    }

}
