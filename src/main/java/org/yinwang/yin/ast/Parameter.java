package org.yinwang.yin.ast;


import org.yinwang.yin.Constants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Parameter {
    public List<Node> elements;
    public List<String> positional = new ArrayList<>();
    public Map<String, Node> keywords = new LinkedHashMap<>();


    public Parameter(List<Node> elements) throws ParseError {
        this.elements = elements;

        for (int i = 0; i < elements.size(); i++) {
            Node n1 = elements.get(i);
            if (n1 instanceof Name) {
                positional.add(((Name) n1).id);
            } else if (n1 instanceof Keyword) {
                positional.add(((Keyword) n1).id);
                if (i >= elements.size()) {
                    throw new ParseError(n1, "missing value for keyword: " + ((Keyword) n1).id);
                } else {
                    Node n2 = elements.get(i + 1);
                    keywords.put(((Keyword) n1).id, n2);
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
