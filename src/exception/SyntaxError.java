package exception;

public class SyntaxError extends RuntimeException {
    public SyntaxError(String message) {
        super("SyntaxError: " + message);
    }
}
