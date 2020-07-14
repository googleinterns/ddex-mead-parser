package com.google.ddex.convertercli;

/** Thrown when the converter has not been supplied valid arguments and options. */
public class InvalidOptionsException extends Exception {
    /**
     * Instantiates a new Invalid options exception.
     *
     * @param message the message
     */
    public InvalidOptionsException(String message) {
        super(message);
    }
    /**
     * Instantiates a new Invalid options exception.
     *
     * @param message the message
     * @param root the root cause of the InvalidOptionsException
     */
    public InvalidOptionsException(String message, Exception root) {
        super(message, root);
    }
}
