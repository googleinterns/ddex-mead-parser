package com.google.ddexmeadparser;

import com.google.common.base.CaseFormat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class MeadConverter {
    public Message convert(Document document) throws MeadConversionException {
        Message.Builder messageBuilder = MeadBuilderResolver.getBuilder(document);
        mergeRoot(document, messageBuilder);
        return messageBuilder.build();
    }

    public Message convert(String xmlString) throws MeadConversionException {
        try {
            InputSource xmlInputSource = new InputSource(new StringReader(xmlString));
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlInputSource);
            return convert(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MeadConversionException("Exception occurred when getting document: " + e.getMessage(), e);
        }
    }

    private void mergeRoot(Document document, Message.Builder messageBuilder) throws MeadConversionException {
        Node root = getRootNode(document);
        mergeMessage(document, root, messageBuilder);
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
                Descriptors.FieldDescriptor field = messageDescriptor.findFieldByName(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, child.getNodeName()));
                if (field != null) {
                    Object content = handleNode(document, child, field, messageBuilder);
                    if (field.isRepeated()) {
                        messageBuilder.addRepeatedField(field, content);
                    } else {
                        messageBuilder.setField(field, content);
                    }
                } else {
                    System.err.println("Skipping " + child.getNodeName() + " in " + node.getNodeName());
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
            attr_to_append.setTextContent(messageDescriptor.getName() +"::"+ node.getTextContent());
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
                    Element attr_to_append = document.createElement(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, attr.getNodeName()));
                    attr_to_append.setTextContent(attr.getNodeValue());
                    node.appendChild(attr_to_append);
                }
                ((Element) node).removeAttribute(attr.getNodeName());
            }
        }
    }

    private Object handleNode(Document document, Node node, Descriptors.FieldDescriptor field, Message.Builder messageBuilder) {
        if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            return handleMessage(document, node, field, messageBuilder);
        } else {
            return handleField(node, field);
        }
    }

    private Object handleMessage(Document document, Node node, Descriptors.FieldDescriptor field, Message.Builder messageBuilder) {
        Message.Builder innerBuilder = messageBuilder.newBuilderForField(field);
        mergeMessage(document, node, innerBuilder);
        return innerBuilder.build();
    }

    private Object handleField(Node node, Descriptors.FieldDescriptor field) {
        Descriptors.FieldDescriptor.JavaType fieldType = field.getJavaType();
        String textContent = node.getTextContent();

        switch (fieldType) {
            // Should not actually ever reach enum in this iteration of the converter
            case ENUM:
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
            case STRING:
                return textContent;
            case LONG: // Handle date
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
        return null;
    }

    public static Node getRootNode(Document document) throws MeadConversionException {
        NodeList nodes = document.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return nodes.item(i);
            }
        }
        throw new MeadConversionException("No root node found");
    }
}
