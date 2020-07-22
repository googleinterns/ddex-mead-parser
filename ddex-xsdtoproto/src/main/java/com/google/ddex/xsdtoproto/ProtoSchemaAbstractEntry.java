package com.google.ddex.xsdtoproto;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ProtoSchemaAbstractEntry represents a type defined in the DDEX XSD. Each entry is output as a Message type definition
 * in the final .proto schema.
 */
public abstract class ProtoSchemaAbstractEntry implements ProtoSchemaAnnotated {
  String entryTitle;
  String entryNamespacePrefix;
  String entryAnnotation;
  Map<String, ProtoSchemaField> entryFields;
  boolean entryIsExtension;
  String versionAnnotation;

  /**
   * Instantiates a new Schema abstract entry by entry name and the containing namespace.
   *
   * @param title The name of the entry
   * @param namespacePrefix The namespace prefix that contains this entry
   */
  public ProtoSchemaAbstractEntry(String title, String namespacePrefix) {
    entryTitle = title;
    entryNamespacePrefix = namespacePrefix;
    entryFields = new HashMap<>();
    entryIsExtension = false;
    entryAnnotation = "";
  }

  /**
   * Adds a field to the ProtoSchemaAbstractEntry.
   *
   * @param entryField The field
   */
  public void addField(ProtoSchemaField entryField) {
    entryFields.put(entryField.getFieldValue(), entryField);
    if (entryField.getFieldValue().equals("ext_value")) {
      entryIsExtension = true;
    }
  }

  /**
   * Sets version annotation.
   *
   * @param annotation The annotation
   */
  public void setVersionAnnotation(String annotation) {
    versionAnnotation = annotation;
  }

  /**
   * Sets annotation.
   *
   * @param annotation The annotation
   */
  public void setAnnotation(String annotation) {
    entryAnnotation = annotation;
  }

  /**
   * Sets annotation.
   *
   * @param annotation The annotation
   */
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
    entryAnnotation = annotationStringBuilder.toString();
  }

  /**
   * Gets version annotation. This annotation pertains to the versioning of each entry and field.
   *
   * @return The version annotation
   */
  public String getVersionAnnotation() {
    return versionAnnotation;
  }

  /**
   * Gets annotation. This annotation stores extracted XML annotations found in the original DDEX XSD.
   *
   * @return The annotation
   */
  public String getAnnotation() {
    return entryAnnotation;
  }

  /**
   * Gets title.
   *
   * @return the title
   */
  public String getTitle() {
    return entryTitle;
  }

  /**
   * Gets namespace prefix.
   *
   * @return the namespace prefix
   */
  public String getNamespacePrefix() {
    return entryNamespacePrefix;
  }

  /**
   * Gets fields.
   *
   * @return the fields
   */
  public List<ProtoSchemaField> getFields() {
    return new ArrayList<>(entryFields.values());
  }

  /**
   * Gets fields.
   *
   * @return the fields
   */
  public Map<String, ProtoSchemaField> getFieldMap() {
    return entryFields;
  }

  /**
   * Entry is an extension flag.
   *
   * @return the boolean
   */
  public boolean isExtension() {
    return entryIsExtension;
  }

  /**
   * Entry is populated flag.
   *
   * @return the boolean
   */
  public boolean isPopulated() {
    return entryFields.size() > 0;
  }

  /**
   * Entry is an enum flag.
   *
   * @return the boolean
   */
  public boolean isEnum() {
    return false;
  }

  /**
   * Entry is a message flag.
   *
   * @return the boolean
   */
  public boolean isMessage() {
    return false;
  }
}
