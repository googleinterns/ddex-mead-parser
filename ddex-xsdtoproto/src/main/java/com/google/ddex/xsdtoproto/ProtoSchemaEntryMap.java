package com.google.ddex.xsdtoproto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Schema entry map. */
public class ProtoSchemaEntryMap {
  /** The Namespace prefix entry map. */
  Map<String, Map<String, ProtoSchemaAbstractEntry>> namespacePrefixEntryMap;
  /** The Root namespace prefix. */
  String rootNamespacePrefix;
  /** The Version. */
  int version;

  /** The Import registry. */
  XsdImportRegistry importRegistry;

  /** Instantiates a new Schema entry map. */
  public ProtoSchemaEntryMap() {
    namespacePrefixEntryMap = new HashMap<>();
    importRegistry = new XsdImportRegistry();
  }

  /**
   * Add entry.
   *
   * @param entry the entry
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
   * Sets version.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.version = Integer.parseInt(version);
  }

  /**
   * Sets version.
   *
   * @param version the version
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * Sets root namespace prefix.
   *
   * @param rootNamespacePrefix the root namespace prefix
   */
  public void setRootNamespacePrefix(String rootNamespacePrefix) {
    this.rootNamespacePrefix = rootNamespacePrefix;
  }

  /**
   * Gets root namespace prefix.
   *
   * @return the root namespace prefix
   */
  public String getRootNamespacePrefix() {
    return rootNamespacePrefix;
  }

  /**
   * Gets version.
   *
   * @return the version
   */
  public int getVersion() {
    return version;
  }

  /**
   * Gets namespace prefixes.
   *
   * @return the namespace prefixes
   */
  public List<String> getNamespacePrefixes() {
    return new ArrayList<>(namespacePrefixEntryMap.keySet());
  }

  /**
   * Gets namespace prefix entry map.
   *
   * @return the namespace prefix entry map
   */
  public Map<String, Map<String, ProtoSchemaAbstractEntry>> getNamespacePrefixEntryMap() {
    return namespacePrefixEntryMap;
  }

  /**
   * Gets import registry.
   *
   * @return the import registry
   */
  public XsdImportRegistry getImportRegistry() {
    return importRegistry;
  }

  private void registerImportsFromEntry(ProtoSchemaAbstractEntry entry) {
    List<ProtoSchemaField> fields = entry.getFields();
    for (ProtoSchemaField field : fields) {
      importRegistry.registerImport(entry.getNamespacePrefix(), field.getFieldType().getPrefix());
    }
  }
}
