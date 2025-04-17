package viettel.dac.backend.execution.exception;

public class TimeoutException extends ExecutionException {
    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}