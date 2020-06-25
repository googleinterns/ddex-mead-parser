package com.google.ddexmeadparser;

/** The type Schema conversion exception. */
public class SchemaConversionException extends Exception {
  /**
   * Instantiates a new Schema conversion exception.
   *
   * @param message the message
   */
public SchemaConversionException(String message) {
    super(message); }
  /**
   * Instantiates a new Schema conversion exception.
   *
   * @param message the message
   * @param root the root
   */
public SchemaConversionException(String message, Exception root) {
        super(message, root);
    }
}
