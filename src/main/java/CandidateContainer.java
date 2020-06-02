import java.lang.reflect.Array;
import java.util.*;

public class CandidateContainer {
    Map<String, List<EntryCandidate>> namespacePrefixCandidateMap;
    Map<String, Map<String, String>> candidateDetails; // TODO Rename

    public CandidateContainer() {
        namespacePrefixCandidateMap = new HashMap<>();
        candidateDetails = new HashMap<>();
    }

    public void addCandidate(EntryCandidate candidate) {
        addCandidate(candidate, null);
    }

    public void addCandidate(EntryCandidate candidate, String details) {
        String prefix = candidate.getNamespacePrefix();

        if (!namespacePrefixCandidateMap.containsKey(prefix)) {
            namespacePrefixCandidateMap.put(prefix, new ArrayList<>());
            candidateDetails.put(prefix, new HashMap<>());
        }

        if (candidate.isExtension()) {
            details = "EXT";
        }
        candidateDetails.get(prefix).put(candidate.getTitle(), details);
        namespacePrefixCandidateMap.get(prefix).add(candidate);
    }

    public List<String> getNamespacePrefixes() {
        return new ArrayList<>(namespacePrefixCandidateMap.keySet());
    }

    public Map<String, Map<String, String>> getCandidateDetails() {
        return candidateDetails;
    }

    public Map<String, List<EntryCandidate>> getNamespacePrefixCandidateMap() {
        return namespacePrefixCandidateMap;
    }
}
