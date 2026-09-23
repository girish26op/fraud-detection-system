package com.fraud.sys.exceptions;

/**
 * Custom checked exception thrown when a transaction violates one or more fraud rules.
 */
public class FraudDetectedException extends Exception {

    public FraudDetectedException(String message) {
        super(message);
    }

    public FraudDetectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
