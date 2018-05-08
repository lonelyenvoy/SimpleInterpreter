package util;

import exception.TypeError;
import infrastructure.SimpleExpression;
import infrastructure.SimpleScope;
import type.SimpleList;
import type.SimpleObject;

public class SimpleListUtils {
    public static SimpleList retrieve(SimpleExpression[] expressions, SimpleScope scope, String operationName) {
        SimpleObject obj = null;
        Assert
                .True(expressions.length == 1 && ((obj = expressions[0].evaluate(scope)) instanceof SimpleList))
                .orThrows(TypeError.class, "<" + operationName + "> function only accepts a list param");
        return (SimpleList) obj;
    }
}
