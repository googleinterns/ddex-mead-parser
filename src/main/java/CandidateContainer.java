import java.lang.reflect.Array;
import java.util.*;

public class CandidateContainer {
    Map<String, List<EntryCandidate>> namespacePrefixCandidateMap;

    public CandidateContainer() {
        namespacePrefixCandidateMap = new HashMap<>();
    }

    public void addCandidate(EntryCandidate candidate) {
        String prefix = candidate.getNamespacePrefix();

        if (!namespacePrefixCandidateMap.containsKey(prefix)) {
            namespacePrefixCandidateMap.put(prefix, new ArrayList<>());
        }

        namespacePrefixCandidateMap.get(prefix).add(candidate);
    }

    public List<String> getNamespacePrefixes() {
        return new ArrayList<>(namespacePrefixCandidateMap.keySet());
    }

    public Map<String, List<EntryCandidate>> getNamespacePrefixCandidateMap() {
        return namespacePrefixCandidateMap;
    }
}
