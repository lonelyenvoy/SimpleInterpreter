package infrastructure;

import exception.ReferenceError;
import exception.RuntimeInternalError;
import type.SimpleObject;
import util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiFunction;

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
        this.variableTable = new HashMap<String, SimpleObject>();
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

    public void keepInterpretingInConsole(BiFunction<String, SimpleScope, SimpleObject> evaluate) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("> ");
                if (!scanner.hasNext()) {
                    break;
                }
                String code = scanner.nextLine();
                if (!code.trim().equals("")) {
                    SimpleObject result = evaluate.apply(code, this);
                    if (result != null) {
                        System.out.println(result);
                    }
                }
            } catch (Throwable e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
