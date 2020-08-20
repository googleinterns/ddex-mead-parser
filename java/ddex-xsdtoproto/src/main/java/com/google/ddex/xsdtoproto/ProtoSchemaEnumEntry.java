package com.google.ddex.xsdtoproto;

/**
 * The ProtoSchemaEnumEntry extends the {@link ProtoSchemaAbstractEntry} class to represent all
 * defined enum types.
 */
public class ProtoSchemaEnumEntry extends ProtoSchemaAbstractEntry {
  /**
   * Instantiates a new enum type by name and the containing namespace.
   *
   * @param title The name of the enum type.
   * @param namespace The namespace prefix that contains this entry.
   */
  public ProtoSchemaEnumEntry(String title, String namespace) {
    super(title, namespace);
  }

  @Override
  public boolean isEnum() {
    return true;
  }
}
