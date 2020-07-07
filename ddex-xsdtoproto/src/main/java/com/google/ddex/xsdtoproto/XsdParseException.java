package com.google.ddex.xsdtoproto;

/** The type Schema conversion exception. */
public class XsdParseException extends Exception {
  /**
   * Instantiates a new Schema conversion exception.
   *
   * @param message the message
   */
  public XsdParseException(String message) {
    super(message);
  }
  /**
   * Instantiates a new Schema conversion exception.
   *
   * @param message the message
   * @param root the root
   */
  public XsdParseException(String message, Exception root) {
    super(message, root);
  }
}
