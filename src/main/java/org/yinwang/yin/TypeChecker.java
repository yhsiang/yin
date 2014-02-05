package org.yinwang.yin;


import org.yinwang.yin.ast.Node;
import org.yinwang.yin.parser.Parser;
import org.yinwang.yin.value.Value;

public class TypeChecker {

    String file;


    public TypeChecker(String file) {
        this.file = file;
    }


    public Value interp(String file) {
        Node program = Parser.parse(file);
        return program.typecheck(Scope.buildInitScope());
    }


    public static void main(String[] args) {
        TypeChecker i = new TypeChecker(args[0]);
        Value result = i.interp(args[0]);
        _.msg(result.toString());
    }

}
