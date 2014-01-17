package org.yinwang.yin.ast;

/**
 * Created by yinwang on 1/17/14.
 */
class Token extends Sexp {
    public TokenType type;
    public String content;


    public Token(TokenType type, String content, String file, int start, int end) {
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


    static enum TokenType {
        OPENPAREN,
        CLOSEPAREN,
        STRING,
        NUMBER,
        IDENT
    }
}
