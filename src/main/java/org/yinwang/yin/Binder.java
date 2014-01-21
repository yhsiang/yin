package org.yinwang.yin;


import org.yinwang.yin.ast.Name;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.value.Value;

public class Binder {

    public static void bind(Node pattern, Value value, Scope env, boolean assign) {
        if (pattern instanceof Name) {
            String id = ((Name) pattern).id;
            Value v = env.lookupLocal(id);

            if (assign && v == null) {
                _.abort(pattern, "assigned name was not defined: " + id);
            } else if (!assign && v != null) {
                _.abort(pattern, "trying to redefine name: " + id);
            } else {
                env.put(id, value);
            }
        }
    }


    public static void assign(Node pattern, Value value, Scope env) {
        bind(pattern, value, env, true);
    }


    public static void def(Node pattern, Value value, Scope env) {
        bind(pattern, value, env, false);
    }

}
