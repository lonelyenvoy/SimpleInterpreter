import exception.TypeError;
import infrastructure.SimpleExpression;
import infrastructure.SimpleExpressions;
import infrastructure.SimpleScope;
import type.SimpleBoolean;
import type.SimpleList;
import type.SimpleNumber;
import type.SimpleObject;
import util.Assert;
import util.SimpleBooleanUtils;
import util.SimpleListUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main {

    public static void main(String[] sysArgs) {

        System.out.print("Welcome to Simple language v-0.0.2 alpha.\nType in expressions for evaluation.\n\n");

        new SimpleScope(null)
                .buildIn("+", (args, scope) ->
                        new SimpleNumber(Arrays.stream(args)
                                .map(expr -> expr.evaluate(scope))
                                .map(SimpleObject::toNumber)
                                .mapToLong(SimpleNumber::getValue)
                                .sum())
                )
                .buildIn("-", (args, scope) -> {
                    List<SimpleNumber> numbers = Arrays.stream(args)
                            .map(expr -> expr.evaluate(scope))
                            .map(SimpleObject::toNumber).collect(Collectors.toList());
                    long firstValue = numbers.get(0).getValue();
                    if (numbers.size() == 1) {
                        return new SimpleNumber(-firstValue);
                    }
                    return new SimpleNumber(firstValue - numbers.stream().skip(1).mapToLong(SimpleNumber::getValue).sum());
                })
                .buildIn("*", (args, scope) ->
                        new SimpleNumber(Arrays.stream(args)
                                .map(expr -> expr.evaluate(scope))
                                .map(SimpleObject::toNumber)
                                .mapToLong(SimpleNumber::getValue)
                                .reduce(1, (x, y) -> x * y))
                )
                .buildIn("/", (args, scope) -> {
                    List<SimpleNumber> numbers = Arrays.stream(args)
                            .map(expr -> expr.evaluate(scope))
                            .map(SimpleObject::toNumber).collect(Collectors.toList());
                    long firstValue = numbers.get(0).getValue();
                    return new SimpleNumber(firstValue / numbers.stream().skip(1).mapToLong(SimpleNumber::getValue).reduce(1, (x, y) -> x * y));
                })
                .buildIn("%", (args, scope) -> {
                    Assert.True(args.length == 2).orThrows(TypeError.class, "<mod> function only accepts 2 params");
                    List<SimpleNumber> numbers = Arrays.stream(args)
                            .map(expr -> expr.evaluate(scope))
                            .map(SimpleObject::toNumber).collect(Collectors.toList());
                    return new SimpleNumber(numbers.get(0).getValue() % numbers.get(1).getValue());
                })
                .buildIn("and", (args, scope) -> {
                    Assert.True(args.length == 0).orThrows(TypeError.class, "<and> function does not accept 0 params");
                    return SimpleBoolean.valueOf(Arrays.stream(args).map(expr -> expr.evaluate(scope))
                            .allMatch(result -> SimpleBoolean.toPrimitive((SimpleBoolean) (result))));
                })
                .buildIn("or", (args, scope) -> {
                    Assert.True(args.length == 0).orThrows(TypeError.class, "<or> function does not accept 0 params");
                    return SimpleBoolean.valueOf(Arrays.stream(args).map(expr -> expr.evaluate(scope))
                            .anyMatch(result -> SimpleBoolean.toPrimitive((SimpleBoolean) (result))));

                })
                .buildIn("not", (args, scope) -> {
                    Assert.True(args.length == 1).orThrows(TypeError.class, "<not> function only accepts 1 param");
                    return SimpleBoolean.valueOf(SimpleBoolean.valueOf(args[0].evaluate(scope)).negate());
                })
                .buildIn("=", (args, scope) -> SimpleBooleanUtils.chainRelations(args, scope, (x, y) -> x.getValue().equals(y.getValue())))
                .buildIn(">", (args, scope) -> SimpleBooleanUtils.chainRelations(args, scope, (x, y) -> x.getValue().compareTo(y.getValue()) > 0))
                .buildIn("<", (args, scope) -> SimpleBooleanUtils.chainRelations(args, scope, (x, y) -> x.getValue().compareTo(y.getValue()) < 0))
                .buildIn("<=", (args, scope) -> SimpleBooleanUtils.chainRelations(args, scope, (x, y) -> x.getValue().compareTo(y.getValue()) <= 0))
                .buildIn(">=", (args, scope) -> SimpleBooleanUtils.chainRelations(args, scope, (x, y) -> x.getValue().compareTo(y.getValue()) >= 0))
                .buildIn("first", (args, scope) -> {
                    SimpleList list = SimpleListUtils.retrieve(args, scope, "first");
                    return StreamSupport.stream(list.spliterator(), false).limit(1).collect(Collectors.toList()).get(0);
                })
                .buildIn("rest", (args, scope) -> {
                    SimpleList list = SimpleListUtils.retrieve(args, scope, "rest");
                    return new SimpleList(StreamSupport.stream(list.spliterator(), false).skip(1).collect(Collectors.toList()));
                })
                .buildIn("append", (args, scope) -> {
                    SimpleObject obj0 = null, obj1 = null;
                    Assert
                            .True(args.length == 2
                                    && ((obj0 = args[0].evaluate(scope)) instanceof SimpleList)
                                    && ((obj1 = args[1].evaluate(scope)) instanceof SimpleList))
                            .orThrows(TypeError.class, "<append> function only accepts 2 lists");
                    SimpleList list0 = (SimpleList) obj0,
                            list1 = (SimpleList) obj1;
                    return new SimpleList(
                            Stream.concat(
                                    StreamSupport.stream(list0.spliterator(), false),
                                    StreamSupport.stream(list1.spliterator(), false))
                                    .collect(Collectors.toList()));
                })
                .buildIn("print", (args, scope) -> {
                    for (SimpleExpression expr : args) {
                        System.out.print(expr.evaluate(scope));
                        System.out.print(" ");
                    }
                    System.out.println();
                    return null;
                })
                .keepInterpretingInConsole((code, scope) -> SimpleExpressions.parse(code).evaluate(scope));
    }
}
