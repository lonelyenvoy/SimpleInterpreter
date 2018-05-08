package infrastructure;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SimpleTokenizer {
    public static String[] tokenize(String text) {
        return Arrays.stream(
                text
                        .replace("(", " ( ")
                        .replace(")", " ) ")
                        .split("[ \t\r\n]"))
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}
