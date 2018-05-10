package util;

import infrastructure.SimpleTokenizer;

public class LexicalAnalysisUtils {
    public static void analysis(String code) {
        int count = 0;
        for (String lex : SimpleTokenizer.tokenize(code)) {
            System.out.println("  " + count + ":\t" + lex);
            count++;
        }
    }
}
