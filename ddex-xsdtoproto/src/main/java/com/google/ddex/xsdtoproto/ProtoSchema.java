package com.google.ddex.xsdtoproto;

import com.google.common.base.CaseFormat;

import javax.xml.namespace.QName;
import java.util.ArrayList;
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
  private XsdImportRegistry importRegistry;
  private final ProtoSchemaEntryMap protoSchemaEntryMap;

  /**
   * Instantiates a new Proto schema.
   *
   * @param entryMap the entry map
   * @throws XsdParseException the schema conversion exception
   */
  public ProtoSchema(ProtoSchemaEntryMap entryMap) throws XsdParseException {
    protoSchemaEntryMap = entryMap;
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

  /**
   * Gets schema entry map.
   *
   * @return the schema entry map
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
    enumStringBuilder.append(serializeVersion(entry));
    enumStringBuilder.append(serializeAnnotation(entry));
    enumStringBuilder.append("message ").append(entry.getTitle()).append(" {\n");
    enumStringBuilder.append("\toptional string enum_value = 1;\n");
    enumStringBuilder.append("}\n\n");
    return enumStringBuilder.toString();
  }

  private String serializeMessage(ProtoSchemaMessageEntry entry, String prefix)
      throws XsdParseException {
    StringBuilder messageStringBuilder = new StringBuilder();
    messageStringBuilder.append(serializeVersion(entry));
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
      fieldSetStringBuilder.append("\t");
      fieldSetStringBuilder.append(serializeVersion(field));
      fieldSetStringBuilder.append("\t");
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
      fieldSetStringBuilder.append(fieldName).append(" = ").append(numerator);

      if (field.isDeprecated()) {
        fieldSetStringBuilder.append(" [deprecated = true]");
      }

      fieldSetStringBuilder.append(";\n");
      numerator++;
    }

    return fieldSetStringBuilder.toString();
  }

  private String serializeVersion(ProtoSchemaAnnotated annotated) {
    String version = annotated.getVersion();
    if (version == null || version.isEmpty()) {
      return "";
    }
    return "/* Source: " + version + " */\n";
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
