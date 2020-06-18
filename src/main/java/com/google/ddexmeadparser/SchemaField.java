package com.google.ddexmeadparser;

import com.google.common.base.CaseFormat;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

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

    public String getEntryAnnotationString() {
        StringBuilder annotationBuilder = new StringBuilder();
        if (fieldAnnotation != null) {
            for (int i = 0; i < fieldAnnotation.getItems().size(); i++) {
                XmlSchemaDocumentation documentation = (XmlSchemaDocumentation) fieldAnnotation.getItems().get(i);
                NodeList markup = documentation.getMarkup();
                for (int j = 0; j < markup.getLength(); j++) {
                    annotationBuilder.append(markup.item(j).getTextContent());
                    if (j != markup.getLength() - 1) annotationBuilder.append('\n');
                }
            }
        }
        return annotationBuilder.toString();
    }

    public void setFieldAnnotation(XmlSchemaAnnotation fieldAnnotation) {
        this.fieldAnnotation = fieldAnnotation;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public XmlSchemaAnnotation getAnnotation() { return fieldAnnotation; }

    public QName getFieldType() {
        return fieldQName;
    }

    public boolean isRepeated() { return fieldRepeated; }

    public boolean isXmlType() {
        return fieldQName.getPrefix().equals("xs");
    }
}
