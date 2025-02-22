package org.nott.exception;

public class TimeFormatException extends RuntimeException {
    public TimeFormatException(Throwable e) {
        super(e);
    }

    public TimeFormatException(String message) {
        super(message);
    }
}
