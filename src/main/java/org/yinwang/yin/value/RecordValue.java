package org.yinwang.yin.value;


import org.yinwang.yin.Constants;

import java.util.Map;


public class RecordValue extends Value {

    public String name;
    public RecordType type;
    public Map<String, Value> values;


    public RecordValue(String name, RecordType type, Map<String, Value> values) {
        this.name = name;
        this.type = type;
        this.values = values;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.RECORD_KEYWORD).append(" ");
        sb.append(name == null ? "_" : name);

        for (String field : values.keySet()) {
            sb.append(" ").append(Constants.ARRAY_BEGIN);
            sb.append(field).append(" ");
            sb.append(values.get(field));
            sb.append(Constants.ARRAY_END);
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }

}
