import javax.xml.namespace.QName;
import java.util.Objects;

public class EntryField {
  String fieldValue;
  QName fieldQName;
  boolean fieldRepeated;

  public EntryField(String value, QName qName, boolean repeated) {
    fieldValue = value;
    fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
    fieldRepeated = repeated;
  }

  public EntryField(String value, QName qName) {
    fieldValue = value;
    fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
    fieldRepeated = false;
  }

  // Default string QName
  public EntryField(String value) {
    fieldValue = value;
    fieldQName = new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");
    fieldRepeated = false;
  }

  public String getFieldValue() {
    return fieldValue;
  }

  public QName getFieldType() {
    return fieldQName;
  }

  public boolean isRepeated() { return fieldRepeated; }

  public boolean isXmlType() {
    return fieldQName.getPrefix().equals("xs");
  }
}
