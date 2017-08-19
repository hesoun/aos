package com.hesoun;

/**
 * General application runtime exception
 *
 * @author Jakub Hesoun
 */
public class AosException extends RuntimeException {
    public AosException(Throwable cause) {
        super(cause);
    }

    public AosException(String message) {
        super(message);
    }

    public AosException(String message, Throwable cause) {
        super(message, cause);
    }
}
