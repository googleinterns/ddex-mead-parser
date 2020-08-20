package com.google.ddex.xsdtoproto;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;

/**
 * The ProtoSchemaAnnotated interface defines annotation getters and setters for schema entries and
 * fields.
 */
public interface ProtoSchemaAnnotated {
  /**
   * Gets annotation. This annotation stores extracted XML annotations found in the original DDEX
   * XSD.
   *
   * @return The annotation
   */
  String getAnnotation();

  /**
   * Gets version annotation. This annotation pertains to the versioning of each entry and field.
   *
   * @return The version annotation
   */
  String getVersionAnnotation();

  /**
   * Sets annotation.
   *
   * @param annotation The annotation
   */
  void setAnnotation(String annotation);

  /**
   * Sets annotation.
   *
   * @param annotation The annotation
   */
  void setAnnotation(XmlSchemaAnnotation annotation);

  /**
   * Sets version annotation.
   *
   * @param version The version annotation
   */
  void setVersionAnnotation(String version);
}
