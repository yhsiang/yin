package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.Value;

import java.util.Map;


public class Declare extends Node {
    public Scope propsNode;
    public Scope properties;


    public Declare(Scope propsNode, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.propsNode = propsNode;
    }


    public Value interp(Scope s) {
        mergeProperties(propsNode, s);
        return Value.VOID;
    }


    // helper
    // evaluate the properties inside propsNode
    // then merge into the Scope s
    public static void mergeProperties(Scope unevaled, Scope s) {
        // evaluate the properties
        Scope evaled = evalProperties(unevaled, s);

        // merge the properties into current scope
        s.putAll(evaled);

        // set default values for variables
        for (String key : evaled.keySet()) {
            Object defaultValue = evaled.lookupPropertyLocal(key, "default");
            if (defaultValue == null) {
                continue;
            } else if (defaultValue instanceof Value) {
                Value existing = s.lookup(key);
                if (existing == null) {
                    s.putValue(key, (Value) defaultValue);
                }
            } else {
                _.abort("default value is not a value, shouldn't happen");
            }
        }
    }


    public static Scope evalProperties(Scope unevaled, Scope s) {
        Scope evaled = new Scope();

        for (String field : unevaled.keySet()) {
            Map<String, Object> props = unevaled.lookupAllProps(field);
            for (Map.Entry<String, Object> e : props.entrySet()) {
                Object v = e.getValue();
                if (v instanceof Node) {
                    Value vValue = ((Node) v).interp(s);
                    evaled.put(field, e.getKey(), vValue);
                } else {
                    _.abort("property is not a node, parser bug: " + v);
                }
            }
        }
        return evaled;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.DECLARE_KEYWORD).append(" ");

        for (String field : propsNode.keySet()) {
            Map<String, Object> props = propsNode.lookupAllProps(field);
            for (Map.Entry<String, Object> e : props.entrySet()) {
                sb.append(" :" + e.getKey() + " " + e.getValue());
            }
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }
}
