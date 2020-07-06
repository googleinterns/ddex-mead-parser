package com.google.ddex.xsdtoproto;

/** The type Schema conversion exception. */
public class SchemaParseException extends Exception {
  /**
   * Instantiates a new Schema conversion exception.
   *
   * @param message the message
   */
  public SchemaParseException(String message) {
    super(message);
  }
  /**
   * Instantiates a new Schema conversion exception.
   *
   * @param message the message
   * @param root the root
   */
  public SchemaParseException(String message, Exception root) {
    super(message, root);
  }
}
