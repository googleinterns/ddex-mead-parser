package com.google.ddexmeadparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaEntryMap {
    Map<String, List<SchemaAbstractEntry>> namespacePrefixEntryMap;

    public SchemaEntryMap() {
        namespacePrefixEntryMap = new HashMap<>();
    }

    public void addEntry(SchemaAbstractEntry entry) {
        String prefix = entry.getNamespacePrefix();
        if (!namespacePrefixEntryMap.containsKey(prefix)) {
            namespacePrefixEntryMap.put(prefix, new ArrayList<>());
        }
        namespacePrefixEntryMap.get(prefix).add(entry);
    }

    public List<String> getNamespacePrefixes() {
        return new ArrayList<>(namespacePrefixEntryMap.keySet());
    }

    public Map<String, List<SchemaAbstractEntry>> getNamespacePrefixEntryMap() {
        return namespacePrefixEntryMap;
    }
}
