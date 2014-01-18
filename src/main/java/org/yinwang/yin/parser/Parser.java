package org.yinwang.yin.parser;

import org.yinwang.yin._;
import org.yinwang.yin.ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static Node parse(String file) {
        PreParser p = new PreParser(file);
        Node prenode = p.parse();
        return parseNode(prenode);
    }


    public static Node parseNode(Node prenode) {
        if (prenode instanceof Tuple) {
            Tuple tuple = ((Tuple) prenode);
            if (tuple.elements.isEmpty()) {
                _.abort(tuple.getFileLineCol() + ": syntax error");
                return null;
            } else {
                Node keyNode = tuple.elements.get(0);
                if (keyNode instanceof Name) {
                    String keyword = ((Name) keyNode).id;
                    if (keyword.equals("if")) {
                        if (tuple.elements.size() == 4) {
                            Node test = parseNode(tuple.elements.get(1));
                            Node conseq = parseNode(tuple.elements.get(2));
                            Node alter = parseNode(tuple.elements.get(3));
                            return new If(test, conseq, alter, prenode.file, prenode.start, prenode.end, prenode.line,
                                    prenode.col);
                        }
                    }
                    return null;   // other cases
                } else {   // application
                    return null;
                }
            }
        } else if (prenode instanceof Block) {
            List<Node> parsed = new ArrayList<>();
            for (Node s : ((Block) prenode).statements) {
                parsed.add(parseNode(s));
            }
            return new Block(parsed, prenode.file, prenode.start, prenode.end, prenode.line, prenode.col);
        } else {
            return prenode;
        }
    }


    public static void main(String[] args) {
        Node tree = Parser.parse(args[0]);
        _.msg("tree: " + tree);
    }

}
