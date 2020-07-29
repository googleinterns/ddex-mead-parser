package com.google.ddex.xsdtoproto;

import java.io.Reader;

/** The XsdParser interface handles conversion from DDEX XSD to Protocol Buffer schemas. */
public interface XsdParser {
    /**
     * Parse DDEX XSD (schema).
     *
     * @param reader The reader for the input DDEX XSD
     * @return The {@link com.google.ddex.xsdtoproto.ProtoSchema} representation of the XSD.
     * @throws XsdParseException If any problem occurred parsing the DDEX XSD
     */
    public ProtoSchema parse(Reader reader) throws XsdParseException;

    /**
     * Parse DDEX XSD (schema).
     *
     * @param reader The reader for the input DDEX XSD.
     * @param reporter Reference to a XsdParseReporter, which will store namespaces processed and warnings generated during parse.
     * @return The {@link com.google.ddex.xsdtoproto.ProtoSchema} representation of the XSD.
     * @throws XsdParseException If any problem occurred parsing the DDEX XSD
     */
    public ProtoSchema parse(Reader reader, XsdParserReporter reporter) throws XsdParseException;
}
