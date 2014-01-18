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
        // initial program is in a block
        if (prenode instanceof Block) {
            List<Node> parsed = parseList(((Block) prenode).statements);
            return new Block(parsed, prenode.file, prenode.start, prenode.end, prenode.line, prenode.col);
        }

        // most structures are encoded in a tuple
        // (if t c a) (+ 1 2) (f x y) ...
        // decode them by their first elements
        if (prenode instanceof Tuple) {
            Tuple tuple = ((Tuple) prenode);

            if (tuple.elements.isEmpty()) {
                _.abort(tuple.getFileLineCol() + ": syntax error");
                return null;
            }

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

                if (keyword.equals("fun")) {
                    if (tuple.elements.size() >= 3) {
                        Node params = parseNode(tuple.elements.get(1));
                        List<Node> statements = parseList(tuple.elements.subList(2, tuple.elements.size()));
                        int start = statements.get(0).start;
                        int end = statements.get(statements.size() - 1).end;
                        Node body = new Block(statements, prenode.file, start, end, prenode.line, prenode.col);
                        return new Fun(params, body, prenode.file, prenode.start, prenode.end, prenode.line,
                                prenode.col);
                    }
                }
            }
            // application
            Node func = parseNode(tuple.elements.get(0));
            Node args = parseNode(tuple.elements.get(1));
            return new Call(func, args, prenode.file, prenode.start, prenode.end, prenode.line, prenode.col);
        }

        // defaut return the node untouched
        return prenode;
    }


    public static List<Node> parseList(List<Node> prenodes) {
        List<Node> parsed = new ArrayList<>();
        for (Node s : prenodes) {
            parsed.add(parseNode(s));
        }
        return parsed;
    }


    public static void main(String[] args) {
        Node tree = Parser.parse(args[0]);
        _.msg("tree: " + tree);
    }

}
