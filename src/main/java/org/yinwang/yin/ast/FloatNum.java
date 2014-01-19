package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin.value.*;

public class FloatNum extends Node {

    public String content;
    public double value;


    public FloatNum(String content, String file, int start, int end, int line, int col) throws ParseError {
        super(file, start, end, line, col);
        this.content = content;
        try {
            this.value = Double.parseDouble(content);
        } catch (NumberFormatException e) {
            throw new ParseError(file + ":" + line + ":" + col + ": illegal number format: " + content);
        }
    }


    public YinValue interp(Scope s) {
        return null;
    }


    @Override
    public String toString() {
        return Double.toString(value);
    }

}
