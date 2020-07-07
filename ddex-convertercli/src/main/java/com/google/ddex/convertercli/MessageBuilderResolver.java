package com.google.ddex.convertercli;

import com.google.ddex.xmltoproto.MessageParseException;
import com.google.protobuf.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.flogger.FluentLogger;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** The type Mead builder resolver. */
public class MessageBuilderResolver {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  /**
   * Gets builder.
   *
   * @param document the document
   * @return the builder
   * @throws MessageParseException the mead conversion exception
   */
  public static Message.Builder getBuilder(Document document) throws MessageParseException {
    Node root = getRootNode(document);
    int versionNumber = getMeadVersionNumber(root);
    int majorVersionNumber = getMeadMajorVersionNumber(root);
    String namespace = getNamespace(root);

    logger.atInfo().log("Detected message using major version " + majorVersionNumber + ", minor version " + versionNumber);

    if (majorVersionNumber == 4) {
      return ern42.ern.Ern.NewReleaseMessage.newBuilder();
    }
//    else if (majorVersionNumber == 3) {
//      return ern381.ern.Ern.NewReleaseMessage.newBuilder();
//    } else {
//      throw new SchemaConversionException("Unsupported message version " + versionNumber + ". Temporarily blocking issue");
//    }
    return null;
  }

  public static Message.Builder getBuilder(File file) throws MessageParseException, IOException {
    Document document = getDocument(file);
    return getBuilder(document);
  }

  private static Document getDocument(File file) throws IOException {
    if (!file.exists() || file.isDirectory()) {
      throw new FileNotFoundException("XML file input does not exist or is a directory.");
    }
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    } catch (ParserConfigurationException | IOException | SAXException e) {
      throw new IOException("Exception occurred when getting document: " + e.getMessage(), e);
    }
  }

  private static int getMeadMajorVersionNumber(Node root) throws MessageParseException {
    String schemaLocation = root.getAttributes().getNamedItem("xmlns:ern").getNodeValue();
    try {
      String uri = new URI(schemaLocation).getPath();
      String schemaVersion = uri.substring(uri.lastIndexOf('/') + 1);
      return Integer.parseInt(schemaVersion.substring(0, 1));
    } catch (URISyntaxException e) {
      throw new MessageParseException("Malformed URI for schema location. Could not determine major version number", e);
    }
  }

  private static int getMeadVersionNumber(Node root) throws MessageParseException {
    String schemaLocation = root.getAttributes().getNamedItem("xmlns:ern").getNodeValue();
    try {
      String uri = new URI(schemaLocation).getPath();
      String schemaVersion = uri.substring(uri.lastIndexOf('/') + 1);
      return Integer.parseInt(schemaVersion);
    } catch (URISyntaxException e) {
      throw new MessageParseException("Malformed URI for schema location. Could not determine version number", e);
    }
  }

  private static String getNamespace(Node root) {
    return root.getNamespaceURI();
  }

  private static Node getRootNode(Document document) throws MessageParseException {
    NodeList nodes = document.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
        return nodes.item(i);
      }
    }
    throw new MessageParseException("No root node found");
  }
}
