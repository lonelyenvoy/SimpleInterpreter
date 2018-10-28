package exception;

public class RuntimeInternalError extends SimpleRuntimeException {
    public RuntimeInternalError(String message) {
        super("RuntimeInternalError: " + message);
    }
}
