package com.google.ddexmeadparser;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;

public interface SchemaAnnotated {
    XmlSchemaAnnotation getAnnotation();
    void setAnnotation(XmlSchemaAnnotation annotation);
}
