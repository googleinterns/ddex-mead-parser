package com.google.ddexmeadparser;

import javax.xml.transform.stream.StreamSource;

public class SchemaConverter {
    public SchemaEntryMap convert(StreamSource inputXml) throws SchemaConversionException {
        SchemaConverterInstance converterInstance = new SchemaConverterInstance(inputXml);
        return converterInstance.convert();
    }
}
