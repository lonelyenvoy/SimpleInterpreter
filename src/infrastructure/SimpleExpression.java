package infrastructure;

import type.*;
import util.PrettyPrintUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static util.PrettyPrintUtils.println;

public class SimpleExpression {
    private final String value;
    private final List<SimpleExpression> children;
    private final SimpleExpression parent;

    public String getValue() {
        return value;
    }
    public List<SimpleExpression> getChildren() {
        return children;
    }
    public SimpleExpression getParent() {
        return parent;
    }

    private SimpleExpression(String value, SimpleExpression parent) {
        this.value = value;
        this.children = new ArrayList<>();
        this.parent = parent;
    }

    public static SimpleExpression of(String value, SimpleExpression parent) {
        return new SimpleExpression(value, parent);
    }

    public static SimpleExpression ofRoot() {
        return new SimpleExpression("", null);
    }

    /**
     * Last updated in version: v-0.0.13 alpha
     */
    public SimpleObject evaluate(SimpleScope scope) {
        SimpleExpression current = this;
        while (true) {
            if (current.children.size() == 0) { // can be an id, a SimpleNumber or a SimpleBoolean
                try { // parse as a SimpleNumber
                    return SimpleNumber.of(Long.parseLong(current.value));
                } catch (NumberFormatException ex) { // parse as a SimpleBoolean
                    if (current.value.equals(SimpleKeyword.FALSE.getLiteral())) {
                        return SimpleBoolean.False;
                    } else if (current.value.equals(SimpleKeyword.TRUE.getLiteral())) {
                        return SimpleBoolean.True;
                    } else {
                        return scope.find(current.value); // parse as an id
                    }
                }
            } else { // can be a SimpleFunction or a SimpleList
                SimpleExpression first = current.children.get(0);
                if (first.value.equals(SimpleKeyword.IF.getLiteral())) { // keyword if
                    // TODO check children
                    SimpleBoolean condition = SimpleBoolean.valueOf(current.children.get(1).evaluate(scope));
                    return condition == SimpleBoolean.True
                            ? current.children.get(2).evaluate(scope)
                            : current.children.get(3).evaluate(scope);
                } else if (first.value.equals(SimpleKeyword.DEFINE.getLiteral())) { // keyword define
                    return scope.define(current.children.get(1).value,
                            current.children.get(2).evaluate(SimpleScope.of(scope)));
                } else if (first.value.equals(SimpleKeyword.DO.getLiteral())) { // keyword do
                    SimpleObject result = null;
                    for (SimpleExpression expr : current.children.stream().skip(1).collect(Collectors.toList())) {
                        result = expr.evaluate(scope);
                    }
                    return result;
                } else if (first.value.equals(SimpleKeyword.FUNCTION.getLiteral())) { // SimpleFunction
                    SimpleExpression body = current.children.get(2);
                    String[] params = current.children.get(1).children.stream()
                            .map(expr -> expr.value).toArray(String[]::new);
                    return SimpleFunction.of(body, params, SimpleScope.of(scope));
                } else if (first.value.equals(SimpleKeyword.LIST.getLiteral())) { // SimpleList
                    SimpleScope finalScope = scope;
                    return SimpleList.of(
                            current.children.stream()
                                    .skip(1).map(expr -> expr.evaluate(finalScope))
                                    .collect(Collectors.toList()));
                } else if (SimpleScope.getBuiltinFunctions().containsKey(first.value)) { // built-in functions
                    SimpleExpression[] args = current.children.stream().skip(1).toArray(SimpleExpression[]::new);
                    return SimpleScope.getBuiltinFunctions().get(first.value).apply(args, scope);
                } else { // custom functions
                    SimpleFunction function = first.value.equals("(")
                            ? (SimpleFunction) first.evaluate(scope)
                            : (SimpleFunction) scope.find(first.value);
                    SimpleScope finalScope = scope;
                    SimpleObject[] arguments = current.children.stream()
                            .skip(1).map(expr -> expr.evaluate(finalScope)).toArray(SimpleObject[]::new);
                    SimpleFunction newFunction = function.update(arguments);
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
     * Last updated in version: v-0.0.12 alpha
     */
    public void doSyntaxAnalysis(int depth) {
        if (children.size() == 0) {
            try {
                Long.parseLong(value);
                println("Number: " + value, depth);
            } catch (NumberFormatException ex) {
                if (value.equals("false") || value.equals("true")) {
                    println("Boolean: " + value, depth);
                } else {
                    println("Id: " + value, depth);
                }
            }
        } else {
            SimpleExpression first = children.get(0);
            if (first.value.equals("if")) {
                println("Keyword: " + first, depth);
                println("Condition: ", depth);
                children.get(1).doSyntaxAnalysis(depth + 1);
                println("If-True: ", depth);
                children.get(2).doSyntaxAnalysis(depth + 1);
                println("If-False: ", depth);
                children.get(3).doSyntaxAnalysis(depth + 1);
            } else if (first.value.equals("define")) {
                println("Keyword: " + first, depth);
                println("Id: " + children.get(1), depth);
                println("Value: ", depth);
                children.get(2).doSyntaxAnalysis(depth + 1);
            } else if (first.value.equals("do")) {
                println("Keyword: " + first, depth);
                for (SimpleExpression expr : children.stream().skip(1).collect(Collectors.toList())) {
                    expr.doSyntaxAnalysis(depth + 1);
                }
            } else if (first.value.equals("function")) {
                println("Keyword: " + first, depth);
                PrettyPrintUtils.print("Arguments: ", depth);
                for (String arg : children.get(1).children.stream().map(expr -> expr.value).collect(Collectors.toList())) {
                    PrettyPrintUtils.print(arg + " ", 0);
                }
                println("", depth);
                println("Body: ", depth);
                children.get(2).doSyntaxAnalysis(depth + 1);
            } else if (first.value.equals("list")) {
                println("Keyword: " + first, depth);
                children.stream().skip(1).forEach(expr -> expr.doSyntaxAnalysis(depth + 1));
            } else if (SimpleScope.getBuiltinFunctions().containsKey(first.value)) {
                println("Function: " + first, depth);
                println("Arguments: ", depth);
                children.stream().skip(1).forEach(expr -> expr.doSyntaxAnalysis(depth + 1));
            } else {
                println("Function: " + first, depth);
                println("Arguments: ", depth);
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
