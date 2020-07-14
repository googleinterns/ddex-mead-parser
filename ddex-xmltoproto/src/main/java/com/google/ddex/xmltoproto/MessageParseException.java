package com.google.ddex.xmltoproto;

/** Thrown when a fatal error occurs trying to convert an XML message to a Protocol Buffer message. */
public class MessageParseException extends Exception {
  /**
   * Instantiates a new Mead conversion exception.
   *
   * @param message the message
   */
  public MessageParseException(String message) {
    super(message);
  }
  /**
   * Instantiates a new Mead conversion exception.
   *
   * @param message the message
   * @param root the root cause of the MessageParseException
   */
  public MessageParseException(String message, Exception root) {
    super(message, root);
  }
}
