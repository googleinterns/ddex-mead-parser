public class ProtoField {
  String fieldValue;
  String fieldType;
  boolean repeated;

  public ProtoField(String type, String value) {
    fieldValue = value;
    fieldType = type;
  }

  public ProtoField(String value) {
    fieldValue = value;
    fieldType = "";
  }

  public String getFieldValue() {
    return fieldValue;
  }

  public String getFieldType() {
    return fieldType;
  }
}
