import org.apache.ws.commons.schema.utils.NamespacePrefixList;

import java.util.HashMap;
import java.util.Map;

// Could do typed prefix and uri

public class NamespaceManager {
  private Map<String, String> prefixToUriMap;
  private Map<String, String> uriToPrefixMap;

  public NamespaceManager() {
    prefixToUriMap = new HashMap<>();
    uriToPrefixMap = new HashMap<>();
  }

  public void populateFromContext(NamespacePrefixList prefixList) {
    for (String prefix : prefixList.getDeclaredPrefixes()) {
      setNamespacePair(prefix, prefixList.getNamespaceURI(prefix));
    }
  }

  public void setNamespacePair(String prefix, String uri) {
    prefixToUriMap.put(prefix, uri);
    uriToPrefixMap.put(uri, prefix);
  }

  public String getPrefix(String assumedUri) {
    if (uriToPrefixMap.containsKey(assumedUri)) {
      return uriToPrefixMap.get(assumedUri);
    } else if (prefixToUriMap.containsKey(assumedUri)) {
      return assumedUri;
    }
    return "";
  }

  public String getPrefixWithUri(String uri) {
    if (uriToPrefixMap.containsKey(uri)) {
      return uriToPrefixMap.get(uri);
    }
    // Log to std err
    return "";
  }

  public String getUri(String assumedPrefix) {
    if (prefixToUriMap.containsKey(assumedPrefix)) {
      return prefixToUriMap.get(assumedPrefix);
    } else if (uriToPrefixMap.containsKey(assumedPrefix)) {
      return assumedPrefix;
    }
    return "";
  }

  public String getUriWithPrefix(String prefix) {
    if (prefixToUriMap.containsKey(prefix)) {
      return prefixToUriMap.get(prefix);
    }
    // Log to std err
    return "";
  }

  public Map<String, String> getPrefixToUriMap() {
    return prefixToUriMap;
  }

  public Map<String, String> getUriToPrefixMap() {
    return uriToPrefixMap;
  }

  public void removePairByPrefix(String query) {
    if (prefixToUriMap.containsKey(query)) {
      uriToPrefixMap.remove(prefixToUriMap.get(query));
      prefixToUriMap.remove(query);
    }
  }

  public void removePairByUri(String query) {
    if (uriToPrefixMap.containsKey(query)) {
      uriToPrefixMap.remove(uriToPrefixMap.get(query));
      uriToPrefixMap.remove(query);
    }
  }

  public void printMappings() {
    for (Map.Entry<String, String> entry : prefixToUriMap.entrySet()) {
      System.out.println(entry.getKey() + " -> " + entry.getValue());
    }
  }
}
