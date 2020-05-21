import java.util.ArrayList;
import java.util.List;

public abstract class EntryCandidate {
    String title;
    String namespace;
    List<ProtoField> fields;

    public EntryCandidate(String entryTitle, String entryNamespace) {
        title = entryTitle;
        namespace = entryNamespace;
        fields = new ArrayList<>();
    }

    public void addField(ProtoField protoField) {
        fields.add(protoField);
    }

    public String getTitle() {
        return title;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<ProtoField> getFields() {
        return fields;
    }
}
