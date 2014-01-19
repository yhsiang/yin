package org.yinwang.yin.ast;


public class ParseError extends Exception {
    public String msg;
    public Node location;


    public ParseError(Node location, String msg) {
        this.msg = msg;
        this.location = location;
    }

    public String toString() {
        return location.getFileLineCol() + ": " + msg;
    }

}
