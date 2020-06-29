package com.google.ddexmeadparser;

/** The type Schema enum entry. */
public class SchemaEnumEntry extends SchemaAbstractEntry {
  /**
   * Instantiates a new Schema enum entry.
   *
   * @param title the title
   * @param namespace the namespace
   */
  public SchemaEnumEntry(String title, String namespace) {
    super(title, namespace);
  }

  @Override
  public boolean isEnum() {
    return true;
  }
}
