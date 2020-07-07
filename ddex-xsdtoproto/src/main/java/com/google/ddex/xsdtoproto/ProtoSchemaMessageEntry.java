package com.google.ddex.xsdtoproto;

/** The type Schema message entry. */
public class ProtoSchemaMessageEntry extends ProtoSchemaAbstractEntry {
  /**
   * Instantiates a new Schema message entry.
   *
   * @param title the title
   * @param namespace the namespace
   */
  public ProtoSchemaMessageEntry(String title, String namespace) {
    super(title, namespace);
  }

  @Override
  public boolean isMessage() {
    return true;
  }
}
