package exception;

public class TypeError extends SimpleRuntimeException {
    public TypeError(String message) {
        super("TypeError: " + message);
    }
}
