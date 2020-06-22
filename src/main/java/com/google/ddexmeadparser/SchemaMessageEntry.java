package com.google.ddexmeadparser;

/**
 * The type Schema message entry.
 */
public class SchemaMessageEntry extends SchemaAbstractEntry {
    /**
     * Instantiates a new Schema message entry.
     *
     * @param title the title
     * @param namespace the namespace
     */
public SchemaMessageEntry(String title, String namespace) {
        super(title, namespace);
    }
    @Override
    public boolean isMessage() {
        return true;
    }
}
