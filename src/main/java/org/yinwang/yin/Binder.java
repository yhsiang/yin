package org.yinwang.yin;


import org.yinwang.yin.ast.Name;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.value.Value;

public class Binder {

    public static void bind(Node pattern, Value value, Scope env, boolean norewrite) {
        if (pattern instanceof Name) {
            String id = ((Name) pattern).id;
            Value v = env.lookupLocal(id);
            if (v == null || !norewrite) {
                env.put(id, value);
            } else {
                _.abort(pattern, "trying to redefine name: " + id);
            }
        }
    }


    public static void assign(Node pattern, Value value, Scope env) {
        bind(pattern, value, env, false);
    }


    public static void def(Node pattern, Value value, Scope env) {
        bind(pattern, value, env, true);
    }

}
