package org.yinwang.yin;


import org.yinwang.yin.ast.Name;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.value.Value;

public class Binder {

    public static void bind(Node pattern, Value value, Scope env, boolean assign) {
        if (pattern instanceof Name) {
            if (assign) {
                assignName((Name) pattern, value, env);
            } else {
                defineName((Name) pattern, value, env);
            }
        }
    }


    public static void define(Node pattern, Value value, Scope env) {
        bind(pattern, value, env, false);
    }


    public static void assign(Node pattern, Value value, Scope env) {
        bind(pattern, value, env, true);
    }


    public static void defineName(Name name, Value value, Scope env) {
        String id = name.id;
        Value v = env.lookupLocal(id);
        if (v != null) {
            _.abort(name, "trying to redefine name: " + id);
        } else {
            env.put(id, value);
        }
    }


    public static void assignName(Name name, Value value, Scope env) {
        String id = name.id;
        Scope d = env.findDefiningScope(id);

        if (d == null) {
            _.abort(name, "assigned name was not defined: " + id);
        } else {
            d.put(id, value);
        }
    }

}
