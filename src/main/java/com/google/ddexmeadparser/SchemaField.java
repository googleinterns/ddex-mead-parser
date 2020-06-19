package com.google.ddexmeadparser;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.Objects;

public class SchemaField implements SchemaAnnotated {
    String fieldValue;
    XmlSchemaAnnotation fieldAnnotation;
    QName fieldQName;
    boolean fieldRepeated;

    public SchemaField(String value, QName qName, boolean repeated) {
        fieldValue = value;
        fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        fieldRepeated = repeated;
    }

    public SchemaField(String value, QName qName) {
        fieldValue = value;
        fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        fieldRepeated = false;
    }

    // Default string QName
    public SchemaField(String value) {
        fieldValue = value;
        fieldQName = new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");
        fieldRepeated = false;
        fieldAnnotation = null;
    }

    public void setAnnotation(XmlSchemaAnnotation annotation) {
        fieldAnnotation = annotation;
    }

    public XmlSchemaAnnotation getAnnotation() { return fieldAnnotation; }

    public String getFieldValue() {
        return fieldValue;
    }

    public QName getFieldType() {
        return fieldQName;
    }

    public boolean isRepeated() { return fieldRepeated; }

    public boolean isXmlType() {
        return fieldQName.getPrefix().equals("xs");
    }
}
