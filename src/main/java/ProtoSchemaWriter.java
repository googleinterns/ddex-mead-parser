import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import Utils.ConversionHelper;

import com.google.common.base.CaseFormat;

public class ProtoSchemaWriter {
    private List<String> namespaces;

    public ProtoSchemaWriter() {
    }

    // TODO Keeping track of types
    // TODO Type resolution
    // TODO smart file writer
    // TODO 2 pass?
    // TODO camel case / uppercase / snake case conversion using guava thing
    // TODO escape illegal stuff
    public void serialize(EntryContainer entryContainer) throws IOException {
        namespaces = entryContainer.getNamespacePrefixes();
        for (String namespace : namespaces) {
            String toWrite = serializeNamespace(entryContainer.getNamespacePrefixEntryMap().get(namespace), namespace);
            writeFile(toWrite, namespace);
        }
    }

    // Todo package? and file structure for output???
    private String serializeNamespace(List<AbstractEntry> entries, String namespace) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("syntax = \"proto2\";\n");
        outputBuilder.append("package ").append(namespace).append(";\n");
        for (String namespaceToImport : getImportsForNamespace(namespace)) {
            outputBuilder.append("import \"").append(namespaceToImport).append("/").append(namespaceToImport).append(".proto\";\n");
        }

        for (AbstractEntry entry : entries) {
            if (entry.isEnum()) {
                String enumEntry = dumbSerializeEnum((EnumEntry) entry);
                outputBuilder.append(enumEntry);
            } else {
                String messageEntry = dumbSerializeMessage((MessageEntry) entry);
                outputBuilder.append(messageEntry);
            }
        }

        return(outputBuilder.toString());
    }

    private String dumbSerializeEnum(EnumEntry entry) {
        StringBuilder enumBuilder = new StringBuilder();
        enumBuilder.append("enum ").append(entry.getTitle()).append(" {\n");

        int ident = 0; // Enums starting at 0
        for (EntryField field : entry.getFields()) {
            String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field.getFieldValue());
            fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, entry.getTitle()) + "_" + fieldName;
            fieldName = ConversionHelper.sanitizeEnumName(fieldName);
            enumBuilder.append("\t").append(fieldName).append(" = ").append(ident).append(";\n");
            ident++;
        }
        enumBuilder.append("}\n");
        return enumBuilder.toString();
    }

    private String dumbSerializeMessage(MessageEntry entry) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("message ").append(entry.getTitle()).append(" {\n");

        int ident = 1; // Messages starting at 1
        for (EntryField field : entry.getFields()) {
            messageBuilder.append("\t");
            if (field.isRepeated()) {
                messageBuilder.append("repeated ");
            } else {
                messageBuilder.append("optional ");
            }
            messageBuilder.append(resolveType(entry, field));
            String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getFieldValue());
            fieldName = ConversionHelper.sanitizeEnumName(fieldName);
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

    private String resolveType(MessageEntry entry, EntryField field) {
        QName fieldType = field.getFieldType();
        String type;

        if (fieldType.getPrefix().equals("xs")) {
            type = convertXmlTypeToProto(fieldType.getLocalPart());
        } else if (!fieldType.getPrefix().equals(entry.getNamespacePrefix())) {
            type = fieldType.getPrefix() + "." + fieldType.getLocalPart();
        } else {
            type = fieldType.getLocalPart();
        }

        return type + " ";
    }

    // TODO MAP ALL TYPES - 100
    private String convertXmlTypeToProto(String xmlType) {
        switch (xmlType) {
            case "string": return "string";
            case "boolean": return "bool";
            case "float": return "float";
            case "integer": return "int32";
            case "decimal": return "double";

            case "dateTime": return "uint64";
            case "anyURI": return "string";
            case "NMTOKEN": return "string";
            case "positiveInteger": return "uint32";

            case "duration": return "string";
            case "token": return "string";

            case "gYear": return "uint32";
            case "IDREF": return "string";
            default: throw new Error("Unhandled " + xmlType);
        }
    }

    private void writeFile(String toWrite, String namespace) throws IOException {
        File file = new File("./src/main/proto/" + namespace + "/" + namespace+ ".proto");
        file.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(file, false);
        writer.write(toWrite);
        writer.close();
    }
}