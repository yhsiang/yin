package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.parser.Parser;
import org.yinwang.yin.value.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Declare extends Node {
    public Scope propsNode;
    public Scope properties;


    public Declare(Scope propsNode, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.propsNode = propsNode;
    }


    public Value interp(Scope s) {
        Scope properties = new Scope();
        for (String field : propsNode.keySet()) {
            Map<String, Object> props = propsNode.lookupAllProps(field);
            for (Map.Entry<String, Object> e : props.entrySet()) {
                Object v = e.getValue();
                if (v instanceof Node) {
                    Value vValue = ((Node) v).interp(s);
                    properties.put(field, e.getKey(), vValue);
                } else {
                    _.abort(this, "property is not a node, parser bug: " + v);
                }
            }
        }

        s.putAll(properties);

        for (String key : properties.keySet()) {
            Object defaultValue = properties.lookupPropertyLocal(key, "default");
            if (defaultValue == null) {
                continue;
            } else if (defaultValue instanceof Value) {
                Value existing = s.lookup(key);
                if (existing == null) {
                    s.putValue(key, (Value) defaultValue);
                }
            } else {
                _.abort("default value is not value, shouldn't happen");
            }
        }

        return Value.VOID;
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
