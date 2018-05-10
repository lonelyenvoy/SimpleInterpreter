package util;

public class PrettyPrintUtils {
    public static void println(String string, int indent) {
        printIndent(indent);
        System.out.println(string);
    }

    public static void print(String string, int indent) {
        printIndent(indent);
        System.out.print(string);
    }

    private static void printIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("    ");
        }
    }
}
