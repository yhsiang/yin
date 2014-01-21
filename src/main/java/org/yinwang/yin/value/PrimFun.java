package org.yinwang.yin.value;


import org.yinwang.yin.ast.Name;
import org.yinwang.yin.ast.Node;

public class PrimFun extends Value {
    public String name;


    public PrimFun(String name) {
        this.name = name;
    }


    public static boolean isOp(Node node) {
        if (node instanceof Name) {
            String id = ((Name) node).id;
            return id.equals("+") || id.equals("-") || id.equals("*") || id.equals("/");
        }
        return false;
    }


    public String toString() {
        return name;
    }

}
