package com.google.ddexmeadparser;

import com.google.common.base.CaseFormat;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.Comparator;
import java.util.List;

public class ProtoSchema {
    private String rootNamespace;
    private String packageName;
    private int versionNumber;
    private final String schemaString;

    public ProtoSchema(SchemaEntryMap entryMap) throws SchemaConversionException {
        schemaString = serialize(entryMap);
    }

    public String getRootNamespace() {
        return rootNamespace;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getSchemaString() {
        return schemaString;
    }

    private String serialize(SchemaEntryMap entryMap) throws SchemaConversionException {
        versionNumber = entryMap.getVersion();
        rootNamespace = entryMap.getRootNamespacePrefix();
        packageName = rootNamespace + versionNumber;

        StringBuilder schemaStringBuilder = new StringBuilder();
        schemaStringBuilder.append("syntax = \"proto2\";\n");
        schemaStringBuilder.append("package ").append(packageName).append(";\n\n");
        List<String> namespaces = entryMap.getNamespacePrefixes();

        for (String namespace : namespaces) {
            List<SchemaAbstractEntry> entries = entryMap.getNamespacePrefixEntryMap().get(namespace);
            schemaStringBuilder.append(serializeNamespace(entries, namespace));
        }

        return schemaStringBuilder.toString();
    }

    private String serializeNamespace(List<SchemaAbstractEntry> entries, String namespace) throws SchemaConversionException {
        StringBuilder namespaceStringBuilder = new StringBuilder();
        entries.sort(Comparator.comparing(SchemaAbstractEntry::getTitle));

        for (SchemaAbstractEntry entry : entries) {
            if (entry instanceof SchemaEnumEntry) {
                namespaceStringBuilder.append(serializeEnum((SchemaEnumEntry) entry, namespace));
            } else if (entry instanceof SchemaMessageEntry) {
                namespaceStringBuilder.append(serializeMessage((SchemaMessageEntry) entry, namespace));
            }
        }
        return namespaceStringBuilder.toString();
    }

    private String serializeEnum(SchemaEnumEntry entry, String prefix) {
        StringBuilder enumStringBuilder = new StringBuilder();
        enumStringBuilder.append(serializeAnnotation(entry));
        enumStringBuilder.append("message ").append(prefix).append("_").append(entry.getTitle()).append(" {\n");
        enumStringBuilder.append("\toptional string enum_value = 1;\n");
        enumStringBuilder.append("}\n\n");
        return enumStringBuilder.toString();
    }

    private String serializeMessage(SchemaMessageEntry entry, String prefix) throws SchemaConversionException {
        StringBuilder messageStringBuilder = new StringBuilder();
        messageStringBuilder.append(serializeAnnotation(entry));
        messageStringBuilder.append("message ").append(prefix).append("_").append(entry.getTitle()).append(" {\n");
        messageStringBuilder.append(serializeFieldSet(entry));
        messageStringBuilder.append("}\n\n");
        return messageStringBuilder.toString();

    }

    private String serializeFieldSet(SchemaMessageEntry entry) throws SchemaConversionException {
        StringBuilder fieldSetStringBuilder = new StringBuilder();

        List<SchemaField> fields = entry.getFields();
        fields.sort(Comparator.comparing(SchemaField::getFieldValue));

        int numerator = 1;
        for (SchemaField field : fields) {
            fieldSetStringBuilder.append(serializeAnnotation(field));
            fieldSetStringBuilder.append("\t");
            if (field.isRepeated()) {
                fieldSetStringBuilder.append("repeated ");
            } else {
                fieldSetStringBuilder.append("optional ");
            }
            fieldSetStringBuilder.append(resolveType(entry, field)).append(' ');
            String fieldName = sanitizeProtoName(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getFieldValue()));
            fieldSetStringBuilder.append(fieldName).append(" = ").append(numerator).append(";\n");
            numerator++;
        }

        return fieldSetStringBuilder.toString();
    }

    private String serializeAnnotation(SchemaAnnotated annotated) {
        XmlSchemaAnnotation annotation = annotated.getAnnotation();
        if (annotation == null) {
            return "";
        }

        StringBuilder annotationStringBuilder = new StringBuilder();
        annotationStringBuilder.append("/* ");
        for (int i = 0; i < annotation.getItems().size(); i++) {
            XmlSchemaDocumentation documentation = (XmlSchemaDocumentation) annotation.getItems().get(i);
            NodeList markup = documentation.getMarkup();
            for (int j = 0; j < markup.getLength(); j++) {
                annotationStringBuilder.append(markup.item(j).getTextContent());
                if (j != markup.getLength() - 1) annotationStringBuilder.append('\n');
            }
        }
        annotationStringBuilder.append(" */\n");
        return annotationStringBuilder.toString();
    }

    private String resolveType(SchemaMessageEntry entry, SchemaField field) throws SchemaConversionException {
        QName fieldType = field.getFieldType();
        String type;

        if (field.isXmlType()) {
            type = convertXmlTypeToProto(fieldType.getLocalPart());
        } else {
            type = fieldType.getPrefix() + "_" + fieldType.getLocalPart();
        }

        return type;
    }

    private String convertXmlTypeToProto(String xmlType) throws SchemaConversionException {
        switch (xmlType) {
            case "boolean":
                return "bool";
            case "float":
                return "float";
            case "integer":
                return "int32";
            case "decimal":
                return "double";
            case "dateTime":
                return "uint64";
            case "positiveInteger":
            case "gYear":
                return "uint32";
            case "string":
            case "anyURI":
            case "NMTOKEN":
            case "duration":
            case "token":
            case "IDREF":
            case "date":
            case "ID":
                return "string";
            default:
                throw new SchemaConversionException("Unhandled XmlType mapping in serialize: " + xmlType);
        }
    }

    private String sanitizeProtoName(String fieldName) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
                stringBuilder.append(c);
            } else {
                stringBuilder.append("__").append(Integer.toString(c)).append("__");
            }
        }
        return stringBuilder.toString();
    }
}
