package exception;

public class SyntaxError extends SimpleRuntimeException {
    public SyntaxError(String message) {
        super("SyntaxError: " + message);
    }
}
