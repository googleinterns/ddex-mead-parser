import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CaseFormat;

public class ProtoWriter {
    private List<String> namespaces;

    public ProtoWriter() {
    }

    // TODO Keeping track of types
    // TODO Type resolution
    // TODO smart file writer
    // TODO 2 pass?
    // TODO camel case / uppercase / snake case conversion using guava thing
    // TODO escape illegal stuff
    public void serialize(CandidateContainer candidateContainer) throws IOException {
        namespaces = candidateContainer.getNamespacePrefixes();
        for (String namespace : namespaces) {
            String toWrite = serializeNamespace(candidateContainer.getNamespacePrefixCandidateMap().get(namespace), namespace);
            writeFile(toWrite, namespace);
        }
    }

    // Todo package? and file structure for output???
    private String serializeNamespace(List<EntryCandidate> candidates, String namespace) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("syntax = \"proto2\";\n");
        outputBuilder.append("package ").append(namespace).append(";\n");
        for (String namespaceToImport : getImportsForNamespace(namespace)) {
            outputBuilder.append("import \"").append(namespaceToImport).append("/").append(namespaceToImport).append(".proto\";\n");
        }

        for (EntryCandidate candidate : candidates) {
            if (candidate.isEnum()) {
                String enumEntry = dumbSerializeEnum((EnumCandidate) candidate);
                outputBuilder.append(enumEntry);
            } else {
                String messageEntry = dumbSerializeMessage((MessageCandidate) candidate);
                outputBuilder.append(messageEntry);
            }
        }

        return(outputBuilder.toString());
    }

    private String dumbSerializeEnum(EnumCandidate candidate) {
        StringBuilder enumBuilder = new StringBuilder();
        enumBuilder.append("enum ").append(candidate.getTitle()).append(" {\n");

        int ident = 0; // Enums starting at 0
        for (ProtoField field : candidate.getFields()) {
            String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field.getFieldValue());
            fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, candidate.getTitle()) + "_" + fieldName;
            fieldName = sanitizeFieldName(fieldName);
            enumBuilder.append("\t").append(fieldName).append(" = ").append(ident).append(";\n");
            ident++;
        }
        enumBuilder.append("}\n");
        return enumBuilder.toString();
    }

    private String dumbSerializeMessage(MessageCandidate candidate) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("message ").append(candidate.getTitle()).append(" {\n");

        int ident = 1; // Messages starting at 1
        for (ProtoField field : candidate.getFields()) {
            messageBuilder.append("\t");
            if (field.isRepeated()) {
                messageBuilder.append("repeated ");
            } else {
                messageBuilder.append("optional ");
            }
            messageBuilder.append(resolveType(candidate, field));
            String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getFieldValue());
            fieldName = sanitizeFieldName(fieldName);
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

    private String resolveType(MessageCandidate candidate, ProtoField field) {
        QName fieldType = field.getFieldType();
        String type;

        if (fieldType.getPrefix().equals("xs")) {
            type = convertXmlTypeToProto(fieldType.getLocalPart());
        } else if (!fieldType.getPrefix().equals(candidate.getNamespacePrefix())) {
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

    private String sanitizeFieldName(String fieldName) {
        fieldName = fieldName.replace("&", "__AND__");
        fieldName = fieldName.replace("/", "__FWSLASH__");
        fieldName = fieldName.replace("-", "__MINUS__");
        fieldName = fieldName.replace("+", "__PLUS__");
        fieldName = fieldName.replace("(", "__FRB__");
        fieldName = fieldName.replace(")", "__BRB__");
        fieldName = fieldName.replace("[", "__FSB__");
        fieldName = fieldName.replace("]", "__BSB__");
        fieldName = fieldName.replace(" ", "__SP__");
        fieldName = fieldName.replace("#", "__SHARP__");
        fieldName = fieldName.replace("!", "__BANG__");
        fieldName = fieldName.replace("ó", "__OI__");
        fieldName = fieldName.replace("í", "__II__");
        fieldName = fieldName.replace(".", "__DOT__");

        return fieldName;

    }

    public void writeFile(String toWrite, String namespace) throws IOException {
        File file = new File("./output/" + namespace + "/" + namespace+ ".proto");
        file.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(file, false);
        writer.write(toWrite);
        writer.close();
    }
}
