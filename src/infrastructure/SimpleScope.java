package infrastructure;

import exception.ReferenceError;
import exception.RuntimeInternalError;
import exception.SyntaxError;
import type.SimpleObject;
import util.Assert;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleScope {
    private SimpleScope parent;
    private Map<String, SimpleObject> variableTable;
    private static Map<String, BiFunction<SimpleExpression[], SimpleScope, SimpleObject>> builtinFunctions = new HashMap<>();

    public SimpleScope getParent() {
        return parent;
    }
    public static Map<String, BiFunction<SimpleExpression[], SimpleScope, SimpleObject>> getBuiltinFunctions() {
        return builtinFunctions;
    }

    public SimpleScope(SimpleScope parent) {
        this.parent = parent;
        this.variableTable = new HashMap<>();
    }

    public SimpleScope buildIn(String name, BiFunction<SimpleExpression[], SimpleScope, SimpleObject> builtinFunction) {
        SimpleScope.builtinFunctions.put(name, builtinFunction);
        return this;
    }

    public SimpleObject find(String name) throws ReferenceError {
        SimpleScope current = this;
        while (current != null) {
            if (current.variableTable.containsKey(name)) {
                return current.variableTable.get(name);
            }
            current = current.getParent();
        }
        throw new ReferenceError(name + " is not defined");
    }

    public SimpleObject define(String name, SimpleObject value) {
        variableTable.put(name, value);
        return value;
    }

    public SimpleScope spawnScopeWithVariables(String[] names, SimpleObject[] values) {
        Assert.True(names.length >= values.length).orThrows(RuntimeInternalError.class, "Too many arguments");
        SimpleScope scope = new SimpleScope(this);
        for (int i = 0; i < values.length; i++) {
            scope.variableTable.put(names[i], values[i]);
        }
        return scope;
    }

    public SimpleObject findLazily(String name) {
        if (variableTable.containsKey(name)) {
            return variableTable.get(name);
        }
        return null;
    }

    public void interpret(InputStream inputStream,
                          Function<String, SimpleExpressionStatus> check,
                          BiFunction<String, SimpleScope, SimpleObject> evaluate,
                          boolean showPrompts,
                          boolean showResults) {
        Scanner scanner = new Scanner(inputStream);
        StringBuilder code = new StringBuilder();
        while (true) {
            try {
                if (showPrompts) System.out.print(code.length() == 0 ? ">>> " : "... ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                code.append(scanner.nextLine());
                if (!code.toString().trim().equals("")) {
                    code.append('\n');
                    String codeString = code.toString();
                    SimpleExpressionStatus status = check.apply(codeString);
                    if (status == SimpleExpressionStatus.OK) {
                        SimpleObject result = evaluate.apply(codeString, this);
                        if (showResults && result != null) {
                            System.out.println(result);
                        }
                        code.setLength(0); // clear
                    } else if (status == SimpleExpressionStatus.INVALID) {
                        throw new SyntaxError("Invalid expression");
                    }
                    // status == SimpleExpressionStatus.INCOMPLETE
                    // continue
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                code.setLength(0); // clear
            }
        }
    }
}
