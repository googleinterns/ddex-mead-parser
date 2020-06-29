package com.google.ddexmeadparser;

import javax.xml.transform.stream.StreamSource;

/** The type Schema converter. */
public class SchemaConverter {
  /**
   * Convert schema entry map.
   *
   * @param inputXml the input xml
   * @return the schema entry map
   * @throws SchemaConversionException the schema conversion exception
   */
  public SchemaEntryMap convert(StreamSource inputXml) throws SchemaConversionException {
    SchemaConverterInstance converterInstance = new SchemaConverterInstance(inputXml);
    return converterInstance.convert();
  }
}
