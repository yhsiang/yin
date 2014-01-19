package org.yinwang.yin.ast;


import org.yinwang.yin.GeneralError;
import org.yinwang.yin.Scope;
import org.yinwang.yin.value.IntValue;
import org.yinwang.yin.value.Value;

public class IntNum extends Node {

    public String content;
    public int value;


    public IntNum(String content, String file, int start, int end, int line, int col) throws GeneralError {
        super(file, start, end, line, col);
        this.content = content;
        try {
            this.value = Integer.parseInt(content);
        } catch (NumberFormatException e) {
            throw new GeneralError(file + ":" + line + ":" + col + ": illegal number format: " + content);
        }
    }


    public Value interp(Scope s) {
        return new IntValue(value);
    }


    @Override
    public String toString() {
        return Integer.toString(value);
    }

}
