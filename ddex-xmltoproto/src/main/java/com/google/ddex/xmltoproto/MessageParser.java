package com.google.ddex.xmltoproto;

import com.google.protobuf.Message;

import java.io.Reader;

/** The MessageParser interface handles conversion from DDEX XSD to Protocol Buffer schemas. */
public interface MessageParser {
  /**
   * Parse DDEX XML message.
   *
   * @param reader The reader for the input DDEX XML
   * @param messageBuilder The protobuf message builder, which can be generated using the MessageBuilder resolver, or
   *                       manually provided.
   * @return The protobuf message
   * @throws MessageParseException If any problem occurred parsing the DDEX XML
   */
  public Message parse(Reader reader, Message.Builder messageBuilder) throws MessageParseException;


  /**
   * Parse DDEX XML message.
   *
   * @param reader The reader for the input DDEX XML
   * @param messageBuilder The protobuf message builder, which can be generated using the MessageBuilder resolver, or
   *                       manually provided.
   * @param reporter Reference to a MessageParseReporter, which will store all warnings generated while parsing the DDEX message
   * @return The protobuf message
   * @throws MessageParseException If any problem occurred parsing the DDEX XML
   */
  public Message parse(Reader reader, Message.Builder messageBuilder, MessageParserReporter reporter) throws MessageParseException;
}