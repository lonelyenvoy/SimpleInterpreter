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
        if (exceptionClass == null) {
            throw new IllegalArgumentException("exceptionClass cannot be null");
        }
        if (!Throwable.class.isAssignableFrom(exceptionClass)) {
            throw new IllegalArgumentException(exceptionClass.toString() + " is not a subclass of Throwable");
        }
        try {
            Object exception = exceptionClass.getDeclaredConstructor(String.class).newInstance(message);
            throw (RuntimeException) exception;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(exceptionClass.toString() + " cannot be initialized", e);
        }
    }
}
