package org.nott.exception;

/**
 * @author Nott
 * @date 2025-2-25
 */
public class VersionNotCorrectException extends Exception {

    public VersionNotCorrectException() {
    }

    public VersionNotCorrectException(String message) {
        super(message);
    }
}
