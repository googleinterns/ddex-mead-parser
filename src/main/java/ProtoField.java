import javax.xml.namespace.QName;
import java.util.Objects;

public class ProtoField {
  String fieldValue;
  QName fieldQName;
  boolean fieldRepeated;

  public ProtoField(String value, QName qName, boolean repeated) {
    fieldValue = value;
    fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
    fieldRepeated = repeated;
  }

  public ProtoField(String value, QName qName) {
    fieldValue = value;
    fieldQName = Objects.requireNonNullElseGet(qName, () -> new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
    fieldRepeated = false;
  }

  // Default string QName
  public ProtoField(String value) {
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
}
