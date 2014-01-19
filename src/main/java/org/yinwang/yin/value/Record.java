package org.yinwang.yin.value;


import org.yinwang.yin.Constants;
import org.yinwang.yin.ast.Node;

import java.util.LinkedHashMap;
import java.util.Map;


public class Record extends Value {
    public String name;
    public Map<String, Value> values;
    public Node definition;


    public Record(String name, Map<String, Value> values, Node definition) {
        this.name = name;
        this.values = values;
        this.definition = definition;
    }


    public Record copy() {
        Map<String, Value> newValues = new LinkedHashMap<>();
        newValues.putAll(values);
        return new Record(name, values, definition);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.RECORD_KEYWORD).append(" ");
        sb.append(name);

        for (Map.Entry<String, Value> e : values.entrySet()) {
            sb.append(" :" + e.getKey() + " " + e.getValue());
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }

}
