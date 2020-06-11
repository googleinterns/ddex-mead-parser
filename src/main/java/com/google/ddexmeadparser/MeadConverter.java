package com.google.ddexmeadparser;

import com.google.common.base.CaseFormat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

//import mead.Mead.MeadMessage;
import ern.Ern.NewReleaseMessage;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

public class MeadConverter {
    public NewReleaseMessage convert(String path) throws MeadConversionException {
        File meadXml = new File(path);
        if (!meadXml.exists() || meadXml.isDirectory()) {
            throw new MeadConversionException("XML file input does not exist.");
        }
        return convert(meadXml);
    }

    public NewReleaseMessage convert(File input) throws MeadConversionException {
        NewReleaseMessage.Builder messageBuilder = NewReleaseMessage.newBuilder();
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
            mergeRoot(document, messageBuilder);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MeadConversionException("Exception occurred when merging XML with builder: " + e.getMessage(), e);
        }

        return messageBuilder.build();
    }

    private void mergeRoot(Document document, Message.Builder messageBuilder) throws MeadConversionException {
        Node root = document.getFirstChild();
        mergeMessage(document, root, messageBuilder);
    }

    private void mergeMessage(Document document, Node node, Message.Builder messageBuilder) {
        Descriptors.Descriptor messageDescriptor = messageBuilder.getDescriptorForType();

        shiftToExtField(document, node, messageDescriptor);
        shiftToAutoField(document, node, messageDescriptor);
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
                }
            }
        }
    }

    private void shiftToExtField(Document document, Node node, Descriptors.Descriptor messageDescriptor) {
        if (shouldShiftExtValue(messageDescriptor)) {
            Element attr_to_append = document.createElement("ext_value");
            attr_to_append.setTextContent(node.getTextContent());
            node.setTextContent("");
            node.appendChild(attr_to_append);
        }
    }

    private void shiftNodeAttributes(Document document, Node node) {
        NamedNodeMap attributes = node.getAttributes();
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

    private void shiftToAutoField(Document document, Node node, Descriptors.Descriptor messageDescriptor) {
        if (shouldShiftAutoValue(messageDescriptor)) {
            Element attr_to_append = document.createElement("auto_value");
            attr_to_append.setTextContent(node.getTextContent());
            node.setTextContent("");
            node.appendChild(attr_to_append);
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
            case BOOLEAN:
                return Boolean.parseBoolean(textContent);
            case INT:
                return Integer.parseInt(textContent);
            case DOUBLE:
                return Double.parseDouble(textContent);
            case FLOAT:
                return Float.parseFloat(textContent);
            case STRING:
                return textContent;
            case BYTE_STRING:
                return ByteString.copyFromUtf8(textContent);
            case ENUM:
                Descriptors.EnumDescriptor enumType = field.getEnumType();
                if (textContent.matches("[0-9]+")) {
                    return enumType.findValueByNumber(Integer.parseInt(textContent));
                } else {
                    textContent = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, enumType.getName())
                                    + "_"
                                    + CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, textContent);
                    return enumType.findValueByName(textContent);
                }
            case LONG:
                // Handle date
                textContent = verifyDate(textContent);
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(textContent);
                return zonedDateTime.toInstant().toEpochMilli();
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

    private String verifyDate(String content) {
        if (!content.endsWith("Z")) {
            content = content + "Z";
        }
        return content;
    }
}
