import com.google.common.base.CaseFormat;
import com.google.protobuf.Descriptors;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.ZonedDateTime;

import ern.Ern;

public class XmlFixer {
  Descriptors.FileDescriptor file;

  public XmlFixer() {
    file = Ern.getDescriptor();
  }

  public void fixFromPath(String path)
      throws ParserConfigurationException, TransformerException, IOException, SAXException {
    Document doc = getDocument(path);
    transformDocToParsableXml(doc);
    writeDocToPath(doc, path);
  }

  private Document getDocument(String path)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    return docBuilder.parse(path);
  }

  private void transformDocToParsableXml(Document doc) {
    Node root = doc.getFirstChild();
    traverseTransformWrapper(doc, root, null);
  }

  private void traverseTransformWrapper(Document doc, Node node, Descriptors.Descriptor parentMessage) {
    Descriptors.Descriptor currentNodeMessageDescriptor =
        getNodeMessageDescriptor(node, parentMessage);
    Descriptors.FieldDescriptor currentNodeFieldDescriptor =
        getNodeFieldDescriptor(node, parentMessage);

    // Wrap extension value
    if (shouldShiftExtValue(currentNodeMessageDescriptor)) {
      Element attr_to_append = doc.createElement("ext_value");
      attr_to_append.setTextContent(node.getTextContent());
      node.setTextContent("");
      node.appendChild(attr_to_append);
    }
    if (shouldShiftAutoValue(currentNodeMessageDescriptor)) {
      Element attr_to_append = doc.createElement("auto_value");
      attr_to_append.setTextContent(escapeString(node.getTextContent()));
      node.setTextContent("");
      node.appendChild(attr_to_append);
    }

    // Shift attributes
    NamedNodeMap attributes = node.getAttributes();
    while (attributes.getLength() > 0) {
      Node attr = attributes.item(0);
      if (!attr.getNodeName().contains(":")) {
        Element attr_to_append = doc.createElement(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, attr.getNodeName()));
        attr_to_append.setTextContent(attr.getNodeValue());
        node.appendChild(attr_to_append);
      }
      ((Element) node).removeAttribute(attr.getNodeName());
    }

    // Rename node
    doc.renameNode(
        node,
        node.getNamespaceURI(),
        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getNodeName()));

    // Fix node value
    if (currentNodeFieldDescriptor != null) {
      Descriptors.FieldDescriptor.JavaType javaType = currentNodeFieldDescriptor.getJavaType();
      if (javaType == Descriptors.FieldDescriptor.JavaType.STRING) {
        String content = escapeString(node.getTextContent());
        node.setTextContent(content);
      }
      if (javaType == Descriptors.FieldDescriptor.JavaType.LONG) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(node.getTextContent());
        node.setTextContent(Long.toString(zonedDateTime.toInstant().toEpochMilli()));
      }
      if (javaType == Descriptors.FieldDescriptor.JavaType.ENUM) {
        String enumName = currentNodeFieldDescriptor.getEnumType().getName();
        String content = node.getTextContent();
        content = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, enumName) + "_" + CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, content);
        node.setTextContent(content);
      }
    }

    // Recurse on children nodes, including previously appended nodes
    NodeList nodes = node.getChildNodes();
    int children_len = nodes.getLength();
    for (int i = 0; i < children_len; i++) {
      if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
        traverseTransformWrapper(doc, nodes.item(i), currentNodeMessageDescriptor);
      }
    }
  }

  // Either returns a message descriptor if the current node corresponds to a field with a type. If
  // a java primitive, will return null
  private Descriptors.Descriptor getNodeMessageDescriptor(
      Node node, Descriptors.Descriptor parentMessage) {
    if (parentMessage != null) {
      // search the parent message descriptor for a field by the name of the current node
      // -> determines type of current node defined by .proto schema
      Descriptors.Descriptor messageDescriptor =
          file.findMessageTypeByName(parentMessage.getName());
      Descriptors.FieldDescriptor currentField =
          messageDescriptor.findFieldByName(
              CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getNodeName()));
      if (currentField.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
        return currentField.getMessageType();
      }
      return null;
    }
    // node has no parent, is a root element such as NewReleaseMessage or PurgeReleaseMessage or MeadMessage
    return file.findMessageTypeByName(node.getNodeName());
  }

  private Descriptors.FieldDescriptor getNodeFieldDescriptor(
      Node node, Descriptors.Descriptor parentMessage) {
    if (parentMessage != null) {
      Descriptors.Descriptor messageDescriptor =
          file.findMessageTypeByName(parentMessage.getName());
      return messageDescriptor.findFieldByName(
              CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getNodeName()));
    }
    return null;
  }

  private boolean shouldShiftExtValue(Descriptors.Descriptor messageDescriptor) {
    if (messageDescriptor == null) {
      return false;
    }
    Descriptors.FieldDescriptor field = messageDescriptor.findFieldByName("ext_value");
    return field != null;
  }

  private boolean shouldShiftAutoValue(Descriptors.Descriptor messageDescriptor) {
    if (messageDescriptor == null) {
      return false;
    }
    Descriptors.FieldDescriptor field = messageDescriptor.findFieldByName("auto_value");
    return field != null;
  }

  private String escapeString(String content) {
    if (!content.startsWith("\"")) {
      content = '\"' + content + '\"';
    }
    return content;
  }

  private void writeDocToPath(Document doc, String path) throws TransformerException {
    // write the content into xml file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(path + "r"));

    // Hide <?xml> tag since it breaks the merger
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(source, result);
  }
}
