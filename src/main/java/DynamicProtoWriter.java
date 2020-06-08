import Utils.ConversionHelper;
import com.google.common.base.CaseFormat;
import com.google.protobuf.DescriptorProtos;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class DynamicProtoWriter {
    private List<String> namespaces;

    public DynamicProtoWriter() { }

    public List<DescriptorProtos.FileDescriptorProto> buildDescriptor(EntryContainer entryContainer) {
        List<DescriptorProtos.FileDescriptorProto> files = new ArrayList<>();
        namespaces = entryContainer.getNamespacePrefixes();
        for (String namespace : namespaces) {
            DescriptorProtos.FileDescriptorProto file = buildNamespace(entryContainer.getNamespacePrefixEntryMap().get(namespace), namespace);
            files.add(file);
        }
        return files;
    }

    private DescriptorProtos.FileDescriptorProto buildNamespace(List<AbstractEntry> entries, String namespace) {
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
                fileBuilder.addEnumType(buildEnum((EnumEntry) entry));
            } else {
                fileBuilder.addMessageType(buildMessage((MessageEntry) entry));
            }
        }
        return fileBuilder.build();
    }


    private DescriptorProtos.EnumDescriptorProto.Builder buildEnum(EnumEntry entry) {
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

    private DescriptorProtos.DescriptorProto.Builder buildMessage(MessageEntry entry) {
        DescriptorProtos.DescriptorProto.Builder messageBuilder = DescriptorProtos.DescriptorProto.newBuilder();
        messageBuilder.setName(entry.getTitle());

        int ident = 1;
        for (EntryField field : entry.getFields()) {
            messageBuilder.addField(buildMessageField(entry, field, ident++));
        }

        return messageBuilder;
    }

    private DescriptorProtos.FieldDescriptorProto.Builder buildMessageField(MessageEntry entry, EntryField field, int ident) {
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
            fieldBuilder.setTypeName(resolveCustomType(entry, field));
        }

        return fieldBuilder;
    }

    private String resolveCustomType(MessageEntry entry, EntryField field) {
        QName fieldType = field.getFieldType();
        String type;

        if (!fieldType.getPrefix().equals(entry.getNamespacePrefix())) {
            type = fieldType.getPrefix() + "." + fieldType.getLocalPart();
        } else {
            type = fieldType.getLocalPart();
        }

        return type;
    }

    private DescriptorProtos.FieldDescriptorProto.Type resolveXmlType(EntryField field) {
        switch (field.getFieldType().getLocalPart()) {
            case "string":
            case "NMTOKEN":
            case "anyURI":
            case "token":
            case "duration":
            case "IDREF":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            case "positiveInteger":
            case "gYear":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32;
            case "boolean":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
            case "float":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT;
            case "integer":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
            case "decimal":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE;
            case "dateTime":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64;
            default:
                throw new Error("Unhandled " + field.getFieldType().getLocalPart());
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
}
