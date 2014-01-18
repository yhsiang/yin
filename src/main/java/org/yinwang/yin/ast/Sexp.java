package org.yinwang.yin.ast;


/**
 * Sexy expression (S-expression)
 */
abstract class Sexp {
    public String file;
    public int start;
    public int end;


    protected Sexp(String file, int start, int end) {
        this.file = file;
        this.start = start;
        this.end = end;
    }
}
