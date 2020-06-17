package com.google.ddexmeadparser;

public class SchemaEnumEntry extends SchemaAbstractEntry {
    public SchemaEnumEntry(String title, String namespace) {
        super(title, namespace);
    }

    @Override
    public boolean isEnum() { return true; }
}
