package com.google.ddexmeadparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaImportRegistry {
    Map<String, Map<String, Boolean>> schemaImportMap;

    public SchemaImportRegistry() {
        schemaImportMap = new HashMap<>();
    }

    public void registerImport(String currentNamespace, String importNamespace) {
        if (currentNamespace.equals(importNamespace) || importNamespace.equals("xs")) return;

        schemaImportMap.computeIfAbsent(currentNamespace, k -> new HashMap<>());
        schemaImportMap.get(currentNamespace).put(importNamespace, true);
    }

    public List<String> getImportsForNamespace(String namespace) {
        Map<String, Boolean> importSet = schemaImportMap.get(namespace);
        if (importSet == null) return null;
        return new ArrayList<>(importSet.keySet());
    }
}
