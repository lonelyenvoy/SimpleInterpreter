package util;

import java.lang.reflect.InvocationTargetException;

public class Assert {
    private final boolean ok;
    private Assert(boolean ok) {
        this.ok = ok;
    }

    public static Assert True(boolean condition) {
        return new Assert(condition);
    }

    public void orThrows(Class exceptionClass, String message) {
        if (ok) return;
        try {
            exceptionClass.getDeclaredConstructor(exceptionClass).newInstance(message);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new Error();
        }
    }
}
