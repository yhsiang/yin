package org.yinwang.yin.parser;

import org.yinwang.yin.Constants;
import org.yinwang.yin._;
import org.yinwang.yin.ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static Node parse(String file) throws ParseError {
        PreParser p = new PreParser(file);
        Node prenode = p.parse();
        return parseNode(prenode);
    }


    public static Node parseNode(Node prenode) throws ParseError {
        // initial program is in a block
        if (prenode instanceof Block) {
            List<Node> parsed = parseList(((Block) prenode).statements);
            return new Block(parsed, prenode.file, prenode.start, prenode.end, prenode.line, prenode.col);
        }

        // most structures are encoded in a tuple
        // (if t c a) (+ 1 2) (f x y) ...
        // decode them by their first map
        if (prenode instanceof Tuple) {
            Tuple tuple = ((Tuple) prenode);

            if (tuple.elements.isEmpty()) {
                throw new ParseError(tuple, "syntax error");
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

                if (keyword.equals(Constants.FUN_KEYWORD)) {
                    if (tuple.elements.size() >= 3) {
                        Node preParams = tuple.elements.get(1);
                        if (preParams instanceof Tuple) {
                            List<Node> parsedElems = parseList(((Tuple) preParams).elements);
                            Parameter parameter = new Parameter(parsedElems);
                            List<Node> statements = parseList(tuple.elements.subList(2, tuple.elements.size()));
                            int start = statements.get(0).start;
                            int end = statements.get(statements.size() - 1).end;
                            Node body = new Block(statements, prenode.file, start, end, prenode.line, prenode.col);
                            return new Fun(parameter, body, prenode.file, prenode.start, prenode.end, prenode.line,
                                    prenode.col);
                        } else {
                            throw new ParseError(preParams, "incorrect format of parameters");
                        }
                    } else {
                        throw new ParseError(tuple, "syntax error in function definition");
                    }
                }

                if (keyword.equals(Constants.DEF_KEYWORD)) {
                    if (tuple.elements.size() == 3) {
                        Node pattern = parseNode(tuple.elements.get(1));
                        Node value = parseNode(tuple.elements.get(2));
                        return new Def(pattern, value, prenode.file, prenode.start, prenode.end, prenode.line,
                                prenode.col);
                    } else {
                        throw new ParseError(tuple, "incorrect format of definition");
                    }
                }

                if (keyword.equals(Constants.ASSIGN_KEYWORD)) {
                    if (tuple.elements.size() == 3) {
                        Node pattern = parseNode(tuple.elements.get(1));
                        Node value = parseNode(tuple.elements.get(2));
                        return new Assign(pattern, value, prenode.file, prenode.start, prenode.end, prenode.line,
                                prenode.col);
                    } else {
                        throw new ParseError(tuple, "incorrect format of definition");
                    }
                }
            }
            // application
            Node func = parseNode(tuple.elements.get(0));
            List<Node> parsedArgs = parseList(tuple.elements.subList(1, tuple.elements.size()));
            Parameter args = new Parameter(parsedArgs);
            return new Call(func, args, prenode.file, prenode.start, prenode.end, prenode.line, prenode.col);
        }

        // defaut return the node untouched
        return prenode;
    }


    public static List<Node> parseList(List<Node> prenodes) throws ParseError {
        List<Node> parsed = new ArrayList<>();
        for (Node s : prenodes) {
            parsed.add(parseNode(s));
        }
        return parsed;
    }


    public static void main(String[] args) throws ParseError {
        Node tree = Parser.parse(args[0]);
        _.msg(tree.toString());
    }

}
