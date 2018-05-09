package infrastructure;

import exception.ReferenceError;
import exception.RuntimeInternalError;
import type.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleExpression {
    private String value;
    private List<SimpleExpression> children;
    private SimpleExpression parent;

    public String getValue() {
        return value;
    }
    public List<SimpleExpression> getChildren() {
        return children;
    }
    public SimpleExpression getParent() {
        return parent;
    }

    public SimpleExpression(String value, SimpleExpression parent) {
        this.value = value;
        this.children = new ArrayList<>();
        this.parent = parent;
    }

    public SimpleObject evaluate(SimpleScope scope) {
        SimpleExpression current = this;
        while (true) {
            if (current.children.size() == 0) {
                try {
                    return new SimpleNumber(Long.parseLong(current.value));
                } catch (NumberFormatException ex) {
                    if (current.value.equals("false")) {
                        return SimpleBoolean.False;
                    } else if (current.value.equals("true")) {
                        return SimpleBoolean.True;
                    } else {
                        return scope.find(current.value);
                    }
                }
            } else {
                SimpleExpression first = current.children.get(0);
                if (first.value.equals("if")) {
                    // TODO check children
                    SimpleBoolean condition = SimpleBoolean.valueOf(current.children.get(1).evaluate(scope));
                    return condition == SimpleBoolean.True ? current.children.get(2).evaluate(scope) : current.children.get(3).evaluate(scope);
                } else if (first.value.equals("define")) {
                    return scope.define(current.children.get(1).value, current.children.get(2).evaluate(new SimpleScope(scope)));
                } else if (first.value.equals("do")) {
                    SimpleObject result = null;
                    for (SimpleExpression expr : current.children.stream().skip(1).collect(Collectors.toList())) {
                        result = expr.evaluate(scope);
                    }
                    return result;
                } else if (first.value.equals("function")) {
                    SimpleExpression body = current.children.get(2);
                    List<String> params = current.children.get(1).children.stream().map(expr -> expr.value).collect(Collectors.toList());
                    return new SimpleFunction(body, params.toArray(new String[0]), new SimpleScope(scope));
                } else if (first.value.equals("list")) {
                    SimpleScope finalScope = scope;
                    return new SimpleList(current.children.stream().skip(1).map(expr -> expr.evaluate(finalScope)).collect(Collectors.toList()));
                } else if (SimpleScope.getBuiltinFunctions().containsKey(first.value)) {
                    List<SimpleExpression> args = current.children.stream().skip(1).collect(Collectors.toList());
                    return SimpleScope.getBuiltinFunctions().get(first.value).apply(args.toArray(new SimpleExpression[0]), scope);
                } else {
                    SimpleFunction function = first.value.equals("(") ? (SimpleFunction) first.evaluate(scope) : (SimpleFunction) scope.find(first.value);
                    SimpleScope finalScope = scope;
                    List<SimpleObject> arguments = current.children.stream().skip(1).map(expr -> expr.evaluate(finalScope)).collect(Collectors.toList());
                    SimpleFunction newFunction = function.update(arguments.toArray(new SimpleObject[0]));
                    if (newFunction.isPartial()) {
                        return newFunction.evaluate();
                    } else {
                        current = newFunction.getBody();
                        scope = newFunction.getScope();
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (value.equals("(")) {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            boolean first = true;
            for (SimpleExpression expr : children) {
                if (first) first = false;
                else builder.append(" ");
                if (expr.value.equals("(")) {
                    builder.append(expr.toString());
                } else {
                    builder.append(expr.value);
                }
            }
            builder.append(")");
            return builder.toString();
        } else {
            return value;
        }
    }
}
