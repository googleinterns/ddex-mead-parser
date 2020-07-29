package com.google.ddex.xsdtoproto;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAnyAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaRedefine;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import javax.xml.namespace.QName;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

class XsdParserImpl implements XsdParser {
    private static final QName DEFAULT_QNAME = new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");
    private final ProtoSchemaEntryMap protoSchemaEntryMap;
    private XsdNamespaceMap namespaceMap;
    private Reader inputXml;
    private XsdParserReporter reporter;

    XsdParserImpl() {
        protoSchemaEntryMap = new ProtoSchemaEntryMap();
    }

    public ProtoSchema parse(Reader reader) throws XsdParseException {
        reporter = new XsdParserReporter.DefaultXsdParserReporter();
        inputXml = reader;
        populateEntryMap();
        return new ProtoSchema(protoSchemaEntryMap);
    }

    public ProtoSchema parse(Reader reader, XsdParserReporter reporter) throws XsdParseException {
        this.reporter = reporter;
        inputXml = reader;
        populateEntryMap();
        return new ProtoSchema(protoSchemaEntryMap);
    }

    private int getMeadVersionNumber(XmlSchema inputSchema) throws XsdParseException {
        String schemaLocation = inputSchema.getTargetNamespace();
        try {
            String uri = new URI(schemaLocation).getPath();
            String schemaVersion = uri.substring(uri.lastIndexOf('/') + 1);
            return Integer.parseInt(schemaVersion);
        } catch (URISyntaxException e) {
            throw new XsdParseException(
                    "Malformed URI for schema location. Could not determine version number", e);
        }
    }

    private void populateEntryMap() throws XsdParseException {
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema inputSchema = schemaCol.read(inputXml);

        List<XmlSchema> allSchema = getAllSchema(inputSchema);

        initializeNamespaceMap(allSchema);
        protoSchemaEntryMap.setVersion(getMeadVersionNumber(inputSchema));
        protoSchemaEntryMap.setRootNamespacePrefix(namespaceMap.getPrefix(inputSchema.getTargetNamespace()));

        for (XmlSchema schema : allSchema) {
            processSchema(schema);
        }
    }

    private void initializeNamespaceMap(List<XmlSchema> allSchema) {
        List<NamespacePrefixList> prefixesList = new ArrayList<>();
        for (XmlSchema schema : allSchema) {
            prefixesList.add(schema.getNamespaceContext());
        }
        namespaceMap = new XsdNamespaceMap(prefixesList);
    }

    private void processSchema(XmlSchema schema) throws XsdParseException {
        reporter.addProcessedSchema(schema.getTargetNamespace());

        String nsPrefix = namespaceMap.getPrefix(schema.getTargetNamespace());
        for (XmlSchemaObject item : schema.getItems()) {
            if (item instanceof XmlSchemaSimpleType) {
                processSimple((XmlSchemaSimpleType) item, null, nsPrefix, null);
            } else if (item instanceof XmlSchemaComplexType) {
                processComplex((XmlSchemaComplexType) item, null, nsPrefix, null);
            } else if (item instanceof XmlSchemaElement) {
                processElement((XmlSchemaElement) item, null, nsPrefix, null);
            }
        }
    }

