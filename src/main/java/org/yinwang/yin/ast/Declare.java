package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.Value;

import java.util.Map;


public class Declare extends Node {
    public Scope propertyForm;


    public Declare(Scope propertyForm, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.propertyForm = propertyForm;
    }


    public Value interp(Scope s) {
//        mergeProperties(propsNode, s);
        return Value.VOID;
    }


    @Override
    public Value typecheck(Scope s) {
        return null;
    }


    // helper
    // evaluate the properties inside propsNode
    // then merge into the Scope s
    public static void mergeProperties(Scope properties, Scope s) {
        // merge the properties into current scope
        s.putAll(properties);

        // set default values for variables
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
                _.abort("default value is not a value, shouldn't happen");
            }
        }
    }


    public static void mergeTypeProperties(Scope properties, Scope s) {
        // merge the properties into current scope
        s.putAll(properties);

        // set default values for variables
        for (String key : properties.keySet()) {
            Object defaultValue = properties.lookupPropertyLocal(key, "type");
            if (defaultValue == null) {
                continue;
            } else if (defaultValue instanceof Value) {
                Value existing = s.lookup(key);
                if (existing == null) {
                    s.putValue(key, (Value) defaultValue);
                }
            } else if (defaultValue instanceof Node) {
                Value existing = s.lookup(key);
                if (existing == null) {
                    s.put(key, "type", defaultValue);
                }
            } else {
                _.abort("illegal type, shouldn't happen" + defaultValue);
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


    public static Scope typecheckProperties(Scope unevaled, Scope s) {
        Scope evaled = new Scope();

        for (String field : unevaled.keySet()) {
            if (field.equals(Constants.RETURN_ARROW)) {
                evaled.putProperties(field, unevaled.lookupAllProps(field));
            } else {
                Map<String, Object> props = unevaled.lookupAllProps(field);
                for (Map.Entry<String, Object> e : props.entrySet()) {
                    Object v = e.getValue();
                    if (v instanceof Node) {
                        Value vValue = ((Node) v).typecheck(s);
                        evaled.put(field, e.getKey(), vValue);
                    } else {
                        _.abort("property is not a node, parser bug: " + v);
                    }
                }
            }
        }
        return evaled;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.DECLARE_KEYWORD).append(" ");

        for (String field : propertyForm.keySet()) {
            Map<String, Object> props = propertyForm.lookupAllProps(field);
            for (Map.Entry<String, Object> e : props.entrySet()) {
                sb.append(" :" + e.getKey() + " " + e.getValue());
            }
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }
}
