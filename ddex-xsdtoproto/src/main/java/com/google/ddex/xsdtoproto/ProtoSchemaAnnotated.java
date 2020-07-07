package com.google.ddex.xsdtoproto;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;

/** The interface Schema annotated. */
public interface ProtoSchemaAnnotated {
  /**
   * Gets annotation.
   *
   * @return the annotation
   */
  String getAnnotation();
  /**
   * Sets annotation.
   *
   * @param annotation the annotation
   */
  void setAnnotation(String annotation);
  /**
   * Sets annotation.
   *
   * @param annotation the annotation
   */
  void setAnnotation(XmlSchemaAnnotation annotation);
}
