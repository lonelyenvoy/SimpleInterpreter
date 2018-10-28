package util;

public class NumberUtils {
    /**
     * Turn double value into three possible integers:
     * 1  - if value > 0
     * 0  - if value == 0
     * -1 - if value < 0
     * @param value the given value
     * @return the ternary result
     */
    public static int makeTernary(double value) {
        if (value == 0) return 0;
        if (value > 0) return 1;
        return -1;
    }
}
