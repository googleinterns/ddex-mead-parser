package com.google.ddex.xmltoproto;

import com.google.common.base.CaseFormat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.Reader;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import com.google.common.flogger.FluentLogger;

public class MessageParser {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static Message parse(Reader reader, Message.Builder messageBuilder) throws MessageParseException {
    MessageParseReporter defaultReporter = new MessageParseReporter();
    MessageParserInstance messageParserInstance = new MessageParserInstance(reader, messageBuilder, defaultReporter);
    return messageParserInstance.parse();
  }

  public static Message parse(Reader reader, Message.Builder messageBuilder, MessageParseReporter reporter) throws MessageParseException {
    MessageParserInstance messageParserInstance = new MessageParserInstance(reader, messageBuilder, reporter);
    return messageParserInstance.parse();
  }

  private static class MessageParserInstance {
    private final Reader inputXml;
    private final Message.Builder baseBuilder;
    private final MessageParseReporter reporter;

    public MessageParserInstance(Reader reader, Message.Builder builder, MessageParseReporter messageParseReporter) {
      inputXml = reader;
      baseBuilder = builder;
      reporter = messageParseReporter;
    }

    public Message parse() throws MessageParseException {
      try {
        InputSource xmlInputSource = new InputSource(inputXml);
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlInputSource);
        Node root = getRootNode(document);
        mergeMessage(document, root, baseBuilder);
        return baseBuilder.build();
      } catch (ParserConfigurationException | SAXException | IOException e) {
        throw new MessageParseException("Exception occurred when getting document: " + e.getMessage(), e);
      }
    }

    private Node getRootNode(Document document) throws MessageParseException {
      NodeList nodes = document.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
          return nodes.item(i);
        }
      }
      throw new MessageParseException("No root node found");
    }

    private void mergeMessage(Document document, Node node, Message.Builder messageBuilder) {
      Descriptors.Descriptor messageDescriptor = messageBuilder.getDescriptorForType();

      shiftToExtField(document, node, messageDescriptor);
      shiftToAutoField(document, node, messageDescriptor);
      shiftToEnumField(document, node, messageDescriptor);
      shiftNodeAttributes(document, node);

      NodeList nodes = node.getChildNodes();
      for (int i = 0, len = nodes.getLength(); i < len; i++) {
        Node child = nodes.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE) {
          Descriptors.FieldDescriptor field =
              messageDescriptor.findFieldByName(
                  CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, child.getNodeName()));
          if (field != null) {
            logger.atFine().log("Handling field: " + field.getFullName());
            Object content = handleNode(document, child, field, messageBuilder);
            if (field.isRepeated()) {
              messageBuilder.addRepeatedField(field, content);
            } else {
              messageBuilder.setField(field, content);
            }
          } else {
            reporter.addWarning("Unexpected field. Skipping " + child.getNodeName() + " in " + node.getNodeName());
          }
        }
      }
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

    private boolean shouldShiftEnumValue(Descriptors.Descriptor messageDescriptor) {
      if (messageDescriptor == null) {
        return false;
      }
      Descriptors.FieldDescriptor field = messageDescriptor.findFieldByName("enum_value");
      return field != null;
    }

    private void shiftToExtField(Document document, Node node, Descriptors.Descriptor messageDescriptor) {
      if (shouldShiftExtValue(messageDescriptor)) {
        Element attr_to_append = document.createElement("ext_value");
        attr_to_append.setTextContent(node.getTextContent());
        node.setTextContent("");
        node.appendChild(attr_to_append);
      }
    }

    private void shiftToAutoField(Document document, Node node, Descriptors.Descriptor messageDescriptor) {
      if (shouldShiftAutoValue(messageDescriptor)) {
        Element attr_to_append = document.createElement("auto_value");
        attr_to_append.setTextContent(node.getTextContent());
        node.setTextContent("");
        node.appendChild(attr_to_append);
      }
    }

    private void shiftToEnumField(Document document, Node node, Descriptors.Descriptor messageDescriptor) {
      if (shouldShiftEnumValue(messageDescriptor)) {
        Element attr_to_append = document.createElement("enum_value");
        attr_to_append.setTextContent(node.getTextContent());
        node.setTextContent("");
        node.appendChild(attr_to_append);
      }
    }

    private void shiftNodeAttributes(Document document, Node node) {
      NamedNodeMap attributes = node.getAttributes();
      if (attributes != null) {
        while (attributes.getLength() > 0) {
          Node attr = attributes.item(0);
          if (!attr.getNodeName().contains(":")) {
            Element attr_to_append =
                document.createElement(
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, attr.getNodeName()));
            attr_to_append.setTextContent(attr.getNodeValue());
            node.appendChild(attr_to_append);
          }
          ((Element) node).removeAttribute(attr.getNodeName());
        }
      }
    }

    private Object handleNode(
        Document document,
        Node node,
        Descriptors.FieldDescriptor field,
        Message.Builder messageBuilder) {
      if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
        return handleMessage(document, node, field, messageBuilder);
      } else {
        return handleField(node, field);
      }
    }

    private Object handleMessage(
        Document document,
        Node node,
        Descriptors.FieldDescriptor field,
        Message.Builder messageBuilder) {
      Message.Builder innerBuilder = messageBuilder.newBuilderForField(field);
      mergeMessage(document, node, innerBuilder);
      return innerBuilder.build();
    }

    private Object handleField(Node node, Descriptors.FieldDescriptor field) {
      Descriptors.FieldDescriptor.JavaType fieldType = field.getJavaType();
      String textContent = node.getTextContent();
      switch (fieldType) {
        case ENUM:
          logger.atWarning().log("Encountered unexpected enum field: " + field.getFullName());
          return null;
        case BOOLEAN:
          return Boolean.parseBoolean(textContent);
        case INT:
          return Integer.parseInt(textContent);
        case DOUBLE:
          return Double.parseDouble(textContent);
        case FLOAT:
          return Float.parseFloat(textContent);
        case BYTE_STRING:
          return ByteString.copyFromUtf8(textContent);
        case LONG:
          return handleDateText(textContent);
        case STRING:
          return textContent;
      }
      return null;
    }

    private long handleDateText(String textContent) {
      if (textContent.endsWith("Z")) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(textContent);
        return zonedDateTime.toInstant().toEpochMilli();
      } else if (textContent.contains("+")) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(textContent);
        return offsetDateTime.toInstant().toEpochMilli();
      } else {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(textContent + "Z");
        return zonedDateTime.toInstant().toEpochMilli();
      }
    }
  }
}
