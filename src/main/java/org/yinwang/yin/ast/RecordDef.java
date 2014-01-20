package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.Record;
import org.yinwang.yin.value.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RecordDef extends Node {
    public Name name;
    public String qname;
    public List<Name> parents;
    public Map<String, Node> map = new LinkedHashMap<>();


    public RecordDef(Name name, List<Name> parents, List<Node> contents,
                     String file, int start, int end, int line, int col)
    {
        super(file, start, end, line, col);
        this.name = name;
        this.parents = parents;

        if (contents.size() % 2 != 0) {
            _.abort(this, "record initializer must have even number of elements");
        }

        for (int i = 0; i < contents.size(); i += 2) {
            Node key = contents.get(i);
            Node value = contents.get(i + 1);
            if (key instanceof Keyword) {
                if (value instanceof Keyword) {
                    _.abort(value, "keywords shouldn't be used as values: " + value);
                } else {
                    map.put(((Keyword) key).id, value);
                }
            } else {
                _.abort(key, "record initializer key is not a keyword: " + key);
            }
        }
    }


    public Value interp(Scope s) {
        Map<String, Value> valueMap = new LinkedHashMap<>();

        if (parents != null) {
            for (Node p : parents) {
                Value pv = p.interp(s);
                if (pv instanceof Record) {
                    for (Map.Entry<String, Value> e : ((Record) pv).values.entrySet()) {
                        Value existing = valueMap.get(e.getKey());
                        if (existing == null) {
                            valueMap.put(e.getKey(), e.getValue());
                        } else {
                            _.abort(p,
                                    "conflicting field " + e.getKey() + " inherited from parent: " + p + ", value: " + pv);
                            return null;
                        }
                    }
                } else {
                    _.abort(p, "parent is not a record");
                    return null;
                }
            }
        }

        for (Map.Entry<String, Node> e : map.entrySet()) {
            valueMap.put(e.getKey(), e.getValue().interp(s));
        }
        Value r = new Record(name.id, valueMap, this);
        s.put(name.id, r);
        return Value.VOID;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.RECORD_KEYWORD).append(" ");
        sb.append(name).append(" ");

        if (parents != null) {
            sb.append("(" + Node.printList(parents) + ") ");
        }

        for (Map.Entry<String, Node> e : map.entrySet()) {
            sb.append(":" + e.getKey() + " " + e.getValue());
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }
}
