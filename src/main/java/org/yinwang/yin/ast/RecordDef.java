package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
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
                     String file, int start, int end, int line, int col) throws ParseError
    {
        super(file, start, end, line, col);
        this.name = name;
        this.parents = parents;

        if (contents.size() % 2 != 0) {
            throw new ParseError(this, "record initializer must have even number of elements");
        }

        for (int i = 0; i < contents.size(); i += 2) {
            Node key = contents.get(i);
            Node value = contents.get(i + 1);
            if (key instanceof Keyword) {
                if (value instanceof Keyword) {
                    throw new ParseError(value, "keywords shouldn't be used as values: " + value);
                } else {
                    map.put(((Keyword) key).id, value);
                }
            } else {
                throw new ParseError(key, "record initializer key is not a keyword: " + key);
            }
        }
    }


    public Value interp(Scope s) {
        return null;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.RECORD_KEYWORD).append(" ");
        sb.append(name).append(" ");

        if (parents != null) {
            sb.append("(" + Node.printList(parents) + ")");
        }

        for (Map.Entry<String, Node> e : map.entrySet()) {
            sb.append(" :" + e.getKey() + " " + e.getValue());
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }
}
