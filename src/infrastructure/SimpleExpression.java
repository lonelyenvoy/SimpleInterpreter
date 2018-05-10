package infrastructure;

import exception.ReferenceError;
import exception.RuntimeInternalError;
import type.*;
import util.PrettyPrintUtils;

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

    /**
     * Last updated in version: v-0.0.10 alpha
     */
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

    /**
     * Last updated in version: v-0.0.10 alpha
     */
    public void doSyntaxAnalysis(int depth) {
        if (children.size() == 0) {
            try {
                Long.parseLong(value);
                PrettyPrintUtils.println("Number: " + value, depth);
            } catch (NumberFormatException ex) {
                if (value.equals("false") || value.equals("true")) {
                    PrettyPrintUtils.println("Boolean: " + value, depth);
                } else {
                    PrettyPrintUtils.println("Id: " + value, depth);
                }
            }
        } else {
            SimpleExpression first = children.get(0);
            if (first.value.equals("if")) {
                PrettyPrintUtils.println("Keyword: " + first, depth);
                PrettyPrintUtils.println("Condition: ", depth);
                children.get(1).doSyntaxAnalysis(depth + 1);
                PrettyPrintUtils.println("If-True: ", depth);
                children.get(2).doSyntaxAnalysis(depth + 1);
                PrettyPrintUtils.println("If-False: ", depth);
                children.get(3).doSyntaxAnalysis(depth + 1);
            } else if (first.value.equals("define")) {
                PrettyPrintUtils.println("Keyword: " + first, depth);
                PrettyPrintUtils.println("Id: " + children.get(1), depth);
                PrettyPrintUtils.println("Value: ", depth);
                children.get(2).doSyntaxAnalysis(depth + 1);
            } else if (first.value.equals("do")) {
                PrettyPrintUtils.println("Keyword: " + first, depth);
                for (SimpleExpression expr : children.stream().skip(1).collect(Collectors.toList())) {
                    expr.doSyntaxAnalysis(depth + 1);
                }
            } else if (first.value.equals("function")) {
                PrettyPrintUtils.println("Keyword: " + first, depth);
                PrettyPrintUtils.print("Arguments: ", depth);
                for (String arg : children.get(1).children.stream().map(expr -> expr.value).collect(Collectors.toList())) {
                    PrettyPrintUtils.print(arg + " ", 0);
                }
                PrettyPrintUtils.println("", depth);
                PrettyPrintUtils.println("Body: ", depth);
                children.get(2).doSyntaxAnalysis(depth + 1);
            } else if (first.value.equals("list")) {
                PrettyPrintUtils.println("Keyword: " + first, depth);
                children.stream().skip(1).forEach(expr -> expr.doSyntaxAnalysis(depth + 1));
            } else if (SimpleScope.getBuiltinFunctions().containsKey(first.value)) {
                PrettyPrintUtils.println("Function: " + first, depth);
                PrettyPrintUtils.println("Arguments: ", depth);
                children.stream().skip(1).forEach(expr -> expr.doSyntaxAnalysis(depth + 1));
            } else {
                PrettyPrintUtils.println("Function: " + first, depth);
                PrettyPrintUtils.println("Arguments: ", depth);
                children.stream().skip(1).forEach(expr -> expr.doSyntaxAnalysis(depth + 1));
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
