package com.google.ddex.xsdtoproto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

/**
 * The ProtoSchemaAbstractEntry represents a type defined in the DDEX XSD. Each entry is output as a
 * Message type definition in the final .proto schema.
 */
public abstract class ProtoSchemaAbstractEntry implements ProtoSchemaAnnotated {
  final String entryTitle;
  final String entryNamespacePrefix;
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
   * @param entryField The field to be added to an entry
   */
  public void addField(ProtoSchemaField entryField) {
    entryFields.put(entryField.getFieldName(), entryField);
    if (entryField.getFieldName().equals("ext_value")) {
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
   * Gets annotation. This annotation stores extracted XML annotations found in the original DDEX
   * XSD.
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
   * @return A list of the fields in this entry.
   */
  public List<ProtoSchemaField> getFields() {
    return new ArrayList<>(entryFields.values());
  }

  /**
   * Gets fields.
   *
   * @return A map of the fields in this entry. The map keys are the names of the entries.
   */
  public Map<String, ProtoSchemaField> getFieldMap() {
    return entryFields;
  }

  /**
   * Entry is an extension flag.
   *
   * @return Boolean value that represents whether the entry is based off an XSD extension type. In
   *     this case, the type will contain an "ext_value" field.
   */
  public boolean isExtension() {
    return entryIsExtension;
  }

  /**
   * Entry is populated flag.
   *
   * @return Boolean value that represents whether the entry is populated by any {@link
   *     ProtoSchemaField}'s
   */
  public boolean isPopulated() {
    return entryFields.size() > 0;
  }

  /**
   * Entry is an enum flag.
   *
   * @return Boolean value that represents whether the entry is an enum type or not
   */
  public boolean isEnum() {
    return false;
  }

  /**
   * Entry is a message flag.
   *
   * @return Boolean value that represents whether the entry is a message type or not
   */
  public boolean isMessage() {
    return false;
  }
}
