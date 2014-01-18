package org.yinwang.yin.ast;

import java.util.ArrayList;
import java.util.List;


public class Block extends Node {
    public List<Node> statements = new ArrayList<>();


    public Block(List<Node> statements, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.statements = statements;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(seq");

        for (int i = 0; i < statements.size(); i++) {
            sb.append(statements.get(i).toString());
            if (i != statements.size() - 1) {
                sb.append("\n");
            }
        }

        sb.append(")");
        return sb.toString();
    }
}
