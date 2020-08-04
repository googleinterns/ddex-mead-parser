package com.google.ddex.xsdtoproto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;

class XsdNamespaceMap {
  private final Map<String, String> prefixToUriMap;
  private final Map<String, String> uriToPrefixMap;


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

  String getPrefix(String uri) {
    if (uriToPrefixMap.containsKey(uri)) {
      return uriToPrefixMap.get(uri);
    }
    return "";
  }

  String getUri(String prefix) {
    if (prefixToUriMap.containsKey(prefix)) {
      return prefixToUriMap.get(prefix);
    }
    return "";
  }
}
