package org.yinwang.yin;


import org.yinwang.yin.ast.Declare;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.parser.Parser;
import org.yinwang.yin.value.FunType;
import org.yinwang.yin.value.Value;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker {

    public static TypeChecker self;
    public String file;
    public List<FunType> uncalled = new ArrayList<>();


    public TypeChecker(String file) {
        this.file = file;
    }


    public Value typecheck(String file) {
        Node program = Parser.parse(file);
        Scope s = Scope.buildInitTypeScope();
        Value ret = program.typecheck(s);
        for (FunType ft : uncalled) {
            invokeUncalled(ft, s);
        }
        return ret;
    }


    public void invokeUncalled(FunType fun, Scope s) {
        Scope funScope = new Scope(fun.env);
        if (fun.properties != null) {
            Declare.mergeTypeProperties(fun.properties, funScope);
        }
        fun.fun.body.typecheck(funScope);
    }


    public static void main(String[] args) {
        TypeChecker tc = new TypeChecker(args[0]);
        TypeChecker.self = tc;
        Value result = tc.typecheck(args[0]);
        _.msg(result.toString());
    }

}
