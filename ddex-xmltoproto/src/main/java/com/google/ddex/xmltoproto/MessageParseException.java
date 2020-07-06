package com.google.ddex.xmltoproto;

/** The type Mead conversion exception. */
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
   * @param root the root
   */
  public MessageParseException(String message, Exception root) {
    super(message, root);
  }
}
