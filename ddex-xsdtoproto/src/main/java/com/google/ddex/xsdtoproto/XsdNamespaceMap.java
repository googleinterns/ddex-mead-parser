package com.google.ddex.xsdtoproto;

import org.apache.ws.commons.schema.utils.NamespacePrefixList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Schema namespace map. */
class XsdNamespaceMap {
  private final Map<String, String> prefixToUriMap;
  private final Map<String, String> uriToPrefixMap;

  /** Instantiates a new Schema namespace map. */
  XsdNamespaceMap(List<NamespacePrefixList> allPrefixList) {
    prefixToUriMap = new HashMap<>();
    uriToPrefixMap = new HashMap<>();

    for (NamespacePrefixList prefixList : allPrefixList) {
      for (String prefix : prefixList.getDeclaredPrefixes()) {
        prefixToUriMap.put(prefix, prefixList.getNamespaceURI(prefix));
        uriToPrefixMap.put(prefixList.getNamespaceURI(prefix), prefix);
      }
    }
  }

  /**
   * Populate from context.
   *
   * @param prefixList the prefix list
   */
  void populateFromContext(NamespacePrefixList prefixList) {
    for (String prefix : prefixList.getDeclaredPrefixes()) {
      prefixToUriMap.put(prefix, prefixList.getNamespaceURI(prefix));
      uriToPrefixMap.put(prefixList.getNamespaceURI(prefix), prefix);
    }
  }

  /**
   * Gets prefix.
   *
   * @param uri the assumed uri
   * @return the prefix
   */
  String getPrefix(String uri) {
    if (uriToPrefixMap.containsKey(uri)) {
      return uriToPrefixMap.get(uri);
    }
    return "";
  }

  /**
   * Gets uri.
   *
   * @param prefix the assumed prefix
   * @return the uri
   */
  String getUri(String prefix) {
    if (prefixToUriMap.containsKey(prefix)) {
      return prefixToUriMap.get(prefix);
    }
    return "";
  }
}
