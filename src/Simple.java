import constant.EnvironmentConstants;
import exception.TypeError;
import infrastructure.SimpleExpression;
import infrastructure.SimpleExpressions;
import infrastructure.SimpleScope;
import type.*;
import util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Simple {

    private SimpleScope environment;

    private Simple(SimpleScope environment) {
        this.environment = environment;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.print("Welcome to Simple language " + EnvironmentConstants.VERSION + ".\nType in expressions for evaluation.\n\n");
            createEnvironment().runREPL();
        } else if (args[0].equals("-v")) {
            System.out.println(EnvironmentConstants.VERSION);
        } else if (args[0].equals("-i")) {
            List<File> files = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                files.add(new File(args[i]));
            }
            createEnvironment().parseFiles(files.toArray(new File[0]));
        } else if (args[0].equals("-l")) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                for (String line : Files.readAllLines(Paths.get(args[i]))) {
                    builder.append(line);
                }
            }
            LexicalAnalysisUtils.analysis(builder.toString());
        } else if (args[0].equals("-s")) {
            List<String> lines = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                lines.addAll(Files.readAllLines(Paths.get(args[i])));
            }
            SyntaxAnalysisUtils.analyse(
                    lines,
                    createEnvironment().environment,
                    SimpleExpressions::check,
                    (code, scope) -> SimpleExpressions.parse(code).doSyntaxAnalysis(0));
        } else {
            showHelp();
        }
    }

    private static Simple createEnvironment() {
        return new Simple(SimpleScope.ofRoot()
                .buildIn("+", (args, scope) ->
                        SimpleNumber.of(Arrays.stream(args)
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
                        return SimpleNumber.of(-firstValue);
                    }
                    return SimpleNumber.of(firstValue - numbers.stream().skip(1).mapToLong(SimpleNumber::getValue).sum());
                })
                .buildIn("*", (args, scope) ->
                        SimpleNumber.of(Arrays.stream(args)
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
                    return SimpleNumber.of(firstValue / numbers.stream().skip(1).mapToLong(SimpleNumber::getValue).reduce(1, (x, y) -> x * y));
                })
                .buildIn("%", (args, scope) -> {
                    Assert.True(args.length == 2).orThrows(TypeError.class, "<mod> function only accepts 2 params");
                    List<SimpleNumber> numbers = Arrays.stream(args)
                            .map(expr -> expr.evaluate(scope))
                            .map(SimpleObject::toNumber).collect(Collectors.toList());
                    return SimpleNumber.of(numbers.get(0).getValue() % numbers.get(1).getValue());
                })
                .buildIn("and", (args, scope) -> {
                    Assert.True(args.length != 0).orThrows(TypeError.class, "<and> function does not accept 0 params");
                    return SimpleBoolean.valueOf(Arrays.stream(args).map(expr -> expr.evaluate(scope))
                            .allMatch(result -> SimpleBoolean.toPrimitive((SimpleBoolean) (result))));
                })
                .buildIn("or", (args, scope) -> {
                    Assert.True(args.length != 0).orThrows(TypeError.class, "<or> function does not accept 0 params");
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
                    return SimpleList.of(StreamSupport.stream(list.spliterator(), false).skip(1).collect(Collectors.toList()));
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
                    return SimpleList.of(
                            Stream.concat(
                                    StreamSupport.stream(list0.spliterator(), false),
                                    StreamSupport.stream(list1.spliterator(), false))
                                    .collect(Collectors.toList()));
                })
                .buildIn("empty", (args, scope) -> {
                    Assert.True(args.length == 1).orThrows(TypeError.class, "<empty> function only accepts 1 param");
                    SimpleObject list = args[0].evaluate(scope);
                    Assert.True(list instanceof SimpleList).orThrows(TypeError.class, "<empty> function only accepts list params");
                    return SimpleBoolean.valueOf(!((SimpleList) list).iterator().hasNext());
                })
                .buildIn("print", (args, scope) -> {
                    for (SimpleExpression expr : args) {
                        System.out.print(expr.evaluate(scope));
                        System.out.print(" ");
                    }
                    System.out.println();
                    return null;
                })
                .buildIn("random", (args, scope) -> {
                    Assert.True(args.length == 2).orThrows(TypeError.class, "<random> function only accepts 2 params");
                    SimpleObject low = args[0].evaluate(scope);
                    SimpleObject high = args[1].evaluate(scope);
                    Assert
                            .True(low instanceof SimpleNumber && high instanceof SimpleNumber)
                            .orThrows(TypeError.class, "<random> function only accepts number params");
                    return SimpleNumber.of(ThreadLocalRandom.current().nextLong(
                            ((SimpleNumber) low).getValue(), ((SimpleNumber) high).getValue() + 1));
                })
                .buildIn("sort", (args, scope) -> {
                    Assert.True(args.length >= 1 && args.length <= 3).orThrows(TypeError.class, "<sort> function only accepts 1-3 params");
                    SimpleObject list = args[0].evaluate(scope);
                    SimpleObject ascending = SimpleBoolean.valueOf(true);
                    SimpleObject criterionIndex = null;
                    if (args.length >= 2) {
                        ascending = args[1].evaluate(scope);
                        if (args.length == 3) {
                            criterionIndex = args[2].evaluate(scope);
                        }
                    }
                    Assert.True(list instanceof SimpleList).orThrows(TypeError.class, "<sort> function only accepts a list as param 1");
                    Assert.True(ascending instanceof SimpleBoolean).orThrows(TypeError.class, "<sort> function only accepts a boolean as param 2");
                    Assert.True(criterionIndex == null || criterionIndex instanceof SimpleNumber).orThrows(TypeError.class, "<sort> function only accepts a number as param 3");

                    if (criterionIndex == null) { // simple sort
                        List<SimpleObject> numberList = new ArrayList<>();
                        for (SimpleObject element : ((SimpleList) list)) {
                            Assert.True(element instanceof SimpleNumber).orThrows(TypeError.class, "Unable to sort non-number values");
                            SimpleNumber numberElement = element.toNumber();
                            numberList.add(numberElement);
                        }
                        SimpleObject finalAscending = ascending;
                        numberList.sort((o1, o2) -> {
                            SimpleNumber n1 = (SimpleNumber) o1;
                            SimpleNumber n2 = (SimpleNumber) o2;
                            Long difference = SimpleBoolean.toPrimitive((SimpleBoolean) finalAscending)
                                    ? n1.getValue() - n2.getValue()
                                    : n2.getValue() - n1.getValue();
                            return NumberUtils.makeTernary(difference);
                        });
                        return SimpleList.of(numberList);
                    } else { // complex sort
                        List<List<Long>> sortingList = new ArrayList<>();
                        for (SimpleObject element : ((SimpleList) list)) {
                            Assert.True(element instanceof SimpleList).orThrows(TypeError.class, "Unable to sort non-list values");
                            SimpleList elementList = ((SimpleList) element);
                            List<Long> longList = new ArrayList<>();
                            for (SimpleObject subelement : elementList) {
                                Assert.True(subelement instanceof SimpleNumber).orThrows(TypeError.class, "Unable to sort non-number values");
                                longList.add(((SimpleNumber) subelement).getValue());
                            }
                            sortingList.add(longList);
                        }
                        int criterionIndexNumber = (int)(long)((SimpleNumber) criterionIndex).getValue();
                        SimpleObject finalAscending = ascending;
                        sortingList.sort((o1, o2) -> {
                            Assert.True(o1.size() > criterionIndexNumber
                                    && o2.size() > criterionIndexNumber)
                                    .orThrows(TypeError.class, "One of the inner lists is too short for <sort> function");
                            Long l1 = o1.get(criterionIndexNumber);
                            Long l2 = o2.get(criterionIndexNumber);
                            Long difference = SimpleBoolean.toPrimitive((SimpleBoolean) finalAscending)
                                    ? l1 - l2
                                    : l2 - l1;
                            return NumberUtils.makeTernary(difference);
                        });
                        List<SimpleObject> resultList = new ArrayList<>();
                        for (List<Long> element : sortingList) {
                            List<SimpleObject> subList = new ArrayList<>();
                            for (Long subelement : element) {
                                subList.add(SimpleNumber.of(subelement));
                            }
                            resultList.add(SimpleList.of(subList));
                        }
                        return SimpleList.of(resultList);
                    }
                })
        );
    }

    private void runREPL() {
        environment.interpret(
                System.in,
                SimpleExpressions::check,
                (code, scope) -> SimpleExpressions.parse(code).evaluate(scope),
                true,
                true);
    }

    private void parseFiles(File... files) throws FileNotFoundException {
        List<FileInputStream> fileInputStreams = new ArrayList<>();
        for (File file : files) {
            fileInputStreams.add(new FileInputStream(file));
        }
        SequenceInputStream stream = new SequenceInputStream(Collections.enumeration(new ArrayList<InputStream>() {{
            addAll(fileInputStreams);
        }}));
        environment.interpret(
                stream,
                SimpleExpressions::check,
                (code, scope) -> SimpleExpressions.parse(code).evaluate(scope),
                false,
                false);
    }

    private static void showHelp() {
        System.out.println("Usage: ");
        System.out.println("  (no argument)                 Enter REPL");
        System.out.println("  -v                            Show version");
        System.out.println("  -i filename1 [filename2 ...]  Read and evaluate code from input files");
        System.out.println("  -l filename1 [filename2 ...]  Do lexical analysis on code from input files");
        System.out.println("  -s filename1 [filename2 ...]  Do syntax analysis on code from input files");
    }
}
