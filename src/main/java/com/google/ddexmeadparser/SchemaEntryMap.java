package com.google.ddexmeadparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Schema entry map. */
public class SchemaEntryMap {
  /** The Namespace prefix entry map. */
  Map<String, List<SchemaAbstractEntry>> namespacePrefixEntryMap;
  /** The Root namespace prefix. */
  String rootNamespacePrefix;
  /** The Version. */
  int version;

  SchemaImportRegistry importRegistry;

  /** Instantiates a new Schema entry map. */
  public SchemaEntryMap() {
    namespacePrefixEntryMap = new HashMap<>();
    importRegistry = new SchemaImportRegistry();
  }

  /**
   * Add entry.
   *
   * @param entry the entry
   */
  public void addEntry(SchemaAbstractEntry entry) {
    String prefix = entry.getNamespacePrefix();
    if (!namespacePrefixEntryMap.containsKey(prefix)) {
      namespacePrefixEntryMap.put(prefix, new ArrayList<>());
    }
    namespacePrefixEntryMap.get(prefix).add(entry);
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
  public Map<String, List<SchemaAbstractEntry>> getNamespacePrefixEntryMap() {
    return namespacePrefixEntryMap;
  }

  public SchemaImportRegistry getImportRegistry() {
    return importRegistry;
  }

  private void registerImportsFromEntry(SchemaAbstractEntry entry) {
    List<SchemaField> fields = entry.getFields();
    for (SchemaField field : fields) {
      importRegistry.registerImport(entry.getNamespacePrefix(), field.getFieldType().getPrefix());
    }
  }
}
