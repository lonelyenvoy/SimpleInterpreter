package exception;

public class TypeError extends RuntimeException {
    public TypeError(String message) {
        super("TypeError: " + message);
    }
}
