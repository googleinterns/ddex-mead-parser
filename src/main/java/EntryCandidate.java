import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EntryCandidate {
  String title;
  String namespacePrefix;
  Map<String, ProtoField> fields;
  boolean extension;

  public EntryCandidate(String entryTitle, String entryNamespacePrefix) {
    title = entryTitle;
    namespacePrefix = entryNamespacePrefix;
    fields = new HashMap<>();
    extension = false;
  }

  public void addField(ProtoField protoField) {
    fields.put(protoField.getFieldValue(), protoField);
    
    if (protoField.getFieldValue().equals("ext_value")) {
      extension = true;
    }
  }

  public String getTitle() {
    return title;
  }

  public String getNamespacePrefix() {
    return namespacePrefix;
  }

  public List<ProtoField> getFields() {
    return new ArrayList<>(fields.values());
  }

  public boolean isExtension() { return extension; }
  public boolean hasFields() { return fields.size() > 0 ;}
  public boolean isEnum() { return false; }
  public boolean isMessage() { return false; }
}
