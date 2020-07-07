package com.google.ddex.xsdtoproto;

/** The type Schema enum entry. */
public class ProtoSchemaEnumEntry extends ProtoSchemaAbstractEntry {
  /**
   * Instantiates a new Schema enum entry.
   *
   * @param title the title
   * @param namespace the namespace
   */
  public ProtoSchemaEnumEntry(String title, String namespace) {
    super(title, namespace);
  }

  @Override
  public boolean isEnum() {
    return true;
  }
}
