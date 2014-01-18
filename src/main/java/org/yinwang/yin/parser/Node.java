package org.yinwang.yin.parser;


/**
 * Sexy expression (S-expression)
 */
abstract class Node {
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

}
