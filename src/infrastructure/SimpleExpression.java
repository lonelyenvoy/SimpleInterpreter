package infrastructure;

import exception.ReferenceError;
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
        if (this.children.size() == 0) {
            try {
                return new SimpleNumber(Long.parseLong(value));
            } catch (NumberFormatException ex) {
                try {
                    return scope.find(value);
                } catch (ReferenceError referenceError) {
                    referenceError.printStackTrace();
                }
            }
        } else {
            SimpleExpression first = children.get(0);
            if (first.value.equals("if")) {
                // TODO check children
                SimpleBoolean condition = SimpleBoolean.valueOf(children.get(1).evaluate(scope));
                return condition == SimpleBoolean.True ? children.get(2).evaluate(scope) : children.get(3).evaluate(scope);
            } else if (first.value.equals("define")) {
                return scope.define(children.get(1).value, children.get(2).evaluate(new SimpleScope(scope)));
            } else if (first.value.equals("begin")) {
                SimpleObject result = null;
                for (SimpleExpression expr : children.stream().skip(1).collect(Collectors.toList())) {
                    result = expr.evaluate(scope);
                }
                return result;
            } else if (first.value.equals("function")) {
                SimpleExpression body = children.get(2);
                List<String> params = children.get(1).children.stream().map(expr -> expr.value).collect(Collectors.toList());
                SimpleFunction func = new SimpleFunction(body, params.toArray(new String[0]), new SimpleScope(scope));
                return func;
            } else if (first.value.equals("list")) {
                return new SimpleList(children.stream().skip(1).map(expr -> expr.evaluate(scope)).collect(Collectors.toList()));
            } else if (SimpleScope.getBuiltinFunctions().containsKey(first.value)) {
                List<SimpleExpression> args = children.stream().skip(1).collect(Collectors.toList());
                return SimpleScope.getBuiltinFunctions().get(first.value).apply(args.toArray(new SimpleExpression[0]), scope);
            } else {
                SimpleFunction function = null;
                try {
                    function = first.value.equals("(") ? (SimpleFunction) first.evaluate(scope) : (SimpleFunction) scope.find(first.value);
                } catch (ReferenceError referenceError) {
                    referenceError.printStackTrace();
                }
                List<SimpleObject> arguments = children.stream().skip(1).map(expr -> expr.evaluate(scope)).collect(Collectors.toList());
                return function.update(arguments.toArray(new SimpleObject[0])).evaluate();
            }
        }
        throw new Error("INVALID EVALUATION");
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
