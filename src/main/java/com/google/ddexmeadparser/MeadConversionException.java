package com.google.ddexmeadparser;

/** The type Mead conversion exception. */
public class MeadConversionException extends Exception {
  /**
   * Instantiates a new Mead conversion exception.
   *
   * @param message the message
   */
public MeadConversionException(String message) {
        super(message);
    }
  /**
   * Instantiates a new Mead conversion exception.
   *
   * @param message the message
   * @param root the root
   */
public MeadConversionException(String message, Exception root) {
        super(message, root);
    }
}
