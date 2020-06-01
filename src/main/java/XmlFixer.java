import com.google.common.base.CaseFormat;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.management.Attribute;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.Map;

public class XmlFixer {
  public XmlFixer() {}

  public String fixFromPath(String path)
      throws IOException, ParserConfigurationException, SAXException, TransformerException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(path);

    Node root = doc.getFirstChild();
      renameNodes(doc, root);
      appendAttributesToNodes(doc, root);

    // write the content into xml file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(path + "r"));

    // Hide <?xml> tag since it breaks the merger
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(source, result);

    System.out.println("Done writing transformed xml");
    return doc.toString();
  }

  public void renameNodes(Document doc, Node node) {
    doc.renameNode(
        node,
        node.getNamespaceURI(),
        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getNodeName()));

    NodeList nodes = node.getChildNodes();
    int children_len = nodes.getLength();
    for (int i = 0; i < children_len; i++) {
      if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
          renameNodes(doc, nodes.item(i));
      }
    }
  }

  public void appendAttributesToNodes(Document doc, Node node) {
      NamedNodeMap attributes = node.getAttributes();
      while (attributes.getLength() > 0) {
          System.out.println(attributes.item(0));
          System.out.println(attributes.item(0).getNodeName());
          System.out.println(attributes.item(0).getNodeValue());
          Node attr = attributes.item(0);
          if (!attr.getNodeName().contains(":")) {
              Element attr_to_append = doc.createElement(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, attr.getNodeName()));
              attr_to_append.setTextContent(attr.getNodeValue());
              node.appendChild(attr_to_append);
          }
          ((Element)node).removeAttribute(attr.getNodeName());
      }

      NodeList nodes = node.getChildNodes();
      int children_len = nodes.getLength();
      for (int i = 0; i < children_len; i++) {
          if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
              appendAttributesToNodes(doc, nodes.item(i));
          }
      }
  }
}
