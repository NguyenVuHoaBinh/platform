package viettel.dac.backend.execution.exception;

import viettel.dac.backend.common.exception.BaseException;

public class ExecutionException extends BaseException {
    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}