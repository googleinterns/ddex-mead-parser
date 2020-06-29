package com.google.ddexmeadparser;

/** The type Invalid options exception. */
public class InvalidOptionsException extends Exception {
  /**
   * Instantiates a new Invalid options exception.
   *
   * @param message the message
   */
  public InvalidOptionsException(String message) {
    super(message);
  }
  /**
   * Instantiates a new Invalid options exception.
   *
   * @param message the message
   * @param root the root
   */
  public InvalidOptionsException(String message, Exception root) {
    super(message, root);
  }
}
