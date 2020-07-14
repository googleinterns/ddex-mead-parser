package com.google.ddex.xsdtoproto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Schema import registry. */
class XsdImportRegistry {
  /** The Schema import map. */
  Map<String, Map<String, Boolean>> schemaImportMap;

  /** Instantiates a new Schema import registry. */
  XsdImportRegistry() {
    schemaImportMap = new HashMap<>();
  }

  /**
   * Register import.
   *
   * @param currentNamespace the current namespace
   * @param importNamespace the import namespace
   */
  void registerImport(String currentNamespace, String importNamespace) {
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
  List<String> getImportsForNamespace(String namespace) {
    Map<String, Boolean> importSet = schemaImportMap.get(namespace);
    if (importSet == null) return null;
    return new ArrayList<>(importSet.keySet());
  }
}
