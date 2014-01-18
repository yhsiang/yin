package org.yinwang.yin.parser;


class Token extends Sexp {


    static enum TokenType {
        DELIMITER,
        STRING,
        NUMBER,
        IDENT
    }


    public String content;
    public TokenType type;


    public Token(TokenType type, String content, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.type = type;
        this.content = content;
    }


    public String getFileLineCol() {
        return file + ":" + line + ":" + col;
    }


    public String toString() {
        if (type == TokenType.STRING) {
            return "\"" + content + "\""; // + ":" + type;
        } else {
            return content; // + ":" + type;
        }
    }

}
