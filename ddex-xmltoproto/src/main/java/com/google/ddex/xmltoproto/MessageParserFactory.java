package com.google.ddex.xmltoproto;

public class MessageParserFactory {
    private MessageParserFactory() {}

    public static MessageParser newInstant() {
        return new MessageParserImpl();
    }
}
