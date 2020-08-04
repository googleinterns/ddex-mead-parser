package com.google.ddex.xsdtoproto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ProtoSchemaEntryMap is the container for the types parsed from the DDEX XSD. Stores schema
 * metadata and individual Protocol Buffer message type definitions ({@link
 * ProtoSchemaAbstractEntry}'s)
 */
public class ProtoSchemaEntryMap {
  Map<String, Map<String, ProtoSchemaAbstractEntry>> namespacePrefixEntryMap;
  String rootNamespacePrefix;
  int version;
  XsdImportRegistry importRegistry;

  /** Instantiates a new Schema entry map. */
  public ProtoSchemaEntryMap() {
    namespacePrefixEntryMap = new HashMap<>();
    importRegistry = new XsdImportRegistry();
  }

  /**
   * Add entry. Each entry will correspond to a message type in the final .proto schema output.
   *
   * @param entry The entry
   */
  public void addEntry(ProtoSchemaAbstractEntry entry) {
    String prefix = entry.getNamespacePrefix();
    if (!namespacePrefixEntryMap.containsKey(prefix)) {
      namespacePrefixEntryMap.put(prefix, new HashMap<>());
    }
    namespacePrefixEntryMap.get(prefix).put(entry.getTitle(), entry);
    registerImportsFromEntry(entry);
  }

  /**
   * Sets version. The version number refers to the version of the DDEX schema - ERN version 4.1.1
   * would be * "411".
   *
   * @param version The version number
   */
  public void setVersion(String version) {
    this.version = Integer.parseInt(version);
  }

  /**
   * Sets version. The version number refers to the version of the DDEX schema - ERN version 4.1.1
   * would be * "411".
   *
   * @param version The version number
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * Sets root namespace prefix. In a set of XSD files with their own unique namespaces ("mead" +
   * "avs") * we need to define the "mead" namespace as the root/entry point of the schema
   *
   * @param rootNamespacePrefix The root namespace prefix
   */
  public void setRootNamespacePrefix(String rootNamespacePrefix) {
    this.rootNamespacePrefix = rootNamespacePrefix;
  }

  /**
   * Gets root namespace prefix.
   *
   * @return The root namespace prefix
   */
  public String getRootNamespacePrefix() {
    return rootNamespacePrefix;
  }

  /**
   * Gets version.
   *
   * @return The version
   */
  public int getVersion() {
    return version;
  }

  /**
   * Gets a list of the namespaces in the schema.
   *
   * @return The namespace prefixes
   */
  public List<String> getNamespacePrefixes() {
    return new ArrayList<>(namespacePrefixEntryMap.keySet());
  }

  /**
   * Gets a nested Map of all the entries in the schema. The top level maps the namespaces to the
   * second level map. The second level maps entry names to the {@link ProtoSchemaAbstractEntry}'s
   * themselves
   *
   * @return The namespace prefix entry map
   */
  public Map<String, Map<String, ProtoSchemaAbstractEntry>> getNamespacePrefixEntryMap() {
    return namespacePrefixEntryMap;
  }

  XsdImportRegistry getImportRegistry() {
    return importRegistry;
  }

  void registerImportsFromEntry(ProtoSchemaAbstractEntry entry) {
    List<ProtoSchemaField> fields = entry.getFields();
    for (ProtoSchemaField field : fields) {
      importRegistry.registerImport(entry.getNamespacePrefix(), field.getFieldType().getPrefix());
    }
  }
}
