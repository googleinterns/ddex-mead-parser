package com.google.ddexmeadparser;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SchemaAbstractEntry implements SchemaAnnotated {
    String entryTitle;
    String entryNamespacePrefix;
    XmlSchemaAnnotation entryAnnotation;
    Map<String, SchemaField> entryFields;
    boolean entryIsExtension;

    public SchemaAbstractEntry(String title, String namespacePrefix) {
        entryTitle = title;
        entryNamespacePrefix = namespacePrefix;
        entryFields = new HashMap<>();
        entryIsExtension = false;
        entryAnnotation = null;
    }

    public void addField(SchemaField entryField) {
        entryFields.put(entryField.getFieldValue(), entryField);
        if (entryField.getFieldValue().equals("ext_value")) {
            entryIsExtension = true;
        }
    }

    public void setAnnotation(XmlSchemaAnnotation annotation) {
        entryAnnotation = annotation;
    }

    public XmlSchemaAnnotation getAnnotation() {
        return entryAnnotation;
    }

    public String getTitle() {
        return entryTitle;
    }

    public String getNamespacePrefix() {
        return entryNamespacePrefix;
    }

    public List<SchemaField> getFields() {
        return new ArrayList<>(entryFields.values());
    }

    public boolean isExtension() {
        return entryIsExtension;
    }

    public boolean isPopulated() {
        return entryFields.size() > 0;
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isMessage() {
        return false;
    }
}
