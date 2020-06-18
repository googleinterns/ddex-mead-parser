package com.google.ddexmeadparser;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SchemaConverterInstance {
    private final SchemaEntryMap schemaEntryMap;
    private final SchemaNamespaceMap namespaceMap;
    private final StreamSource inputXml;

    public SchemaConverterInstance(StreamSource source) {
        schemaEntryMap = new SchemaEntryMap();
        namespaceMap = new SchemaNamespaceMap();
        inputXml = source;
    }

    public SchemaEntryMap convert() {
        populateEntryMap();
        return schemaEntryMap;
    }

    private int getMeadVersionNumber(XmlSchema inputSchema) {
        String schemaLocation = inputSchema.getTargetNamespace();
        try {
            String uri = new URI(schemaLocation).getPath();
            String schemaVersion = uri.substring(uri.lastIndexOf('/') + 1);
            return Integer.parseInt(schemaVersion);
        } catch (URISyntaxException e) {
//            throw new MeadConversionException("Malformed URI for schema location. Could not determine version number", e);
        }
        return 0;
    }

    public void populateEntryMap() {
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema inputSchema = schemaCol.read(inputXml);

        List<XmlSchema> allSchema = getAllSchema(inputSchema);
        populateNamespaceMap(allSchema);

        schemaEntryMap.setVersion(getMeadVersionNumber(inputSchema));
        schemaEntryMap.setRootNamespacePrefix(namespaceMap.getPrefix(inputSchema.getTargetNamespace()));

        System.out.println(namespaceMap.getPrefix(inputSchema.getTargetNamespace()));
        for (XmlSchema schema : allSchema) {
            processSchema(schema);
        }
    }

    private void processSchema(XmlSchema schema) {
        System.out.println("Processing schema -> " + schema.getTargetNamespace());

        String schemaNamespacePrefix = namespaceMap.getPrefix(schema.getTargetNamespace());
        List<XmlSchemaObject> schemaItems = schema.getItems();

        for (XmlSchemaObject item : schemaItems) {
            if (item instanceof XmlSchemaSimpleType) {
                processSimple((XmlSchemaSimpleType) item, null, schemaNamespacePrefix, null);
            } else if (item instanceof XmlSchemaComplexType) {
                processComplex((XmlSchemaComplexType) item, null, schemaNamespacePrefix, null);
            } else if (item instanceof XmlSchemaElement) {
                processElement((XmlSchemaElement) item, null, schemaNamespacePrefix, null);
            }
        }
    }

    private QName processElement(
            XmlSchemaElement elementItem, String entryName, String nsPrefix, SchemaAbstractEntry parent) {
        if (elementItem.getSchemaType() instanceof XmlSchemaSimpleType) {
            return processSimple(
                    (XmlSchemaSimpleType) elementItem.getSchemaType(),
                    elementItem.getName(),
                    nsPrefix,
                    parent);
        } else if (elementItem.getSchemaType() instanceof XmlSchemaComplexType) {
            return processComplex(
                    (XmlSchemaComplexType) elementItem.getSchemaType(),
                    elementItem.getName(),
                    nsPrefix,
                    parent);
        } else {
            throw new Error("Unable to process element " + elementItem.getClass());
        }
    }

    private QName processSimple(
            XmlSchemaSimpleType simpleItem,
            String entryName,
            String nsPrefix,
            SchemaAbstractEntry parent) {
        if (entryName == null && simpleItem.getName() == null) {
            throw new Error("No name for simple entry");
        }
        entryName = entryName != null ? entryName : simpleItem.getName();

        if (simpleItem.getContent() instanceof XmlSchemaSimpleTypeUnion) {
            return processSimpleUnion(
                    (XmlSchemaSimpleTypeUnion) simpleItem.getContent(),
                    entryName,
                    nsPrefix,
                    parent);
        } else if (simpleItem.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            return processSimpleRestriction(
                    (XmlSchemaSimpleTypeRestriction) simpleItem.getContent(),
                    entryName,
                    nsPrefix,
                    parent);
        } else {
            throw new Error("Unhandled simple type " + simpleItem.getClass());
        }
    }

    private QName processSimpleUnion(
            XmlSchemaSimpleTypeUnion union,
            String entryName,
            String nsPrefix,
            SchemaAbstractEntry parent) {
        SchemaMessageEntry messageEntry = new SchemaMessageEntry(entryName, nsPrefix);
        messageEntry.setEntryAnnotation(getAnnotations(union));
        messageEntry.addField(new SchemaField("auto_value"));
        schemaEntryMap.addEntry(messageEntry);
        return new QName(
                namespaceMap.getUri(messageEntry.getNamespacePrefix()),
                messageEntry.getTitle(),
                messageEntry.getNamespacePrefix());
    }

    private QName processSimpleRestriction(
            XmlSchemaSimpleTypeRestriction restriction,
            String entryName,
            String nsPrefix,
            SchemaAbstractEntry parent) {
        List<XmlSchemaFacet> facets = restriction.getFacets();

        if (isEnumFacetList(facets)) {
            SchemaEnumEntry enumEntry = new SchemaEnumEntry(entryName, nsPrefix);
            enumEntry.setEntryAnnotation(getAnnotations(restriction));

            for (XmlSchemaFacet facet : facets) {
                SchemaField field = new SchemaField(facet.getValue().toString());
                field.setFieldAnnotation(getAnnotations(facet));
                enumEntry.addField(field);
            }
            if (enumEntry.hasFields()) {
                schemaEntryMap.addEntry(enumEntry);
                return new QName(
                        namespaceMap.getUri(enumEntry.getNamespacePrefix()),
                        enumEntry.getTitle(),
                        enumEntry.getNamespacePrefix());
            }
        } else if (parent == null) {
            SchemaMessageEntry messageEntry = new SchemaMessageEntry(entryName, nsPrefix);
            messageEntry.setEntryAnnotation(getAnnotations(restriction));
            QName restrictionQName = restriction.getBaseTypeName(); // Always a STRING restriction
            messageEntry.addField(new SchemaField("auto_value", restrictionQName));
            schemaEntryMap.addEntry(messageEntry);
            return new QName(
                    namespaceMap.getUri(messageEntry.getNamespacePrefix()),
                    messageEntry.getTitle(),
                    messageEntry.getNamespacePrefix());
        }
        return null;
    }

    private QName processComplex(
            XmlSchemaComplexType complexItem,
            String entryName,
            String nsPrefix,
            SchemaAbstractEntry parent) {
        if (entryName == null && complexItem.getName() == null) {
            throw new Error("Complex item had no name");
        }
        entryName = entryName != null ? entryName : complexItem.getName();
        SchemaMessageEntry messageEntry = new SchemaMessageEntry(entryName, nsPrefix);
        messageEntry.setEntryAnnotation(getAnnotations(complexItem));

        processAttributes(complexItem.getAttributes(), complexItem.getAnyAttribute(), messageEntry);
        processParticle(complexItem.getParticle(), messageEntry, parent);
        processContentModel(complexItem.getContentModel(), messageEntry, parent);

        if (messageEntry.hasFields()) {
            schemaEntryMap.addEntry(messageEntry);
            return new QName(
                    namespaceMap.getUri(messageEntry.getNamespacePrefix()),
                    messageEntry.getTitle(),
                    messageEntry.getNamespacePrefix());
        }
        return null;
    }

    private void processParticle(
            XmlSchemaParticle particle,
            SchemaAbstractEntry entry,
            SchemaAbstractEntry parent) {
        if (particle != null) {
            if (particle instanceof XmlSchemaSequence) {
                List<XmlSchemaSequenceMember> items = ((XmlSchemaSequence) particle).getItems();
                for (XmlSchemaSequenceMember item : items) {
                    if (item instanceof XmlSchemaSequence || item instanceof XmlSchemaChoice) {
                        processParticle((XmlSchemaParticle) item, entry, parent);
                    } else {
                        processItem(item, entry, parent);
                    }
                }
            }

            if (particle instanceof XmlSchemaChoice) {
                List<XmlSchemaChoiceMember> items = ((XmlSchemaChoice) particle).getItems();
                for (XmlSchemaChoiceMember item : items) {
                    if (item instanceof XmlSchemaSequence || item instanceof XmlSchemaChoice) {
                        processParticle((XmlSchemaParticle) item, entry, parent);
                    } else {
                        processItem(item, entry, parent);
                    }
                }
            }
        }
    }

    private void processItem(
            XmlSchemaObjectBase item,
            SchemaAbstractEntry entry,
            SchemaAbstractEntry parent) {
        if (item instanceof XmlSchemaElement) {
            XmlSchemaType type = ((XmlSchemaElement) item).getSchemaType();
            QName itemType = ((XmlSchemaElement) item).getSchemaTypeName();

            // Only evaluate an element that is a concrete definition as opposed to a leaf referencing an el
            if (type != null && itemType == null) {
                itemType = processElement((XmlSchemaElement) item, entry.getTitle(), entry.getNamespacePrefix(), entry);
            } else {
                itemType = ((XmlSchemaElement) item).getSchemaTypeName();
            }

            String name = ((XmlSchemaElement) item).getName();
            boolean repeated = ((XmlSchemaElement) item).getMaxOccurs() > 1;

            getAnnotations((XmlSchemaAnnotated) item);
            SchemaField field = new SchemaField(name, itemType, repeated);
            field.setFieldAnnotation(getAnnotations((XmlSchemaAnnotated) item));
            entry.addField(field);
        } else if (item instanceof XmlSchemaAny) {
            boolean repeated = ((XmlSchemaAny) item).getMaxOccurs() > 1;
            entry.addField(new SchemaField("any_value", null, repeated));
        } else {
            throw new Error("Unhandled item " + item.getClass());
        }
    }

    private void processContentModel(
            XmlSchemaContentModel contentModel,
            SchemaAbstractEntry entry,
            SchemaAbstractEntry parent) {
        if (contentModel != null) {
            if (contentModel.getContent() instanceof XmlSchemaSimpleContentExtension) {
                XmlSchemaSimpleContentExtension content = (XmlSchemaSimpleContentExtension) contentModel.getContent();
                processAttributes(content.getAttributes(), content.getAnyAttribute(), entry);
                entry.addField(new SchemaField("ext_value", content.getBaseTypeName()));
            } else if (contentModel.getContent() instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension content = (XmlSchemaComplexContentExtension) contentModel.getContent();
                processParticle(content.getParticle(), entry, parent);
                entry.addField(new SchemaField("ext_value", content.getBaseTypeName()));
            } else {
                throw new Error("Unhandled content " + contentModel.getContent().getClass());
            }
        }
    }

    private void processAttributes(List<XmlSchemaAttributeOrGroupRef> attributes, XmlSchemaAnyAttribute anyAttribute, SchemaAbstractEntry entry) {
        if (attributes != null && attributes.size() > 0) {
            for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
                if (attribute instanceof XmlSchemaAttribute) {
                    String name = ((XmlSchemaAttribute) attribute).getName();
                    QName attributeType = getAttributeTypeName(((XmlSchemaAttribute) attribute));
                    SchemaField field = new SchemaField(name, attributeType);
                    field.setFieldAnnotation(getAnnotations(attribute));
                    entry.addField(field);
                } else {
                    throw new Error("Unhandled attribute");
                }
            }
        }

        if (anyAttribute != null) {
            entry.addField(new SchemaField("any_attribute_value", null, true));

        }
    }

    private List<XmlSchema> getAllSchema(XmlSchema inputSchema) {
        List<XmlSchema> allSchema = new ArrayList<>();
        allSchema.add(inputSchema);
        inputSchema.getTargetNamespace();

        List<XmlSchemaExternal> schemaExternals = inputSchema.getExternals();
        for (XmlSchemaExternal external : schemaExternals) {
            if (external instanceof XmlSchemaImport || external instanceof XmlSchemaInclude) {
                XmlSchema externalSchema = external.getSchema();
                allSchema.add(externalSchema);
            } else if (external instanceof XmlSchemaRedefine) {
                XmlSchema redefinedSchema = external.getSchema();
                System.out.println( "Found XmlSchemaRedefine node."); // TODO handle redefines? Not needed for MEAD
            }
        }

        return allSchema;
    }

    private void populateNamespaceMap(List<XmlSchema> allSchema) {
        for (XmlSchema schema : allSchema) {
            NamespacePrefixList prefixes = schema.getNamespaceContext();
            namespaceMap.populateFromContext(prefixes);
        }
    }

    private QName getAttributeTypeName(XmlSchemaAttribute attribute) {
        QName qName = attribute.getSchemaTypeName();
        XmlSchemaSimpleType simpleItem =  attribute.getSchemaType();

        if (qName != null) {
            return qName;
        }
        if (simpleItem != null) {
            XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) simpleItem.getContent();
            return restriction.getBaseTypeName(); // Always a STRING restriction
        }
        return new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");
    }

    private boolean isEnumFacetList(List<XmlSchemaFacet> facets) {
        if (facets.size() <= 0) {
            return false;
        }
        for (XmlSchemaFacet facet : facets) {
            if (!(facet instanceof XmlSchemaEnumerationFacet)) {
                return false;
            }
        }
        return true;
    }

    XmlSchemaAnnotation getAnnotations(XmlSchemaAnnotated element) {
        XmlSchemaAnnotation annotation = element.getAnnotation();
        if (annotation != null) {
            for (int i = 0; i < annotation.getItems().size(); i++) {
                XmlSchemaDocumentation documentation = (XmlSchemaDocumentation) annotation.getItems().get(i);
                NodeList markup = documentation.getMarkup();
                for (int j = 0; j < markup.getLength(); j++) {
                    System.out.println(markup.item(j).getTextContent());
                }
            }
        }
        return annotation;
    }
}
