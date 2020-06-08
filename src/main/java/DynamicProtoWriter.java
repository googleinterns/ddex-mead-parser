import Utils.ConversionHelper;
import com.google.common.base.CaseFormat;
import com.google.protobuf.DescriptorProtos;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynamicProtoWriter {
    private List<String> namespaces;

    public DynamicProtoWriter() {
    }

    public List<DescriptorProtos.FileDescriptorProto> serialize(EntryContainer entryContainer) throws IOException {
        List<DescriptorProtos.FileDescriptorProto> files = new ArrayList<>();
        namespaces = entryContainer.getNamespacePrefixes();
        for (String namespace : namespaces) {
            DescriptorProtos.FileDescriptorProto f = serializeNamespace(entryContainer.getNamespacePrefixEntryMap().get(namespace), namespace);
            files.add(f);
        }
        return files;
    }

    private DescriptorProtos.FileDescriptorProto serializeNamespace(List<AbstractEntry> entries, String namespace) {
        DescriptorProtos.FileDescriptorProto.Builder fileBuilder = DescriptorProtos.FileDescriptorProto.newBuilder();
        fileBuilder.setName(namespace);
        fileBuilder.setSyntax("proto2");
        fileBuilder.setPackage(namespace);

        for (String namespaceToImport : getImportsForNamespace(namespace)) {
            String pathToImport = namespaceToImport + "/" + namespaceToImport + ".proto";
            fileBuilder.addDependency(pathToImport);
        }

        for (AbstractEntry entry : entries) {
            if (entry.isEnum()) {
                fileBuilder.addEnumType(dumbSerializeEnum((EnumEntry) entry));
            } else {
                fileBuilder.addMessageType(dumbSerializeMessage((MessageEntry) entry));
            }
        }
        DescriptorProtos.FileDescriptorProto done = fileBuilder.build();
        return fileBuilder.build();
    }


    private DescriptorProtos.EnumDescriptorProto.Builder dumbSerializeEnum(EnumEntry entry) {
        DescriptorProtos.EnumDescriptorProto.Builder enumBuilder = DescriptorProtos.EnumDescriptorProto.newBuilder();
        enumBuilder.setName(entry.getTitle());

        int ident = 0;
        for (EntryField field : entry.getFields()) {
            DescriptorProtos.EnumValueDescriptorProto.Builder enumValueBuilder = DescriptorProtos.EnumValueDescriptorProto.newBuilder();

            String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field.getFieldValue());
            fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, entry.getTitle()) + "_" + fieldName;
            fieldName = ConversionHelper.sanitizeEnumName(fieldName);

            enumValueBuilder.setName(fieldName);
            enumValueBuilder.setNumber(ident);
            enumBuilder.addValue(enumValueBuilder);
            ident++;
        }
        return enumBuilder;
    }

    private DescriptorProtos.DescriptorProto.Builder dumbSerializeMessage(MessageEntry entry) {
        DescriptorProtos.DescriptorProto.Builder messageBuilder = DescriptorProtos.DescriptorProto.newBuilder();
        messageBuilder.setName(entry.getTitle());

        int ident = 1;
        for (EntryField field : entry.getFields()) {
            messageBuilder.addField(dumbSerializeMessageField(entry, field, ident++));
        }

        return messageBuilder;
    }

    private DescriptorProtos.FieldDescriptorProto.Builder dumbSerializeMessageField(MessageEntry entry, EntryField field, int ident) {
        DescriptorProtos.FieldDescriptorProto.Builder fieldBuilder = DescriptorProtos.FieldDescriptorProto.newBuilder();

        String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getFieldValue());
        fieldName = ConversionHelper.sanitizeEnumName(fieldName);

        fieldBuilder.setName(fieldName);
        fieldBuilder.setNumber(ident);

        if (field.isRepeated()) {
            fieldBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
        } else {
            fieldBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        }

        if (field.isXmlType()) {
            fieldBuilder.setType(resolveXmlType(field));
        } else {
            fieldBuilder.setTypeName(resolveType(entry, field));
        }

        return fieldBuilder;
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

        return type;
    }

    private DescriptorProtos.FieldDescriptorProto.Type resolveXmlType(EntryField field) {
        switch (field.getFieldType().getLocalPart()) {
            case "string":  return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            case "boolean": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
            case "float": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT;
            case "integer": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
            case "decimal": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE;

            case "dateTime": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64;
            case "anyURI": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            case "NMTOKEN": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            case "positiveInteger": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32;

            case "duration": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            case "token": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;

            case "gYear": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32;
            case "IDREF": return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            default: throw new Error("Unhandled " + field.getFieldType().getLocalPart());
        }
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

    private List<String> getImportsForNamespace(String currentNamespace) {
        List<String> nonCurrentNamespaces = new ArrayList<>();
        for (String namespace : namespaces) {
            if (!currentNamespace.equals(namespace) && !namespace.equals("mead") && !namespace.equals("ern")) {
                nonCurrentNamespaces.add(namespace);
            }
        }
        return nonCurrentNamespaces;
    }

    public void writeFile(String toWrite, String namespace) throws IOException {
        File file = new File("./src/main/proto/" + namespace + "/" + namespace+ ".proto");
        file.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(file, false);
        writer.write(toWrite);
        writer.close();
    }
}
