package org.yinwang.yin.value;


import org.yinwang.yin.Constants;
import org.yinwang.yin.ast.Node;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class RecordType extends Value {

    public String name;
    public Map<String, Value> valueMap;
    public Map<String, Value> typeMap;
    public Node definition;


    public RecordType(String name, Map<String, Value> typeMap, Map<String, Value> valueMap, Node definition) {
        this.name = name;
        this.typeMap = typeMap;
        this.valueMap = valueMap;
        this.definition = definition;
    }


    public RecordType(String name, Map<String, Value> valueMap, Node definition) {
        this(name, new HashMap<String, Value>(), valueMap, definition);
    }


    public RecordType copy() {
        Map<String, Value> newValues = new LinkedHashMap<>();
        newValues.putAll(valueMap);
        Map<String, Value> newTypes = new LinkedHashMap<>();
        newTypes.putAll(typeMap);
        return new RecordType(name, typeMap, newValues, definition);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.RECORD_KEYWORD).append(" ");
        sb.append(name == null ? "_" : name);

        for (Map.Entry<String, Value> e : valueMap.entrySet()) {
            Value type = typeMap.get(e.getKey());
            sb.append(" (:" + e.getKey() + " " + type + " " + e.getValue() + ")");
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }

}
