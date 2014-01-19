package org.yinwang.yin.ast;


public class ParseError extends Exception {
    public String msg;
    public Node location;


    public ParseError(Node location, String msg) {
        this.msg = msg;
        this.location = location;
    }


    public ParseError(String msg) {
        this.msg = msg;
    }


    public String toString() {
        if (location != null) {
            return location.getFileLineCol() + ": " + msg;
        } else {
            return msg;
        }
    }

}
