package infrastructure;

import java.util.Arrays;

public class SimpleTokenizer {
    public static String[] tokenize(String text) {
        return Arrays.stream(
                text
                        .replace("(", " ( ")
                        .replace(")", " ) ")
                        .split("[ \t\r\n]"))
                .filter(str -> !str.isEmpty())
                .toArray(String[]::new);
    }
}
