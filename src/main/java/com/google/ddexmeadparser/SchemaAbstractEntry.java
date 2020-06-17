package com.google.ddexmeadparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaAbstractEntry {
    String title;
    String namespacePrefix;
    Map<String, SchemaField> fields;
    boolean extension;

    public SchemaAbstractEntry(String entryTitle, String entryNamespacePrefix) {
        title = entryTitle;
        namespacePrefix = entryNamespacePrefix;
        fields = new HashMap<>();
        extension = false;
    }

    public void addField(SchemaField entryField) {
        fields.put(entryField.getFieldValue(), entryField);

        if (entryField.getFieldValue().equals("ext_value")) {
            extension = true;
        }
    }

    public String getTitle() {
        return title;
    }
    public String getNamespacePrefix() {
        return namespacePrefix;
    }
    public List<SchemaField> getFields() {
        return new ArrayList<>(fields.values());
    }
    public boolean isExtension() {
        return extension;
    }
    public boolean hasFields() {
        return fields.size() > 0;
    }
    public boolean isEnum() {
        return false;
    }
    public boolean isMessage() {
        return false;
    }
}
