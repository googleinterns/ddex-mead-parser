package com.google.ddexmeadparser;

import org.apache.ws.commons.schema.utils.NamespacePrefixList;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Schema namespace map.
 */
public class SchemaNamespaceMap {
    private final Map<String, String> prefixToUriMap;
    private final Map<String, String> uriToPrefixMap;

    /**
     * Instantiates a new Schema namespace map.
     */
public SchemaNamespaceMap() {
        prefixToUriMap = new HashMap<>();
        uriToPrefixMap = new HashMap<>();
    }

    /**
     * Populate from context.
     *
     * @param prefixList the prefix list
     */
public void populateFromContext(NamespacePrefixList prefixList) {
        for (String prefix : prefixList.getDeclaredPrefixes()) {
            prefixToUriMap.put(prefix, prefixList.getNamespaceURI(prefix));
            uriToPrefixMap.put(prefixList.getNamespaceURI(prefix), prefix);
        }
    }

    /**
     * Gets prefix.
     *
     * @param assumedUri the assumed uri
     * @return the prefix
     */
public String getPrefix(String assumedUri) {
        if (uriToPrefixMap.containsKey(assumedUri)) {
            return uriToPrefixMap.get(assumedUri);
        } else if (prefixToUriMap.containsKey(assumedUri)) {
            return assumedUri;
        }
        return "";
    }

    /**
     * Gets uri.
     *
     * @param assumedPrefix the assumed prefix
     * @return the uri
     */
public String getUri(String assumedPrefix) {
        if (prefixToUriMap.containsKey(assumedPrefix)) {
            return prefixToUriMap.get(assumedPrefix);
        } else if (uriToPrefixMap.containsKey(assumedPrefix)) {
            return assumedPrefix;
        }
        return "";
    }
}
