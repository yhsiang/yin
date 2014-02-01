package org.yinwang.yin.ast;


import org.yinwang.yin.Binder;
import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.parser.Parser;
import org.yinwang.yin.value.*;

import java.util.*;

public class Call extends Node {
    public Node func;
    public Argument args;


    public Call(Node func, Argument args, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.func = func;
        this.args = args;
    }


    public Value interp(Scope s) {
        if (this.func instanceof Name && ((Name) this.func).id.equals(Constants.DECLARE_KEYWORD)) {
            // declare
            List<Node> fields = args.positional;

            Scope properties = new Scope();
            for (Node field : fields) {
                if (field instanceof VectorLiteral) {
                    List<Node> elements = ((VectorLiteral) field).elements;
                    if (elements.isEmpty()) {
                        _.abort(field, "empty record slot not allowed");
                    }

                    Node nameNode = elements.get(0);
                    if (!(nameNode instanceof Name)) {
                        _.abort(nameNode, "expect field name, but got: " + nameNode);
                    }

                    Map<String, Node> props = Parser.parseMap(elements.subList(1, elements.size()));
                    Map<String, Object> propsObj = new LinkedHashMap<>();
                    for (Map.Entry<String, Node> e : props.entrySet()) {
                        propsObj.put(e.getKey(), e.getValue().interp(s));
                    }
                    properties.putProperties(((Name) nameNode).id, propsObj);
                }
            }

            s.putAll(properties);

            for (String key : properties.keySet()) {
                Object defaultValue = properties.lookupPropertyLocal(key, "default");
                if (defaultValue == null) {
                    continue;
                } else if (defaultValue instanceof Value) {
                    Value existing = s.lookup(key);
                    if (existing == null) {
                        s.putValue(key, (Value) defaultValue);
                    }
                } else {
                    _.abort("default value is not value, shouldn't happen");
                }
            }
            return Value.VOID;
        } else {
            Value fun = this.func.interp(s);
            if (fun instanceof Closure) {
                Closure closure = (Closure) fun;
                Scope funScope = new Scope(closure.env);
                List<Name> params = closure.fun.params;

                if (!args.positional.isEmpty() && args.keywords.isEmpty()) {
                    // positional
                    if (args.positional.size() != params.size()) {
                        _.abort(this.func,
                                "calling function with wrong number of arguments: " + args.positional.size());
                    }

                    for (int i = 0; i < args.positional.size(); i++) {
                        Value value = args.positional.get(i).interp(s);
                        Binder.define(params.get(i), value, funScope);
                    }
                    return closure.fun.body.interp(funScope);
                } else {
                    // keywords
                    Set<String> seen = new HashSet<>();

                    // try to bind all arguments
                    for (Name param : params) {
                        Node actual = args.keywords.get(param.id);
                        if (actual != null) {
                            seen.add(param.id);
                            Value value = actual.interp(funScope);
                            funScope.putValue(param.id, value);
                        }
//                    else {
//                        _.abort(param, "argument not supplied for: " + param);
//                        return Value.VOID;
//                    }
                    }

                    // detect extra arguments
                    List<String> extra = new ArrayList<>();
                    for (String id : args.keywords.keySet()) {
                        if (!seen.contains(id)) {
                            extra.add(id);
                        }
                    }

                    if (!extra.isEmpty()) {
                        _.abort(this, "extra keyword arguments: " + extra);
                        return Value.VOID;
                    } else {
                        return closure.fun.body.interp(funScope);
                    }
                }
            } else if (fun instanceof RecordType) {
                RecordType template = (RecordType) fun;
                Map<String, Value> values = new LinkedHashMap<>();

                for (String key : template.properties.keySet()) {
                    Object defaultValue = template.properties.lookupPropertyLocal(key, "default");
                    if (defaultValue == null) {
                        continue;
                    } else if (defaultValue instanceof Value) {
                        values.put(key, (Value) defaultValue);
                    } else {
                        _.abort("default value is not value, shouldn't happen");
                    }
                }

                for (Map.Entry<String, Node> e : args.keywords.entrySet()) {
                    if (!template.properties.keySet().contains(e.getKey())) {
                        _.abort(this, "extra keyword argument: " + e.getKey());
                    } else {
                        values.put(e.getKey(), e.getValue().interp(s));
                    }
                }

                for (String field : template.properties.keySet()) {
                    if (!values.containsKey(field)) {
                        _.abort(this, "field is not initialized: " + field);
                    }
                }

                // instantiate
                return new RecordValue(template.name, template, values);
            } else if (fun instanceof PrimFun) {
                PrimFun prim = (PrimFun) fun;
                if (args.positional.size() != prim.arity) {
                    _.abort(this, "incorrect number of arguments for primitive " +
                            prim.name + ", expecting " + prim.arity + ", but got " + args.positional.size());
                    return null;
                } else {
                    List<Value> args = Node.interpList(this.args.positional, s);
                    return prim.apply(args, this);
                }
            } else {
                _.abort(this.func, "calling non-function: " + fun);
                return Value.VOID;
            }
        }
    }


    public String toString() {
        if (args.positional.size() != 0) {
            return "(" + func + " " + args + ")";
        } else {
            return "(" + func + ")";
        }
    }

}
