package com.google.ddex.xsdtoproto;

import com.google.common.base.CaseFormat;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The ProtoSchema is an internal representation of a Protocol Buffer schema. This class acts as wrapper on top of a {@link ProtoSchemaEntryMap} to preserve schema metadata and
 * to serialize the entries to a .proto format string.
 */
public class ProtoSchema {
  private String rootNamespace;
  private String packageName;
  private int versionNumber;
  private final Map<String, String> schemaStringMap;
  private XsdImportRegistry importRegistry;
  private final ProtoSchemaEntryMap protoSchemaEntryMap;

  /**
   * Instantiates a new ProtoSchema using a {@link ProtoSchemaEntryMap}.
   *
   * @param entryMap The entry map
   * @throws XsdParseException If any problems occur attempting to serialize the entryMap
   */
  public ProtoSchema(ProtoSchemaEntryMap entryMap) throws XsdParseException {
    protoSchemaEntryMap = entryMap;
    schemaStringMap = new HashMap<>();
    serialize(entryMap);
  }

  /**
   * Gets root namespace. In a set of XSD files with their own unique namespaces ("mead" + "avs")
   * we need to define the "mead" namespace as the root/entry point of the ProtoSchema
   *
   * @return The DDEX schema's root namespace
   */
  public String getRootNamespace() {
    return rootNamespace;
  }

  /**
   * Gets package name. The package name is a custom identifier consisting of the
   * rootNamespace+versionNumber (mead101) that is used to separate the different schema's usage at runtime in the
   * {@link com.google.ddex.xmltoproto.MessageBuilderResolver}
   *
   * @return The DDEX schema's package name
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Gets version number. The version number refers to the version of the DDEX schema - ERN version 4.1.1 would be
   * "411"
   *
   * @return The DDEX schema's version number
   */
  public int getVersionNumber() {
    return versionNumber;
  }

  /**
   * Gets a map of all the .proto schemas in string format. Each entry can be written to a file as a .proto.
   *
   * @return A Map of all the serialized .proto files as strings.
   */
  public Map<String, String> getSchemaStringMap() {
    return schemaStringMap;
  }

  /**
   * Gets schema entry map. Allows the original {@link ProtoSchemaEntryMap} to be accessed.
   *
   * @return The original entry map
   */
  public ProtoSchemaEntryMap getProtoSchemaEntryMap() {
    return protoSchemaEntryMap;
  }

  private void serialize(ProtoSchemaEntryMap entryMap) throws XsdParseException {
    versionNumber = entryMap.getVersion();
    rootNamespace = entryMap.getRootNamespacePrefix();
    packageName = rootNamespace + versionNumber;

    List<String> namespaces = entryMap.getNamespacePrefixes();
    importRegistry = entryMap.getImportRegistry();

    for (String namespace : namespaces) {
      List<ProtoSchemaAbstractEntry> entries =
          new ArrayList<>(entryMap.getNamespacePrefixEntryMap().get(namespace).values());
      schemaStringMap.put(namespace, serializeNamespace(entries, namespace));
    }
  }

  private String serializeNamespace(List<ProtoSchemaAbstractEntry> entries, String namespace)
      throws XsdParseException {

    StringBuilder namespaceStringBuilder = new StringBuilder();
    namespaceStringBuilder
        .append("/* Generated schema for ")
        .append(namespace)
        .append(", version ")
        .append(packageName)
        .append(" */\n");

    entries.sort(Comparator.comparing(ProtoSchemaAbstractEntry::getTitle));

    namespaceStringBuilder.append("syntax = \"proto2\";\n");
    namespaceStringBuilder
        .append("package ")
        .append(packageName)
        .append(".")
        .append(namespace)
        .append(";\n\n");
    namespaceStringBuilder.append(serializeImports(namespace));

    for (ProtoSchemaAbstractEntry entry : entries) {
      if (entry instanceof ProtoSchemaEnumEntry) {
        namespaceStringBuilder.append(serializeEnum((ProtoSchemaEnumEntry) entry, namespace));
      } else if (entry instanceof ProtoSchemaMessageEntry) {
        namespaceStringBuilder.append(serializeMessage((ProtoSchemaMessageEntry) entry, namespace));
      }
    }

    return namespaceStringBuilder.toString();
  }

