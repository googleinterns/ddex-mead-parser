import java.util.ArrayList;
import java.util.List;

public abstract class EntryCandidate {
  String title;
  String namespacePrefix;
  List<ProtoField> fields;

  public EntryCandidate(String entryTitle, String entryNamespacePrefix) {
    title = entryTitle;
    namespacePrefix = entryNamespacePrefix;
    fields = new ArrayList<>();
  }

  public void addField(ProtoField protoField) {
    fields.add(protoField);
  }

  public String getTitle() {
    return title;
  }

  public String getNamespacePrefix() {
    return namespacePrefix;
  }

  public List<ProtoField> getFields() {
    return fields;
  }

  public boolean isEnum() { return false; }
  public boolean isMessage() { return false; }
}
