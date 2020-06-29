package com.google.ddexmeadparser;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Schema abstract entry. */
public abstract class SchemaAbstractEntry implements SchemaAnnotated {
  /** The Entry title. */
  String entryTitle;

  /** The Entry namespace prefix. */
  String entryNamespacePrefix;

  /** The Entry annotation. */
  String entryAnnotation;

  /** The Entry fields. */
  Map<String, SchemaField> entryFields;

  /** The Entry is extension. */
  boolean entryIsExtension;

  /**
   * Instantiates a new Schema abstract entry.
   *
   * @param title the title
   * @param namespacePrefix the namespace prefix
   */
  public SchemaAbstractEntry(String title, String namespacePrefix) {
    entryTitle = title;
    entryNamespacePrefix = namespacePrefix;
    entryFields = new HashMap<>();
    entryIsExtension = false;
    entryAnnotation = "";
  }

  /**
   * Add field.
   *
   * @param entryField the entry field
   */
  public void addField(SchemaField entryField) {
    entryFields.put(entryField.getFieldValue(), entryField);
    if (entryField.getFieldValue().equals("ext_value")) {
      entryIsExtension = true;
    }
  }

  public void setAnnotation(String annotation) {
    entryAnnotation = annotation;
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
    entryAnnotation = annotationStringBuilder.toString();
  }

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
  public List<SchemaField> getFields() {
    return new ArrayList<>(entryFields.values());
  }

  /**
   * Gets fields.
   *
   * @return the fields
   */
  public Map<String, SchemaField> getFieldMap() {
    return entryFields;
  }

  /**
   * Is extension boolean.
   *
   * @return the boolean
   */
  public boolean isExtension() {
    return entryIsExtension;
  }

  /**
   * Is populated boolean.
   *
   * @return the boolean
   */
  public boolean isPopulated() {
    return entryFields.size() > 0;
  }

  /**
   * Is enum boolean.
   *
   * @return the boolean
   */
  public boolean isEnum() {
    return false;
  }

  /**
   * Is message boolean.
   *
   * @return the boolean
   */
  public boolean isMessage() {
    return false;
  }
}
