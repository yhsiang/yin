package org.yinwang.yin.parser;


public class FloatNum extends Sexp {

    public String content;
    public double value;


    public FloatNum(String content, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.content = content;
        this.value = Double.parseDouble(content);
    }


    public static FloatNum parse(String content, String file, int start, int end, int line, int col) {
        try {
            return new FloatNum(content, file, start, end, line, col);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    @Override
    public String toString() {
        return Double.toString(value);
    }

}
