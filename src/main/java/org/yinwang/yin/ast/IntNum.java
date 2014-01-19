package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin.value.IntValue;
import org.yinwang.yin.value.Value;

public class IntNum extends Node {

    public String content;
    public int value;


    public IntNum(String content, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.content = content;
        this.value = Integer.parseInt(content);
    }


    public static IntNum parse(String content, String file, int start, int end, int line, int col) {
        try {
            return new IntNum(content, file, start, end, line, col);
        } catch (NumberFormatException e) {
            return null;
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
