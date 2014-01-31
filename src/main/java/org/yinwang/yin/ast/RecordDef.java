package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.RecordType;
import org.yinwang.yin.value.Value;

import java.util.List;
import java.util.Map;


public class RecordDef extends Node {
    public Name name;
    public List<Name> parents;
    public Scope propsNode;
    public Scope properties;


    public RecordDef(Name name, List<Name> parents, Scope propsNode,
                     String file, int start, int end, int line, int col)
    {
        super(file, start, end, line, col);
        this.name = name;
        this.parents = parents;
        this.propsNode = propsNode;
    }


    public Value interp(Scope s) {
        Scope properties = new Scope();

        if (parents != null) {
            for (Node p : parents) {
                Value pv = p.interp(s);
                if (pv instanceof RecordType) {
                    Scope parentProps = ((RecordType) pv).properties;

                    // check for duplicated keys
                    for (String key : parentProps.keySet()) {
                        Value existing = properties.lookupLocal(key);
                        if (existing != null) {
                            _.abort(p, "conflicting field " + key +
                                    " inherited from parent: " + p + ", value: " + pv);
                            return null;
                        }
                    }

                    // add all properties or all fields in parent
                    properties.putAll(parentProps);
                } else {
                    _.abort(p, "parent is not a record");
                    return null;
                }
            }
        }

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

        Value r = new RecordType(name.id, this, properties);
        s.putValue(name.id, r);
        return Value.VOID;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.RECORD_KEYWORD).append(" ");
        sb.append(name);

        if (parents != null) {
            sb.append(" (" + Node.printList(parents) + ")");
        }

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
