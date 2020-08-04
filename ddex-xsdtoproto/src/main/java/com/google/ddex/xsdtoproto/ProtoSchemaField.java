package com.google.ddex.xsdtoproto;

import java.util.Objects;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

/**
 * The ProtoSchemaField represents a field defined within a {@link ProtoSchemaAbstractEntry}.
 */
public class ProtoSchemaField implements ProtoSchemaAnnotated {
  private static final QName DEFAULT_QNAME =
      new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");

  String fieldName;
  QName fieldType;
  String fieldAnnotation;
  String version;
  boolean fieldIsRepeated;
  boolean deprecated;

  /**
   * Instantiates a new field with a specified type and repeated flag.
   *
   * @param name The field name.
   * @param type The field type.
   * @param repeated The flag .
   */
  public ProtoSchemaField(String name, QName type, boolean repeated) {
    fieldName = name;
    fieldType = Objects.requireNonNullElse(type, DEFAULT_QNAME);
    fieldIsRepeated = repeated;
    deprecated = false;
  }

  /**
   * Instantiates a new field with a specified type.
   *
   * @param name The field name.
   * @param type The field type.
   */
  public ProtoSchemaField(String name, QName type) {
    fieldName = name;
    fieldType = Objects.requireNonNullElse(type, DEFAULT_QNAME);
    fieldIsRepeated = false;
    deprecated = false;
  }

  /**
   * Instantiates a new field.
   *
   * @param name The field name.
   */
  public ProtoSchemaField(String name) {
    fieldName = name;
    fieldType = DEFAULT_QNAME;
    deprecated = false;
  }

  /**
   * Sets the version annotation property. The annotation will be printed as a comment
   * in the .proto schema.
   *
   * @param versionAnnotation The annotation specific to schema versioning
   */
  public void setVersionAnnotation(String versionAnnotation) {
    version = versionAnnotation;
  }

  /**
   * Sets the annotation property. The annotation will be printed as a comment in the
   * .proto schema.
   *
   * @param annotation The annotation
   */
  public void setAnnotation(String annotation) {
    fieldAnnotation = annotation;
  }

  /**
   * Sets the annotation. The annotation will be printed as a comment in the
   * .proto schema.
   *
   * @param annotation The annotation
   */
  public void setAnnotation(XmlSchemaAnnotation annotation) {
    StringBuilder annotationStringBuilder = new StringBuilder();
    if (annotation == null || annotation.getItems() == null) {
      return;
    }

    for (int i = 0; i < annotation.getItems().size(); i++) {
      XmlSchemaDocumentation documentation = (XmlSchemaDocumentation) annotation.getItems().get(i);
      NodeList markup = documentation.getMarkup();
      for (int j = 0; j < markup.getLength(); j++) {
        annotationStringBuilder.append(markup.item(j).getTextContent());
        if (j != markup.getLength() - 1) {
          annotationStringBuilder.append('\n');
        }
      }
    }
    fieldAnnotation = annotationStringBuilder.toString();
  }

  /**
   * Gets the versioning annotation.
   * @return Version annotation.
   */
  public String getVersionAnnotation() {
    return version;
  }

  /**
   * Gets the annotation.
   * @return Annotation.
   */
  public String getAnnotation() {
    return fieldAnnotation;
  }

  /**
   * Gets field value.
   *
   * @return the field value
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Gets field type.
   *
   * @return the field type
   */
  public QName getFieldType() {
    return fieldType;
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
    return fieldType.getPrefix().equals("xs");
  }

  /**
   * Gets the deprecated status of the field.
   *
   * @return The deprecated status.
   */
  public boolean isDeprecated() {
    return deprecated;
  }

  /**
   * Sets the deprecated status of the field.
   *
   * @param flag The status to set the field to.
   */
  public void setDeprecated(boolean flag) {
    deprecated = flag;
  }
}
