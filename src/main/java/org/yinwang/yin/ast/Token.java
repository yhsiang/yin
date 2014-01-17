package org.yinwang.yin.ast;


class Token extends Sexp {


    static enum TokenType {
        DELIMITER,
        STRING,
        NUMBER,
        IDENT
    }


    public String content;
    public TokenType type;


    public Token(TokenType type,
                 String content,
                 String file,
                 int start,
                 int end)
    {
        super(file, start, end);
        this.type = type;
        this.content = content;
    }


    public String toString() {
        if (type == TokenType.STRING) {
            return "\"" + content + "\"";
        } else {
            return content;
        }
    }

}
