package com.google.ddex.xsdtoproto;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.Objects;

/** The type Schema field. */
public class ProtoSchemaField implements ProtoSchemaAnnotated {
  /** The Field value. */
  String fieldValue;
  /** The Field annotation. */
  String fieldAnnotation;
  /** The Field q name. */
  QName fieldQName;
  /** The Field is repeated. */
  boolean fieldIsRepeated;

  String version;

  boolean deprecated;

  /**
   * Instantiates a new Schema field.
   *
   * @param value the value
   * @param qName the q name
   * @param repeated the repeated
   */
  public ProtoSchemaField(String value, QName qName, boolean repeated) {
    fieldValue = value;
    fieldQName =
        Objects.requireNonNullElseGet(
            qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
    fieldIsRepeated = repeated;
    deprecated = false;
  }

  /**
   * Instantiates a new Schema field.
   *
   * @param value the value
   * @param qName the q name
   */
  public ProtoSchemaField(String value, QName qName) {
    fieldValue = value;
    fieldQName =
        Objects.requireNonNullElseGet(
            qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
    fieldIsRepeated = false;
    deprecated = false;
  }

  /**
   * Instantiates a new Schema field.
   *
   * @param value the value
   */
  // Default string QName
  public ProtoSchemaField(String value) {
    fieldValue = value;
    fieldQName = new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");
    fieldIsRepeated = false;
    fieldAnnotation = null;
    deprecated = false;
  }

  public void setVersionAnnotation(String v) {
    version = v;
  }

  public void setAnnotation(String annotation) {
    fieldAnnotation = annotation;
  }

  public void setAnnotation(XmlSchemaAnnotation annotation) {
    StringBuilder annotationStringBuilder = new StringBuilder();
    if (annotation == null || annotation.getItems() == null) return;

    for (int i = 0; i < annotation.getItems().size(); i++) {
      XmlSchemaDocumentation documentation = (XmlSchemaDocumentation) annotation.getItems().get(i);
      NodeList markup = documentation.getMarkup();
      for (int j = 0; j < markup.getLength(); j++) {
        annotationStringBuilder.append(markup.item(j).getTextContent());
        if (j != markup.getLength() - 1) annotationStringBuilder.append('\n');
      }
    }
    fieldAnnotation = annotationStringBuilder.toString();
  }

  public String getVersionAnnotation() {
    return version;
  }

  public String getAnnotation() {
    return fieldAnnotation;
  }

  /**
   * Gets field value.
   *
   * @return the field value
   */
  public String getFieldValue() {
    return fieldValue;
  }

  /**
   * Gets field type.
   *
   * @return the field type
   */
  public QName getFieldType() {
    return fieldQName;
  }

  /**
   * Is repeated boolean.
   *
   * @return the boolean
   */
  public boolean isRepeated() {
    return fieldIsRepeated;
  }

  /**
   * Is xml type boolean.
   *
   * @return the boolean
   */
  public boolean isXmlType() {
    return fieldQName.getPrefix().equals("xs");
  }

  public boolean isDeprecated() {
    return deprecated;
  }
  public void markDeprecated() {
    deprecated = true;
  }
}
