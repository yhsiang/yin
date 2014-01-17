package org.yinwang.yin.ast;

/**
 * Created by yinwang on 1/17/14.
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
