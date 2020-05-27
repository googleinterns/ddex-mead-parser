import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

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
    public void serialize(CandidateContainer candidateContainer) {
        namespaces = candidateContainer.getNamespacePrefixes();
        for (String namespace : namespaces) {
            serializeNamespace(candidateContainer.getNamespacePrefixCandidateMap().get(namespace), namespace);
        }
    }

    // Todo package? and file structure for output???
    private void serializeNamespace(List<EntryCandidate> candidates, String namespace) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("syntax = \"proto2\";\n");

        for (String namespaceToImport : getImportsForNamespace(namespace)) {
            outputBuilder.append("import \"").append(namespaceToImport).append("\"\n");
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

        System.out.println(outputBuilder.toString());
    }

    private String dumbSerializeEnum(EnumCandidate candidate) {
        StringBuilder enumBuilder = new StringBuilder();
        enumBuilder.append("enum ").append(candidate.getTitle()).append(" {\n");

        int ident = 0; // Enums starting at 0
        for (ProtoField field : candidate.getFields()) {
            enumBuilder.append("\t").append(field.getFieldValue()).append(" = ").append(ident).append(";\n");
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
            messageBuilder.append(field.getFieldValue()).append(" = ").append(ident).append(";\n");
            ident++;
        }
        messageBuilder.append("}\n");
        return messageBuilder.toString();

    }

    private List<String> getImportsForNamespace(String currentNamespace) {
        List<String> nonCurrentNamespaces = new ArrayList<>();
        for (String namespace : namespaces) {
            if (!currentNamespace.equals(namespace)) {
                nonCurrentNamespaces.add(namespace);
            }
        }
        return nonCurrentNamespaces;
    }

    private String resolveType(MessageCandidate candidate, ProtoField field) {
        QName fieldType = field.getFieldType();
        String type;

        if (fieldType.getPrefix().equals("xs")) {
            type = fieldType.getLocalPart();
        } else if (!fieldType.getPrefix().equals(candidate.getNamespacePrefix())) {
            type = fieldType.getPrefix() + "." + fieldType.getLocalPart();
        } else {
            type = fieldType.getLocalPart();
        }

        return type + " ";
    }
}
