import java.util.*;

public class EntryContainer {
    Map<String, List<AbstractEntry>> namespacePrefixEntryMap;

    public EntryContainer() {
        namespacePrefixEntryMap = new HashMap<>();
    }

    public void addEntry(AbstractEntry entry) {
        String prefix = entry.getNamespacePrefix();

        if (!namespacePrefixEntryMap.containsKey(prefix)) {
            namespacePrefixEntryMap.put(prefix, new ArrayList<>());
        }

        namespacePrefixEntryMap.get(prefix).add(entry);
    }

    public List<String> getNamespacePrefixes() {
        return new ArrayList<>(namespacePrefixEntryMap.keySet());
    }

    public Map<String, List<AbstractEntry>> getNamespacePrefixEntryMap() {
        return namespacePrefixEntryMap;
    }
}
