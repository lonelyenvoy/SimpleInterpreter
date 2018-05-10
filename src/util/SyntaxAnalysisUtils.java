package util;

import exception.SyntaxError;
import infrastructure.SimpleExpressionStatus;
import infrastructure.SimpleScope;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Last updated in version: v-0.0.10 alpha
 */
public class SyntaxAnalysisUtils {
    public static void analyse(List<String> code,
                                SimpleScope environment,
                                Function<String, SimpleExpressionStatus> check,
                                BiConsumer<String, SimpleScope> doSyntaxAnalysis) {
        Iterator<String> iterator = code.iterator();
        String currentCode = "";
        while (true) {
            try {
                if (!iterator.hasNext()) {
                    break;
                }
                currentCode += iterator.next();
                if (!currentCode.trim().equals("")) {
                    currentCode += "\n";
                    SimpleExpressionStatus status = check.apply(currentCode);
                    if (status == SimpleExpressionStatus.OK) {
                        doSyntaxAnalysis.accept(currentCode, environment);
                        currentCode = "";
                    } else if (status == SimpleExpressionStatus.INVALID) {
                        throw new SyntaxError("Invalid expression");
                    }
                    // status == SimpleExpressionStatus.INCOMPLETE
                    // continue
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                currentCode = "";
            }
        }
    }
}
