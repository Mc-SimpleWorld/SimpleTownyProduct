package org.nott.exception;

/**
 * @author Nott
 * @date 2025-3-3
 */
public class ProductException extends RuntimeException {

    public ProductException() {
    }

    public ProductException(String message) {
        super(message);
    }
}
