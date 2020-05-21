import java.util.ArrayList;
import java.util.List;

public abstract class EntryCandidate {
    String title;
    String namespace;
    List<ProtoField> fields;

    public EntryCandidate(String enumTitle, String enumNamespace) {
        title = enumTitle;
        namespace = enumNamespace;
        fields = new ArrayList<>();
    }

    public void addField(ProtoField protoField) {
        fields.add(protoField);
    }
}
