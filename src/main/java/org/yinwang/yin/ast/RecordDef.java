package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.parser.Parser;
import org.yinwang.yin.value.RecordType;
import org.yinwang.yin.value.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RecordDef extends Node {
    public Name name;
    public List<Name> parents;
    public Map<String, Node> valueMap = new LinkedHashMap<>();
    public Map<String, Node> typeMap = new LinkedHashMap<>();


    public RecordDef(Name name, List<Name> parents, List<Node> contents,
                     String file, int start, int end, int line, int col)
    {
        super(file, start, end, line, col);
        this.name = name;
        this.parents = parents;

        for (int i = 0; i < contents.size(); i++) {
            Node tuple = contents.get(i);
            if (tuple instanceof Tuple) {
                List<Node> elements = Parser.parseList(((Tuple) tuple).elements);

                if (elements.size() == 3) {
                    Node fieldName = elements.get(0);
                    Node type = elements.get(1);
                    Node value = elements.get(2);

                    if (!(fieldName instanceof Keyword)) {
                        _.abort(fieldName, "record initializer key is not a keyword: " + fieldName);
                    } else {
                        typeMap.put(((Keyword) fieldName).id, type);
                        valueMap.put(((Keyword) fieldName).id, value);
                    }
                } else if (elements.size() == 2) {
                    Node fieldName = elements.get(0);
                    Node type = elements.get(1);

                    if (!(fieldName instanceof Keyword)) {
                        _.abort(fieldName, "record initializer key is not a keyword: " + fieldName);
                    } else {
                        typeMap.put(((Keyword) fieldName).id, type);
                    }
                }
            }
        }
    }


    public Value interp(Scope s) {
        Map<String, Value> tm = new LinkedHashMap<>();
        Map<String, Value> vm = new LinkedHashMap<>();

        if (parents != null) {
            for (Node p : parents) {
                Value pv = p.interp(s);
                if (pv instanceof RecordType) {
                    for (Map.Entry<String, Value> e : ((RecordType) pv).typeMap.entrySet()) {
                        Value existing = vm.get(e.getKey());
                        if (existing == null) {
                            tm.put(e.getKey(), e.getValue());
                        } else {
                            _.abort(p, "conflicting field " + e.getKey() +
                                    " inherited from parent: " + p + ", value: " + pv);
                            return null;
                        }
                    }

                    for (Map.Entry<String, Value> e : ((RecordType) pv).valueMap.entrySet()) {
                        Value existing = vm.get(e.getKey());
                        if (existing == null) {
                            vm.put(e.getKey(), e.getValue());
                        } else {
                            _.abort(p, "conflicting field " + e.getKey() +
                                    " inherited from parent: " + p + ", value: " + pv);
                            return null;
                        }
                    }
                } else {
                    _.abort(p, "parent is not a record");
                    return null;
                }
            }
        }

        for (Map.Entry<String, Node> e : this.typeMap.entrySet()) {
            tm.put(e.getKey(), e.getValue().interp(s));
        }

        for (Map.Entry<String, Node> e : this.valueMap.entrySet()) {
            vm.put(e.getKey(), e.getValue().interp(s));
        }

        Value r = new RecordType(name.id, tm, vm, this);
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

        for (Map.Entry<String, Node> e : valueMap.entrySet()) {
            sb.append(" :" + e.getKey() + " " + e.getValue());
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }
}
