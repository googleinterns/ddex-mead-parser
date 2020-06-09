import org.apache.ws.commons.schema.XmlSchema;

public class WrappedXmlSchema {
  public XmlSchema schema;
  public String uri;
  public String prefix;

  public WrappedXmlSchema(String uriNamespace, String prefixNamespace, XmlSchema innerSchema) {
    schema = innerSchema;
    uri = uriNamespace;
    prefix = prefixNamespace;
  }

  public XmlSchema getSchema() {
    return schema;
  }

  public String getUri() {
    return uri;
  }

  public String getPrefix() {
    return prefix;
  }
}
