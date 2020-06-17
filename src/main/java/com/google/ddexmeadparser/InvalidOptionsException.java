package com.google.ddexmeadparser;

public class InvalidOptionsException extends Exception {
    public InvalidOptionsException(String message) { super(message); }
    public InvalidOptionsException(String message, Exception root) {
        super(message, root);
    }
}
