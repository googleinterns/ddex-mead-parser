package com.google.ddexmeadparser;

public class MeadParseException extends Exception {
    public MeadParseException(String message) {
        super(message);
    }
    public MeadParseException(String message, Exception root) {
        super(message, root);
    }
}
