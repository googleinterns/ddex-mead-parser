package com.google.ddexmeadparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaEntryMap {
    Map<String, List<SchemaAbstractEntry>> namespacePrefixEntryMap;
    String rootNamespacePrefix;
    int version;

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

    public void setVersion(String version) {
        this.version = Integer.parseInt(version);
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setRootNamespacePrefix(String rootNamespacePrefix) {
        this.rootNamespacePrefix = rootNamespacePrefix;
    }

    public String getRootNamespacePrefix() {
        return rootNamespacePrefix;
    }

    public int getVersion() {
        return version;
    }

    public List<String> getNamespacePrefixes() {
        return new ArrayList<>(namespacePrefixEntryMap.keySet());
    }

    public Map<String, List<SchemaAbstractEntry>> getNamespacePrefixEntryMap() {
        return namespacePrefixEntryMap;
    }
}
