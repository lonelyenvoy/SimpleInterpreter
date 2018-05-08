package exception;

public class RuntimeInternalError extends RuntimeException {
    public RuntimeInternalError(String message) {
        super("RuntimeInternalError: " + message);
    }
}
