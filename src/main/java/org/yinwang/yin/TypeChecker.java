package org.yinwang.yin;


import org.yinwang.yin.ast.Declare;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.parser.Parser;
import org.yinwang.yin.value.FunType;
import org.yinwang.yin.value.Type;
import org.yinwang.yin.value.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeChecker {

    public static TypeChecker self;
    public String file;
    public List<FunType> uncalled = new ArrayList<>();
    public Set<FunType> callStack = new HashSet<>();


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
        Value actual = fun.fun.body.typecheck(funScope);
        Object retNode = fun.properties.lookupPropertyLocal(Constants.RETURN_ARROW, "type");


        if (retNode == null || !(retNode instanceof Node)) {
            _.abort("illegal return type: " + retNode);
        }

        Value retType = ((Node) retNode).typecheck(s);

        if (!Type.subtype(actual, retType)) {
            _.abort(fun.fun, "type error in return value, expected: " + retType + ", actual: " + actual);
        }
    }


    public static void main(String[] args) {
        TypeChecker tc = new TypeChecker(args[0]);
        TypeChecker.self = tc;
        Value result = tc.typecheck(args[0]);
        _.msg(result.toString());
    }

}
