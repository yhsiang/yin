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
        String valueContent;

        if (content.startsWith("+")) {
            sign = 1;
            valueContent = content.substring(1);
        } else if (content.startsWith("-")) {
            sign = -1;
            valueContent = content.substring(1);
        } else {
            sign = 1;
            valueContent = content;
        }

        if (valueContent.startsWith("0b")) {
            base = 2;
            valueContent = valueContent.substring(2);
        } else if (valueContent.startsWith("0x")) {
            base = 16;
            valueContent = valueContent.substring(2);
        } else if (content.startsWith("0") && content.length() >= 2) {
            base = 8;
            valueContent = valueContent.substring(1);
        } else {
            base = 10;
        }

        this.value = parseValue(valueContent, base, sign);
    }


    public static String prefix(int base) {
        if (base == 2) {
            return "0b";
        } else if (base == 8) {
            return "0";
        } else if (base == 16) {
            return "0x";
        } else {
            return "";
        }
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
