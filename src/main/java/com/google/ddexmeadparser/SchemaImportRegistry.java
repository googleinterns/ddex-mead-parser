package com.google.ddexmeadparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Schema import registry. */
public class SchemaImportRegistry {
  /** The Schema import map. */
  Map<String, Map<String, Boolean>> schemaImportMap;

  /** Instantiates a new Schema import registry. */
  public SchemaImportRegistry() {
    schemaImportMap = new HashMap<>();
  }

  /**
   * Register import.
   *
   * @param currentNamespace the current namespace
   * @param importNamespace the import namespace
   */
  public void registerImport(String currentNamespace, String importNamespace) {
    if (currentNamespace.equals(importNamespace) || importNamespace.equals("xs")) return;

    schemaImportMap.computeIfAbsent(currentNamespace, k -> new HashMap<>());
    schemaImportMap.get(currentNamespace).put(importNamespace, true);
  }

  /**
   * Gets imports for namespace.
   *
   * @param namespace the namespace
   * @return the imports for namespace
   */
  public List<String> getImportsForNamespace(String namespace) {
    Map<String, Boolean> importSet = schemaImportMap.get(namespace);
    if (importSet == null) return null;
    return new ArrayList<>(importSet.keySet());
  }
}
