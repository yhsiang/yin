package org.yinwang.yin.parser;


import java.math.BigInteger;

public class IntNum extends Token {

    public String content;
    public BigInteger value;
    public int base;


    public IntNum(String content, String file, int start, int end, int line, int col) {
        super(TokenType.NUMBER, content, file, start, end, line, col);
        this.content = content;

        int sign;
        if (content.startsWith("+")) {
            sign = 1;
            content = content.substring(1);
        } else if (content.startsWith("-")) {
            sign = -1;
            content = content.substring(1);
        } else {
            sign = 1;
        }

        if (content.endsWith("b") || content.endsWith("B")) {
            base = 2;
            content = content.substring(0, content.length() - 1);
        } else if (content.endsWith("h") || content.endsWith("H")) {
            base = 16;
            content = content.substring(0, content.length() - 1);
        } else if (content.endsWith("o") || content.endsWith("O")) {
            base = 8;
            content = content.substring(0, content.length() - 1);
        } else {
            base = 10;
        }

        this.value = parseValue(content, base, sign);
    }


    public static BigInteger parseValue(String s, int base, int sign) {
        BigInteger value = new BigInteger(s, base);
        if (sign == -1) {
            value = value.negate();
        }

        return value;
    }


    public static IntNum parse(String content, String file, int start, int end, int line, int col) {
        try {
            return new IntNum(content, file, start, end, line, col);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public String toString() {
        return content + "=" + value;
    }

}
