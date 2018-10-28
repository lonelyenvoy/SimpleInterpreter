package exception;

public abstract class SimpleRuntimeException extends RuntimeException {
    public SimpleRuntimeException(String message) {
        super(message);
    }
}
