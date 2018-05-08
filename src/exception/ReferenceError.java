package exception;

public class ReferenceError extends RuntimeException {
    public ReferenceError(String message) {
        super("ReferenceError: " + message);
    }
}
