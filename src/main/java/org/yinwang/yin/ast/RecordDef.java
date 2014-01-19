package org.yinwang.yin.ast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RecordDef extends Node {

    public Map<String, Node> map = new LinkedHashMap<>();
    public Node open;
    public Node close;


    public RecordDef(List<Node> contents, Node open, Node close, String file, int start, int end, int line,
                     int col) throws ParseError
    {
        super(file, start, end, line, col);

        if (contents.size() % 2 != 0) {
            throw new ParseError(this, "record initializer must have even number of elements");
        }

        for (int i = 0; i < contents.size(); i += 2) {
            Node key = contents.get(i);
            Node value = contents.get(i + 1);
            if (key instanceof Keyword) {
                map.put(((Keyword) key).id, value);
            } else {
                throw new ParseError(this, "record initializer key is not a keyword: " + key);
            }
        }

        this.open = open;
        this.close = close;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Node> e : map.entrySet()) {
            if (!first) {
                sb.append(" ");
            }
            sb.append(":" + e.getKey() + " " + e.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
