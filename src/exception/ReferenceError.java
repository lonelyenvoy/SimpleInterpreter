package exception;

public class ReferenceError extends SimpleRuntimeException {
    public ReferenceError(String message) {
        super("ReferenceError: " + message);
    }
}