  private String serializeImports(String namespace) {
    StringBuilder importStringBuilder = new StringBuilder();
    List<String> imports = importRegistry.getImportsForNamespace(namespace);
    if (imports != null) {
      for (String toImport : imports) {
        if (!toImport.equals(rootNamespace) && !toImport.equals(namespace)) {
          importStringBuilder.append("import \"").append(toImport).append(".proto\";\n");
        }
      }
      importStringBuilder.append('\n');
    }
    return importStringBuilder.toString();
  }

  private String serializeEnum(ProtoSchemaEnumEntry entry, String prefix) {
    StringBuilder enumStringBuilder = new StringBuilder();
    enumStringBuilder.append(serializeMessageVersion(entry));
    enumStringBuilder.append(serializeAnnotation(entry));
    enumStringBuilder.append("message ").append(entry.getTitle()).append(" {\n");
    enumStringBuilder.append("\toptional string enum_value = 1;\n");
    enumStringBuilder.append("}\n\n");
    return enumStringBuilder.toString();
  }

  private String serializeMessage(ProtoSchemaMessageEntry entry, String prefix)
      throws XsdParseException {
    StringBuilder messageStringBuilder = new StringBuilder();
    messageStringBuilder.append(serializeMessageVersion(entry));
    messageStringBuilder.append(serializeAnnotation(entry));
    messageStringBuilder.append("message ").append(entry.getTitle()).append(" {\n");
    messageStringBuilder.append(serializeFieldSet(entry));
    messageStringBuilder.append("}\n\n");
    return messageStringBuilder.toString();
  }

  private String serializeFieldSet(ProtoSchemaMessageEntry entry) throws XsdParseException {
    StringBuilder fieldSetStringBuilder = new StringBuilder();

    List<ProtoSchemaField> fields = entry.getFields();
    fields.sort(Comparator.comparing(ProtoSchemaField::getFieldValue));

    int numerator = 1;
    for (ProtoSchemaField field : fields) {
      if (field.getVersionAnnotation() != null && !field.getVersionAnnotation().isEmpty()) {
        fieldSetStringBuilder.append("\t");
        fieldSetStringBuilder.append(serializeFieldVersion(field));
      }
      if (field.getAnnotation() != null && !field.getAnnotation().isEmpty()) {
        fieldSetStringBuilder.append("\t");
        fieldSetStringBuilder.append(serializeAnnotation(field));
      }
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
      fieldSetStringBuilder.append(fieldName).append(" = ").append(numerator);

      if (field.isDeprecated()) {
        fieldSetStringBuilder.append(" [deprecated = true]");
      }

      fieldSetStringBuilder.append(";\n");
      numerator++;
    }

    return fieldSetStringBuilder.toString();
  }

  private String serializeMessageVersion(ProtoSchemaAnnotated annotated) {
    String version = annotated.getVersionAnnotation();
    if (version == null || version.isEmpty()) {
      return "";
    }
    return "/* Defined in: " + version + " */\n";
  }

  private String serializeFieldVersion(ProtoSchemaAnnotated annotated) {
    String version = annotated.getVersionAnnotation();
    if (version == null || version.isEmpty()) {
      return "";
    }
    return "/* First defined in: " + version + " */\n";
  }

  private String serializeAnnotation(ProtoSchemaAnnotated annotated) {
    String annotation = annotated.getAnnotation();
    if (annotation == null || annotation.isEmpty()) {
      return "";
    }
    return "/* " + annotation + " */\n";
  }

  private String resolveType(ProtoSchemaMessageEntry entry, ProtoSchemaField field)
      throws XsdParseException {
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

  private String convertXmlTypeToProto(String xmlType) throws XsdParseException {
    switch (xmlType) {
      case "boolean":
        return "bool";
      case "float":
        return "float";
      case "integer":
        return "int32";
      case "decimal":
        return "double";
      case "positiveInteger":
      case "gYear":
        return "uint32";
      case "dateTime":
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
        throw new XsdParseException("Unhandled Xml type mapping for: " + xmlType);
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
