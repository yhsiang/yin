package org.yinwang.yin;


import org.yinwang.yin.ast.Name;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.value.Value;

public class Binder {

    public static void bind(Node pattern, Value value, Scope env) {
        if (pattern instanceof Name) {
            env.put(((Name) pattern).id, value);
        }
    }
}
