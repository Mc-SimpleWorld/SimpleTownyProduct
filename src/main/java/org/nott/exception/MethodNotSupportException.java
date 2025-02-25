package org.nott.exception;

/**
 * @author Nott
 * @date 2025-2-25
 */
public class MethodNotSupportException extends RuntimeException {

    public MethodNotSupportException(String message) {
        super(message);
    }

    public MethodNotSupportException() {
        super("Method not support");
    }
}
