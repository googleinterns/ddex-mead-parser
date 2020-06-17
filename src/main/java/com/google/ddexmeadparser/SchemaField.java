package com.google.ddexmeadparser;

import javax.xml.namespace.QName;
import java.util.Objects;

public class SchemaField {
    String fieldValue;
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
    }

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