    private QName processElement(
            XmlSchemaElement elementItem,
            String entryName,
            String nsPrefix,
            ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
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
            throw new XsdParseException(
                    "Unable to process element with class: " + elementItem.getClass());
        }
    }

    private QName processSimple(
            XmlSchemaSimpleType simpleItem,
            String entryName,
            String nsPrefix,
            ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
        if (entryName == null && simpleItem.getName() == null) {
            throw new XsdParseException("Simple element has no name. Cannot create type.");
        } else if (entryName == null) {
            entryName = simpleItem.getName();
        }

        if (simpleItem.getContent() instanceof XmlSchemaSimpleTypeUnion) {
            return processSimpleUnion(
                    (XmlSchemaSimpleTypeUnion) simpleItem.getContent(), entryName, nsPrefix, parent);
        } else if (simpleItem.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            return processSimpleRestriction(
                    (XmlSchemaSimpleTypeRestriction) simpleItem.getContent(), entryName, nsPrefix, parent);
        } else {
            throw new XsdParseException("Unhandled simple type: " + simpleItem.getClass());
        }
    }

    private QName processSimpleUnion(
            XmlSchemaSimpleTypeUnion union,
            String entryName,
            String nsPrefix,
            ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
        ProtoSchemaMessageEntry messageEntry = new ProtoSchemaMessageEntry(entryName, nsPrefix);
        messageEntry.setAnnotation(union.getAnnotation());
        messageEntry.setVersionAnnotation(
                protoSchemaEntryMap.getRootNamespacePrefix() + protoSchemaEntryMap.getVersion());
        messageEntry.addField(new ProtoSchemaField("auto_value"));
        protoSchemaEntryMap.addEntry(messageEntry);
        return new QName(
                namespaceMap.getUri(messageEntry.getNamespacePrefix()),
                messageEntry.getTitle(),
                messageEntry.getNamespacePrefix());
    }

    private QName processSimpleRestriction(
            XmlSchemaSimpleTypeRestriction restriction,
            String entryName,
            String nsPrefix,
            ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
        List<XmlSchemaFacet> facets = restriction.getFacets();

        if (isEnumFacetList(facets)) {
            ProtoSchemaMessageEntry messageEntry = new ProtoSchemaMessageEntry(entryName, nsPrefix);
            messageEntry.setAnnotation("SchemaConverter generated enum replacement message type");
            messageEntry.setVersionAnnotation(
                    protoSchemaEntryMap.getRootNamespacePrefix() + protoSchemaEntryMap.getVersion());
            messageEntry.addField(new ProtoSchemaField("enum_value", DEFAULT_QNAME));
            protoSchemaEntryMap.addEntry(messageEntry);
            return new QName(
                    namespaceMap.getUri(messageEntry.getNamespacePrefix()),
                    messageEntry.getTitle(),
                    messageEntry.getNamespacePrefix());
        } else if (parent == null) {
            ProtoSchemaMessageEntry messageEntry = new ProtoSchemaMessageEntry(entryName, nsPrefix);
            QName restrictionQName = restriction.getBaseTypeName(); // Always a STRING restriction
            messageEntry.setAnnotation("SchemaConverter generated base level auto field wrapper");
            messageEntry.setVersionAnnotation(
                    protoSchemaEntryMap.getRootNamespacePrefix() + protoSchemaEntryMap.getVersion());
            messageEntry.addField(new ProtoSchemaField("auto_value", restrictionQName));
            protoSchemaEntryMap.addEntry(messageEntry);
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
            ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
        if (entryName == null && complexItem.getName() == null) {
            throw new XsdParseException("Complex element has no name. Cannot create type.");
        }
        if (entryName == null) {
            entryName = complexItem.getName();
        }
        ProtoSchemaMessageEntry messageEntry = new ProtoSchemaMessageEntry(entryName, nsPrefix);
        messageEntry.setAnnotation(complexItem.getAnnotation());

        processAttributes(complexItem.getAttributes(), complexItem.getAnyAttribute(), messageEntry);
        processParticle(complexItem.getParticle(), messageEntry, parent);
        processContentModel(complexItem.getContentModel(), messageEntry, parent);

        if (messageEntry.isPopulated()) {
            messageEntry.setVersionAnnotation(
                    protoSchemaEntryMap.getRootNamespacePrefix() + protoSchemaEntryMap.getVersion());
            protoSchemaEntryMap.addEntry(messageEntry);
            return new QName(
                    namespaceMap.getUri(messageEntry.getNamespacePrefix()),
                    messageEntry.getTitle(),
                    messageEntry.getNamespacePrefix());
        }
        return null;
    }

    private void processParticle(
            XmlSchemaParticle particle, ProtoSchemaAbstractEntry entry, ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
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
            XmlSchemaObjectBase item, ProtoSchemaAbstractEntry entry, ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
        if (item instanceof XmlSchemaElement) {
            XmlSchemaType type = ((XmlSchemaElement) item).getSchemaType();
            QName itemType = ((XmlSchemaElement) item).getSchemaTypeName();

            // Only evaluate an element that is a concrete definition as opposed to a leaf referencing an
            // el
            if (type != null && itemType == null) {
                itemType =
                        processElement(
                                (XmlSchemaElement) item, entry.getTitle(), entry.getNamespacePrefix(), entry);
            } else {
                itemType = ((XmlSchemaElement) item).getSchemaTypeName();
            }

            String name = ((XmlSchemaElement) item).getName();
            boolean repeated = ((XmlSchemaElement) item).getMaxOccurs() > 1;

            ProtoSchemaField field = new ProtoSchemaField(name, itemType, repeated);
            field.setVersionAnnotation(
                    protoSchemaEntryMap.getRootNamespacePrefix() + protoSchemaEntryMap.getVersion());
            field.setAnnotation(((XmlSchemaElement) item).getAnnotation());
            entry.addField(field);
        } else if (item instanceof XmlSchemaAny) {
            boolean repeated = ((XmlSchemaAny) item).getMaxOccurs() > 1;
            entry.addField(new ProtoSchemaField("any_value", null, repeated));
        } else {
            throw new XsdParseException("Unhandled item: " + item.getClass());
        }
    }

    private void processContentModel(
            XmlSchemaContentModel contentModel,
            ProtoSchemaAbstractEntry entry,
            ProtoSchemaAbstractEntry parent)
            throws XsdParseException {
        if (contentModel != null) {
            if (contentModel.getContent() instanceof XmlSchemaSimpleContentExtension) {
                XmlSchemaSimpleContentExtension content =
                        (XmlSchemaSimpleContentExtension) contentModel.getContent();
                processAttributes(content.getAttributes(), content.getAnyAttribute(), entry);
                entry.addField(new ProtoSchemaField("ext_value", content.getBaseTypeName()));
            } else if (contentModel.getContent() instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension content =
                        (XmlSchemaComplexContentExtension) contentModel.getContent();
                processParticle(content.getParticle(), entry, parent);
                entry.addField(new ProtoSchemaField("ext_value", content.getBaseTypeName()));
            } else {
                throw new XsdParseException(
                        "Unhandled content model: " + contentModel.getContent().getClass());
            }
        }
    }

    private void processAttributes(
            List<XmlSchemaAttributeOrGroupRef> attributes,
            XmlSchemaAnyAttribute anyAttribute,
            ProtoSchemaAbstractEntry entry)
            throws XsdParseException {
        if (attributes != null && attributes.size() > 0) {
            for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
                if (attribute instanceof XmlSchemaAttribute) {
                    String name = ((XmlSchemaAttribute) attribute).getName();
                    QName attributeType = getAttributeTypeName(((XmlSchemaAttribute) attribute));
                    ProtoSchemaField field = new ProtoSchemaField(name, attributeType);
                    field.setAnnotation(attribute.getAnnotation());
                    field.setVersionAnnotation(
                            protoSchemaEntryMap.getRootNamespacePrefix() + protoSchemaEntryMap.getVersion());
                    entry.addField(field);
                } else {
                    throw new XsdParseException("Unhandled attribute: " + attribute.getClass());
                }
            }
        }

        if (anyAttribute != null) {
            entry.addField(new ProtoSchemaField("any_attribute_value", null, true));
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
                reporter.addWarning("Found XmlSchemaRedefine node, ignoring.");
            }
        }

        return allSchema;
    }


    private QName getAttributeTypeName(XmlSchemaAttribute attribute) {
        QName qName = attribute.getSchemaTypeName();
        XmlSchemaSimpleType simpleItem = attribute.getSchemaType();

        if (qName != null) {
            return qName;
        }
        if (simpleItem != null) {
            XmlSchemaSimpleTypeRestriction restriction =
                    (XmlSchemaSimpleTypeRestriction) simpleItem.getContent();
            return restriction.getBaseTypeName(); // Always a STRING restriction
        }
        return DEFAULT_QNAME;
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
}
