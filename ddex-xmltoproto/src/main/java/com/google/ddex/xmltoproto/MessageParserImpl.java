package com.google.ddex.xmltoproto;

import com.google.common.base.CaseFormat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;

class MessageParserImpl implements MessageParser {
    private Reader inputXml;
    private Message.Builder baseBuilder;
    private MessageParserReporter reporter;

    MessageParserImpl() {}

    public Message parse(Reader reader, Message.Builder messageBuilder) throws MessageParseException {
        inputXml = reader;
        baseBuilder = messageBuilder;
        reporter = new MessageParserReporter.DefaultMessageParserReporter();

        return getMessage();
    }

    public Message parse(Reader reader, Message.Builder messageBuilder, MessageParserReporter reporter) throws MessageParseException {
        inputXml = reader;
        baseBuilder = messageBuilder;
        this.reporter = reporter;

        return getMessage();
    }

    public Message getMessage() throws MessageParseException {
        try {
            InputSource inputXmlSource = new InputSource(inputXml);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputXmlSource);
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

        nestParserGeneratedXmlTags(document, node, messageDescriptor);
        nestNodeAttributes(document, node);

        NodeList nodes = node.getChildNodes();
        for (int i = 0, len = nodes.getLength(); i < len; i++) {
            Node child = nodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Descriptors.FieldDescriptor field =
                        messageDescriptor.findFieldByName(
                                CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, child.getNodeName()));
                if (field != null) {
                    reporter.addLog("Handling field: " + field.getFullName());
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

    /**
     * Extract text content from a tag, and add it back as a child of the original tag. This is to allow for additional tags
     * such as attributes to be included in the original tag. The purpose of this flattening is to accommodate for the Protobuf schema
     * not supporting "attributes", requiring the attributes to be expressed as siblings of the original text content
     */
    private void nestParserGeneratedXmlTags(Document document, Node node, Descriptors.Descriptor messageDescriptor) {
        if (messageDescriptor == null) return;

        String parserGeneratedTag = "";
        if (messageDescriptor.findFieldByName("ext_value") != null) {
            parserGeneratedTag = "ext_value";
        } else if (messageDescriptor.findFieldByName("auto_value") != null) {
            parserGeneratedTag = "auto_value";
        } else if (messageDescriptor.findFieldByName("enum_value") != null) {
            parserGeneratedTag = "enum_value";
        }

        if (!parserGeneratedTag.isEmpty()) {
            Element attr_to_append = document.createElement(parserGeneratedTag);
            attr_to_append.setTextContent(node.getTextContent());
            node.setTextContent("");
            node.appendChild(attr_to_append);
        }
    }

    private void nestNodeAttributes(Document document, Node node) {
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
                return Long.parseLong(textContent);
            case STRING:
                return textContent;
            default:
                reporter.addWarning("Encountered unexpected enum field: " + field.getFullName());
                return null;
        }
    }
}