package com.google.ddex.xsdtoproto;

/**
 * Thrown when a fatal error occurs trying to convert an XSD schema to a Protocol Buffer schema.
 * */
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
   * @param cause the root cause of the XsdParseException
   */
  public XsdParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
