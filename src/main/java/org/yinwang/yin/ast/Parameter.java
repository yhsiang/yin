package org.yinwang.yin.ast;


import org.yinwang.yin._;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Parameter {

    public List<Node> elements;
    public List<Name> positional = new ArrayList<>();
    public Map<String, Node> keywords = new LinkedHashMap<>();


    public Parameter(List<Node> elements) {
        this.elements = elements;

        for (int i = 0; i < elements.size(); i++) {
            Node key = elements.get(i);
            if (key instanceof Name) {
                positional.add((Name) key);
            } else if (key instanceof Keyword) {
                positional.add(((Keyword) key).asName());
                if (i >= elements.size() - 1) {
                    _.abort(key, "missing value for keyword: " + key);
                } else {
                    Node value = elements.get(i + 1);
                    if (value instanceof Keyword) {
                        _.abort(value, "keywords can't be used as values: " + value);
                    } else {
                        keywords.put(((Keyword) key).id, value);
                        i++;
                    }
                }
            } else {
                // parameter does not allow other things
                _.abort(key, "illegal argument form: " + key);
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
