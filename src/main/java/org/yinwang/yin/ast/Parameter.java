package org.yinwang.yin.ast;


import org.yinwang.yin._;
import org.yinwang.yin.parser.Parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Parameter {

    public List<Node> elements;
    public List<Name> positional = new ArrayList<>();
    public Map<String, Node> valueMap = new LinkedHashMap<>();
    public Map<String, Node> typeMap = new LinkedHashMap<>();


    public Parameter(List<Node> contents) {
        this.elements = contents;

        for (int i = 0; i < contents.size(); i++) {
            Node tuple = contents.get(i);
            if (tuple instanceof Tuple) {
                List<Node> elements = Parser.parseList(((Tuple) tuple).elements);

                if (elements.size() == 3) {
                    Node fieldName = elements.get(0);
                    Node type = elements.get(1);
                    Node value = elements.get(2);

                    if (!(fieldName instanceof Keyword)) {
                        _.abort(fieldName, "argument initializer key is not a keyword: " + fieldName);
                    } else {
                        positional.add(((Keyword) fieldName).asName());
                        typeMap.put(((Keyword) fieldName).id, type);
                        valueMap.put(((Keyword) fieldName).id, value);
                    }
                } else if (elements.size() == 2) {
                    Node fieldName = elements.get(0);
                    Node type = elements.get(1);

                    if (!(fieldName instanceof Keyword)) {
                        _.abort(fieldName, "argument initializer key is not a keyword: " + fieldName);
                    } else {
                        positional.add(((Keyword) fieldName).asName());
                        typeMap.put(((Keyword) fieldName).id, type);
                    }
                } else {
                    _.abort(tuple, "illegal argument form: " + tuple);
                }
            }
        }
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Node e : elements) {
            if (!first) {
                sb.append(" ");
            }
            sb.append(e);
            first = false;
        }
        return sb.toString();
    }

}
