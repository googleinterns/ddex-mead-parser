package com.google.ddexmeadparser;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SchemaAbstractEntry {
    String entryTitle;
    String entryNamespacePrefix;
    XmlSchemaAnnotation entryAnnotation;
    Map<String, SchemaField> fields;
    boolean extension;

    public SchemaAbstractEntry(String title, String namespacePrefix) {
        entryTitle = title;
        entryNamespacePrefix = namespacePrefix;
        fields = new HashMap<>();
        extension = false;
        entryAnnotation = null;
    }

    public SchemaAbstractEntry(String title, String namespacePrefix, XmlSchemaAnnotation annotation) {
        entryTitle = title;
        entryNamespacePrefix = namespacePrefix;
        fields = new HashMap<>();
        extension = false;
        entryAnnotation = annotation;
    }

    public void addField(SchemaField entryField) {
        fields.put(entryField.getFieldValue(), entryField);
        if (entryField.getFieldValue().equals("ext_value")) {
            extension = true;
        }
    }

    public void setEntryAnnotation(XmlSchemaAnnotation entryAnnotation) {
        this.entryAnnotation = entryAnnotation;
    }
    public String getTitle() {
        return entryTitle;
    }
    public String getNamespacePrefix() {
        return entryNamespacePrefix;
    }
    public XmlSchemaAnnotation getAnnotation() {
        return entryAnnotation;
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
