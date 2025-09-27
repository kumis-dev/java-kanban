package taskmanager.exceptions;

public class OverlapException extends RuntimeException {
    String message;

    public OverlapException(String message) {
        this.message = message;
    }
}
