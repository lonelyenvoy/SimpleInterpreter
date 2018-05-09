package infrastructure;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class SimpleExpressions {
    public static List<String> getValues(List<SimpleExpression> expressions) {
        return expressions.stream().map(SimpleExpression::getValue).collect(Collectors.toList());
    }

    public static SimpleExpression parse(String code) {
        SimpleExpression program = new SimpleExpression("", null);
        SimpleExpression current = program;
        for (String lex : SimpleTokenizer.tokenize(code)) {
            if (lex.equals("(")) {
                SimpleExpression newNode = new SimpleExpression("(", current);
                current.getChildren().add(newNode);
                current = newNode;
            } else if (lex.equals(")")) {
                current = current.getParent();
            } else {
                current.getChildren().add(new SimpleExpression(lex, current));
            }
        }
        return program.getChildren().get(0);
    }

    public static SimpleExpressionStatus check(String code) {
        Stack<Character> stack = new Stack<>();
        for (char c : code.toCharArray()) {
            if (c == '(') stack.push(c);
            else if (c == ')') {
                if (!stack.empty() && stack.peek() == '(') stack.pop();
                else return SimpleExpressionStatus.INVALID;
            }
        }
        return stack.empty() ? SimpleExpressionStatus.OK : SimpleExpressionStatus.INCOMPLETE;
    }
}
