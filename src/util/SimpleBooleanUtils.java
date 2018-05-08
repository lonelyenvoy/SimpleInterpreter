package util;

import exception.TypeError;
import infrastructure.SimpleExpression;
import infrastructure.SimpleScope;
import type.SimpleBoolean;
import type.SimpleNumber;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SimpleBooleanUtils {
    public static SimpleBoolean chainRelations(SimpleExpression[] expressions, SimpleScope scope, BiFunction<SimpleNumber, SimpleNumber, Boolean> relation) {
        Assert.True(expressions.length > 1).orThrows(TypeError.class, "Relation functions only accepts > 1 params");
        SimpleNumber current = (SimpleNumber) expressions[0].evaluate(scope);
        for (SimpleExpression expression : Arrays.stream(expressions).skip(1).collect(Collectors.toList())){
            SimpleNumber next = (SimpleNumber) expression.evaluate(scope);
            if (relation.apply(current, next)) {
                current = next;
            } else {
                return SimpleBoolean.valueOf(false);
            }
        }
        return SimpleBoolean.valueOf(true);
    }
}
