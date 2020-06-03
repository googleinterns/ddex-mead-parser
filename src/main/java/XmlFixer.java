import com.google.common.base.CaseFormat;
import ern.Ern;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.management.Attribute;
import javax.management.Descriptor;
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
import com.google.protobuf.Descriptors;

public class XmlFixer {
  Map<String, Map<String, String>> candidateDetails;
  Descriptors.FileDescriptor file;

  public XmlFixer(CandidateContainer candidateContainer) {
    candidateDetails = candidateContainer.getCandidateDetails();
    file = Ern.getDescriptor();
  }

  public String fixFromPath(String path)
          throws IOException, ParserConfigurationException, SAXException, TransformerException {

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(path);

    Descriptors.Descriptor baseDescriptor = Ern.NewReleaseMessage.getDescriptor();
    Node root = doc.getFirstChild();

    // Perform transformations
    shiftExtValue(doc, root, null);
    appendAttributesToNodes(doc, root, null);
    renameNodes(doc, root);

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

  public void shiftExtValue(Document doc, Node node, String parentMessageName) {
    String name;

    boolean toShift = false;
    if (parentMessageName != null) {
      Descriptors.Descriptor messageDescriptor = file.findMessageTypeByName(parentMessageName);
      Descriptors.FieldDescriptor p = messageDescriptor.findFieldByName(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getNodeName()));
      if (p.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
        if (shouldShiftExtValue(p)) {
          String content;
          if (p.getMessageType().findFieldByName("ext_value").getType().equals("STRING")) {
            content = escapeString(node.getTextContent());
          } else {
            content = node.getTextContent();
          }
          Element attr_to_append = doc.createElement("ext_value");
          attr_to_append.setTextContent(content);
          node.setTextContent("");
          node.appendChild(attr_to_append);
        }
        name = p.getMessageType().getName();
      } else {
        name = p.getJavaType().name();
      }
    } else {
      Descriptors.Descriptor messageDescriptor = file.findMessageTypeByName(node.getNodeName());
      name = messageDescriptor.getName();
    }

    if (name.equals("STRING")) {
      String content = escapeString(node.getTextContent());
      node.setTextContent(content);
    }

    NodeList nodes = node.getChildNodes();
    int children_len = nodes.getLength();
    for (int i = 0; i < children_len; i++) {
      if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
        shiftExtValue(doc, nodes.item(i), name);
      }
    }
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

  public void appendAttributesToNodes(Document doc, Node node, String parentMessageName) {
    String name;

    boolean toShift = false;
    if (parentMessageName != null) {
      Descriptors.Descriptor messageDescriptor = file.findMessageTypeByName(parentMessageName);
      Descriptors.FieldDescriptor p = messageDescriptor.findFieldByName(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getNodeName()));
      if (p.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
        toShift  = shouldShiftExtValue(p);
        name = p.getMessageType().getName();
      } else {
        name = p.getJavaType().name();
      }
      if (name.equals("ENUM")) {
        String enumName = p.getEnumType().getName();
        String content  = node.getTextContent();
        content = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, enumName) + "_" +  CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, content);
        node.setTextContent(content);
      }
    } else {
      Descriptors.Descriptor messageDescriptor = file.findMessageTypeByName(node.getNodeName());
      name = messageDescriptor.getName();
    }


    NamedNodeMap attributes = node.getAttributes();
    while (attributes.getLength() > 0) {
      Node attr = attributes.item(0);
      if (!attr.getNodeName().contains(":")) {
        Element attr_to_append =
                doc.createElement(
                        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, attr.getNodeName()));
        attr_to_append.setTextContent(attr.getNodeValue());
        node.appendChild(attr_to_append);
      }
      ((Element) node).removeAttribute(attr.getNodeName());
    }

    NodeList nodes = node.getChildNodes();
    int children_len = nodes.getLength();
    for (int i = 0; i < children_len; i++) {
      if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
        appendAttributesToNodes(doc, nodes.item(i), name);
      }
    }
  }

  private boolean shouldShiftExtValue(Descriptors.FieldDescriptor fieldDescriptor) {
    Descriptors.FieldDescriptor field = fieldDescriptor.getMessageType().findFieldByName("ext_value");
    return field != null;
  }

  private String escapeString(String content) {
    content.replace("\"", "\\\"");
    if (!content.startsWith("\"")) {
      content = '\"' + content + '\"';
    }
    return content;
  }
}
