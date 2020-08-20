package com.google.ddex.xsdtoproto;

/**
 * The ProtoSchemaMessageEntry extends the {@link ProtoSchemaAbstractEntry} class to represent all
 * defined message types.
 */
public class ProtoSchemaMessageEntry extends ProtoSchemaAbstractEntry {
  /**
   * Instantiates a new message type by name and the containing namespace.
   *
   * @param title The name of the message type.
   * @param namespace The namespace prefix that contains this entry.
   */
  public ProtoSchemaMessageEntry(String title, String namespace) {
    super(title, namespace);
  }

  @Override
  public boolean isMessage() {
    return true;
  }
}
