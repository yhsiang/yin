package org.yinwang.yin.ast;


import java.util.List;

public abstract class Node {
    public String file;
    public int start;
    public int end;
    public int line;
    public int col;


    protected Node(String file, int start, int end, int line, int col) {
        this.file = file;
        this.start = start;
        this.end = end;
        this.line = line;
        this.col = col;
    }


    public String getFileLineCol() {
        return file + ":" + line + ":" + col;
    }


    public static String printList(List<? extends Node> nodes) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Node e : nodes) {
            if (!first) {
                sb.append(" ");
            }
            sb.append(e);
            first = false;
        }
        return sb.toString();
    }

}
