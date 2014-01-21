package org.yinwang.yin;


import org.yinwang.yin.ast.Attr;
import org.yinwang.yin.ast.Name;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.ast.Subscript;
import org.yinwang.yin.value.Value;

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
        } else {
            _.abort(pattern, "unsupported pattern of assign: " + pattern);
        }
    }

}
