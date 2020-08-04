package com.google.ddex.xsdtoproto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class XsdImportRegistry {
  final Map<String, Map<String, Boolean>> schemaImportMap;

  XsdImportRegistry() {
    schemaImportMap = new HashMap<>();
  }

  void registerImport(String currentNamespace, String importNamespace) {
    if (currentNamespace.equals(importNamespace) || importNamespace.equals("xs")) {
      return;
    }

    schemaImportMap.computeIfAbsent(currentNamespace, k -> new HashMap<>());
    schemaImportMap.get(currentNamespace).put(importNamespace, true);
  }

  List<String> getImportsForNamespace(String namespace) {
    Map<String, Boolean> importSet = schemaImportMap.getOrDefault(namespace, null);
    if (importSet == null) {
      return null;
    }
    return new ArrayList<>(importSet.keySet());
  }
}
