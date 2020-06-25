package com.google.ddexmeadparser;

import com.google.common.base.CaseFormat;

import javax.xml.namespace.QName;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Proto schema. */
public class ProtoSchema {
    private String rootNamespace;
    private String packageName;
    private int versionNumber;
    private final Map<String, String> schemaStringMap;
    private List<String> namespaces;
    private SchemaImportRegistry importRegistry;

    /**
     * Instantiates a new Proto schema.
     *
     * @param entryMap the entry map
     * @throws SchemaConversionException the schema conversion exception
     */
    public ProtoSchema(SchemaEntryMap entryMap) throws SchemaConversionException {
        schemaStringMap = new HashMap<>();
        serialize(entryMap);
    }

    /**
     * Gets root namespace.
     *
     * @return the root namespace
     */
    public String getRootNamespace() {
        return rootNamespace;
    }

    /**
     * Gets package name.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets version number.
     *
     * @return the version number
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Gets schema string.
     *
     * @return the schema string
     */
    public Map<String, String> getSchemaStringMap() {
        return schemaStringMap;
    }

    private void serialize(SchemaEntryMap entryMap) throws SchemaConversionException {
        versionNumber = entryMap.getVersion();
        rootNamespace = entryMap.getRootNamespacePrefix();
        packageName = rootNamespace + versionNumber;

        namespaces =  entryMap.getNamespacePrefixes();
        importRegistry = entryMap.getImportRegistry();

        for (String namespace : namespaces) {
            List<SchemaAbstractEntry> entries = entryMap.getNamespacePrefixEntryMap().get(namespace);

            schemaStringMap.put(namespace, serializeNamespace(entries, namespace));
        }
    }

    private String serializeNamespace(List<SchemaAbstractEntry> entries, String namespace)
            throws SchemaConversionException {

        StringBuilder namespaceStringBuilder = new StringBuilder();
        namespaceStringBuilder
                .append("/* Generated schema for ")
                .append(namespace)
                .append(", version ")
                .append(packageName)
                .append(" */\n\n");
        entries.sort(Comparator.comparing(SchemaAbstractEntry::getTitle));

        namespaceStringBuilder.append("syntax = \"proto2\";\n");
        namespaceStringBuilder.append("package ").append(packageName).append(".").append(namespace).append(";\n\n");

        List<String> imports = importRegistry.getImportsForNamespace(namespace);
        if (imports != null) {
            for (String toImport : imports) {
                if (!toImport.equals(rootNamespace) && !toImport.equals(namespace)) {
                    namespaceStringBuilder.append("import \"").append(toImport).append(".proto\";\n");
                }
            }
            namespaceStringBuilder.append('\n');
        }


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
        enumStringBuilder
                .append("message ")
                .append(entry.getTitle())
                .append(" {\n");
        enumStringBuilder.append("\toptional string enum_value = 1;\n");
        enumStringBuilder.append("}\n\n");
        return enumStringBuilder.toString();
    }

    private String serializeMessage(SchemaMessageEntry entry, String prefix)
            throws SchemaConversionException {
        StringBuilder messageStringBuilder = new StringBuilder();
        messageStringBuilder.append(serializeAnnotation(entry));
        messageStringBuilder
                .append("message ")
                .append(entry.getTitle())
                .append(" {\n");
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
            String fieldName =
                    sanitizeProtoName(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getFieldValue()));
            fieldSetStringBuilder.append(fieldName).append(" = ").append(numerator).append(";\n");
            numerator++;
        }

        return fieldSetStringBuilder.toString();
    }

    private String serializeAnnotation(SchemaAnnotated annotated) {
        String annotation = annotated.getAnnotation();
        if (annotation == null || annotation.isEmpty()) {
            return "";
        }
        return "/* " + annotation + " */\n";
    }

    private String resolveType(SchemaMessageEntry entry, SchemaField field) throws SchemaConversionException {
        QName fieldType = field.getFieldType();
        String type;

        if (field.isXmlType()) {
            type = convertXmlTypeToProto(fieldType.getLocalPart());
        } else if (!fieldType.getPrefix().equals(entry.getNamespacePrefix())) {
            type = packageName + "." + fieldType.getPrefix() + "." + fieldType.getLocalPart();
        } else {
            type = fieldType.getLocalPart();
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
                throw new SchemaConversionException("Unhandled Xml type mapping for: " + xmlType);
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
