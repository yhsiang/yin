package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin.value.*;

public class IntNum extends Node {

    public String content;
    public int value;


    public IntNum(String content, String file, int start, int end, int line, int col) throws ParseError {
        super(file, start, end, line, col);
        this.content = content;
        try {
            this.value = Integer.parseInt(content);
        } catch (NumberFormatException e) {
            throw new ParseError(file + ":" + line + ":" + col + ": illegal number format: " + content);
        }
    }


    public YinValue interp(Scope s) {
        return null;
    }


    @Override
    public String toString() {
        return Integer.toString(value);
    }

}
