package com.google.ddexmeadparser;

import com.google.common.base.CaseFormat;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;

import javax.xml.namespace.QName;
import java.util.Objects;

public class SchemaField {
    String fieldValue;
    XmlSchemaAnnotation fieldAnnotation;
    QName fieldQName;
    boolean fieldRepeated;

    public SchemaField(String value, QName qName, boolean repeated) {
        fieldValue = value;
        fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
//        fieldAnnotation = annotation;
        fieldRepeated = repeated;
    }

    public SchemaField(String value, QName qName) {
        fieldValue = value;
        fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
//        fieldAnnotation = annotation;
        fieldRepeated = false;
    }

    // Default string QName
    public SchemaField(String value) {
        fieldValue = value;
        fieldQName = new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");
        fieldRepeated = false;
        fieldAnnotation = null;
    }

    public String toSchemaString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldValue));
        return stringBuilder.toString();
    }

    public void setFieldAnnotation(XmlSchemaAnnotation fieldAnnotation) {
        this.fieldAnnotation = fieldAnnotation;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public XmlSchemaAnnotation getFieldAnnotation() { return fieldAnnotation; }

    public QName getFieldType() {
        return fieldQName;
    }

    public boolean isRepeated() { return fieldRepeated; }

    public boolean isXmlType() {
        return fieldQName.getPrefix().equals("xs");
    }
}
