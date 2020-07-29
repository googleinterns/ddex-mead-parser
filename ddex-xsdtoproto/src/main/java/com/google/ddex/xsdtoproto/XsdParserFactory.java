package com.google.ddex.xsdtoproto;

public class XsdParserFactory {
  private XsdParserFactory() {}

  public static XsdParser newInstant() {
    return new XsdParserImpl();
  }
}
