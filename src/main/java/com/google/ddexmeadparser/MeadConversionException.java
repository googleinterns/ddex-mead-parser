package com.google.ddexmeadparser;

public class MeadConversionException extends Exception {
    public MeadConversionException(String message) {
        super(message);
    }
    public MeadConversionException(String message, Exception root) {
        super(message, root);
    }
}
