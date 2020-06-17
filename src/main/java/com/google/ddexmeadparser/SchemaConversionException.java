package com.google.ddexmeadparser;

public class SchemaConversionException extends Exception{
    public SchemaConversionException(String message) { super(message); }
    public SchemaConversionException(String message, Exception root) {
        super(message, root);
    }
}
