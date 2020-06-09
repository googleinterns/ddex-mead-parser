import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEntry {
  String title;
  String namespacePrefix;
  Map<String, EntryField> fields;
  boolean extension;

  public AbstractEntry(String entryTitle, String entryNamespacePrefix) {
    title = entryTitle;
    namespacePrefix = entryNamespacePrefix;
    fields = new HashMap<>();
    extension = false;
  }

  public void addField(EntryField entryField) {
    fields.put(entryField.getFieldValue(), entryField);
    
    if (entryField.getFieldValue().equals("ext_value")) {
      extension = true;
    }
  }

  public String getTitle() {
    return title;
  }

  public String getNamespacePrefix() {
    return namespacePrefix;
  }

  public List<EntryField> getFields() {
    return new ArrayList<>(fields.values());
  }

  public boolean isExtension() { return extension; }
  public boolean hasFields() { return fields.size() > 0 ;}
  public boolean isEnum() { return false; }
  public boolean isMessage() { return false; }
}
