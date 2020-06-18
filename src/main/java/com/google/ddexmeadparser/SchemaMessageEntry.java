package com.google.ddexmeadparser;

public class SchemaMessageEntry extends SchemaAbstractEntry {
    public SchemaMessageEntry(String title, String namespace) {
        super(title, namespace);
    }
    @Override
    public boolean isMessage() {
        return true;
    }
}
