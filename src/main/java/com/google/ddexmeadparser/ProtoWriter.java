package com.google.ddexmeadparser;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.common.base.CaseFormat;

public class ProtoWriter {
    private List<String> namespaces;

    public ProtoWriter() {}

    public void serialize(SchemaEntryMap entryMap) throws IOException {
        int version = entryMap.getVersion();
        String rootNamespace = entryMap.getRootNamespacePrefix();
        String packageName = rootNamespace.concat(Integer.toString(version));

        StringBuilder schemaStringBuilder = new StringBuilder();
        schemaStringBuilder.append("syntax = \"proto2\";\n");
        schemaStringBuilder.append("package ").append(packageName).append(";\n");
        namespaces = entryMap.getNamespacePrefixes();
        for (String namespace : namespaces) {
            schemaStringBuilder.append(serializeNamespace(entryMap.getNamespacePrefixEntryMap().get(namespace), namespace));
        }

        writeFile(schemaStringBuilder.toString(), packageName, rootNamespace);
    }

    private String serializeNamespace(List<SchemaAbstractEntry> entries, String namespace) {
        StringBuilder outputBuilder = new StringBuilder();
        entries.sort(Comparator.comparing(SchemaAbstractEntry::getTitle));

        for (SchemaAbstractEntry entry : entries) {
            if (entry.isEnum()) {
                String enumEntry = dumbSerializeEnum((SchemaEnumEntry) entry, namespace);
                outputBuilder.append(enumEntry);
            } else {
                String messageEntry = dumbSerializeMessage((SchemaMessageEntry) entry, namespace);
                outputBuilder.append(messageEntry);
            }
        }

        return(outputBuilder.toString());
    }

    private String dumbSerializeEnum(SchemaEnumEntry entry, String prefix) {
        StringBuilder enumBuilder = new StringBuilder();
        enumBuilder.append("message ").append(prefix).append("_").append(entry.getTitle()).append(" {\n");
        enumBuilder.append("\toptional string enum_value = 1;\n");
        enumBuilder.append("}\n");
        return enumBuilder.toString();
    }

    private String dumbSerializeMessage(SchemaMessageEntry entry, String prefix) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("message ").append(prefix).append("_").append(entry.getTitle()).append(" {\n");

        List<SchemaField> sorted = entry.getFields();
        sorted.sort(Comparator.comparing(SchemaField::getFieldValue));

        int ident = 1; // Messages starting at 1
        for (SchemaField field : sorted) {
            messageBuilder.append("\t");
            if (field.isRepeated()) {
                messageBuilder.append("repeated ");
            } else {
                messageBuilder.append("optional ");
            }
            messageBuilder.append(resolveType(entry, field));
            String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getFieldValue());
            fieldName = sanitizeEnumName(fieldName);
            messageBuilder.append(fieldName).append(" = ").append(ident).append(";\n");
            ident++;
        }
        messageBuilder.append("}\n");
        return messageBuilder.toString();

    }

    private List<String> getImportsForNamespace(String currentNamespace) {
        List<String> nonCurrentNamespaces = new ArrayList<>();
        for (String namespace : namespaces) {
            if (!currentNamespace.equals(namespace) && !namespace.equals("mead") && !namespace.equals("ern")) {
                nonCurrentNamespaces.add(namespace);
            }
        }
        return nonCurrentNamespaces;
    }

    private String resolveType(SchemaMessageEntry entry, SchemaField field) {
        QName fieldType = field.getFieldType();
        String type;

        if (fieldType.getPrefix().equals("xs")) {
            type = convertXmlTypeToProto(fieldType.getLocalPart());
        } else if (!fieldType.getPrefix().equals(entry.getNamespacePrefix())) {
            type = fieldType.getPrefix() + "_" + fieldType.getLocalPart();
        } else {
            type = field.getFieldType().getPrefix() + "_" + fieldType.getLocalPart();
        }

        return type + " ";
    }

    // TODO MAP ALL TYPES - 100
    private String convertXmlTypeToProto(String xmlType) {
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
            default: throw new Error("Unhandled " + xmlType);
        }
    }

    private void writeFile(String toWrite, String packageName, String prefixName) throws IOException {
        File file = new File("./src/main/proto/" + prefixName + "/" + packageName + "/" + prefixName + ".proto");
        file.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(file, false);
        writer.write(toWrite);
        writer.close();
    }

    private String sanitizeEnumName(String fieldName) {
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