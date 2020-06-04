import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProtoSchemaBuilder {
  private final NamespaceManager namespaces;
  private final List<XmlSchema> schemaSet;
  private final List<WrappedXmlSchema> wrappedSchemaSet;
  private final EntryContainer entryContainer;

  private XmlSchema baseSchema;

  private boolean xsdIngested;

  public ProtoSchemaBuilder() {
    namespaces = new NamespaceManager();
    entryContainer = new EntryContainer();
    schemaSet = new ArrayList<>();
    wrappedSchemaSet = new ArrayList<>();

    xsdIngested = false;
  }

  public boolean ingestXsdFromPath(String path) throws IOException {
    File initialFile = new File(path);
    InputStream xsdFile = new FileInputStream(initialFile);

    XmlSchemaCollection schemaCol = new XmlSchemaCollection();

    baseSchema = schemaCol.read(new StreamSource(xsdFile));
    schemaSet.add(baseSchema);
    xsdIngested = true;

    // TODO add check for failure
    return true;
  }

  public EntryContainer parseXsd() {
    if (!xsdIngested) {
      System.err.println("No XSD to process");
      return null;
    }

    initializeSchemaSet();
    processSchemaSet();

    return entryContainer;
  }

  // TODO fix the import/include not being fully recursive (does it need to be)
  private void initializeSchemaSet() {
    List<XmlSchemaExternal> schemaExternals = baseSchema.getExternals();

    for (XmlSchemaExternal external : schemaExternals) {
      if (external instanceof XmlSchemaImport) {
        XmlSchema importedSchema = external.getSchema();
        schemaSet.add(importedSchema);
      } else if (external instanceof XmlSchemaInclude) {
        XmlSchema includedSchema = external.getSchema();
        schemaSet.add(includedSchema);
      } else if (external instanceof XmlSchemaRedefine) {
        XmlSchema redefinedSchema = external.getSchema();
        // TODO handle redefines? Not be needed for MEAD specifically
      }
    }

    // Set namespace map
    for (XmlSchema schema : schemaSet) {
      NamespacePrefixList prefixes = schema.getNamespaceContext();
      namespaces.populateFromContext(prefixes);
    }

    // Add all schemas to the wrapped schema set to preserve their namespace
    for (XmlSchema schema : schemaSet) {
      String targetNamespace = schema.getTargetNamespace();
      WrappedXmlSchema wSchema = new WrappedXmlSchema(namespaces.getUri(targetNamespace), namespaces.getPrefix(targetNamespace), schema);
      wrappedSchemaSet.add(wSchema);
    }
  }

  private void processSchemaSet() {
    for (WrappedXmlSchema wSchema : wrappedSchemaSet) {
      processSchema(wSchema);
    }
  }

  private void processSchema(WrappedXmlSchema wSchema) {
    System.out.println("Processing wrapped schema -> " + wSchema.getPrefix() + " " + wSchema.getUri());
    String nsPrefix = wSchema.getPrefix();
    List<XmlSchemaObject> schemaItems = wSchema.getSchema().getItems();

    for (XmlSchemaObject item : schemaItems) {
      if (item instanceof XmlSchemaSimpleType) {
        processSimple((XmlSchemaSimpleType) item, null, nsPrefix, null);
      } else if (item instanceof XmlSchemaComplexType) {
        processComplex((XmlSchemaComplexType) item, null, nsPrefix, null);
      } else if (item instanceof XmlSchemaElement) {
        processElement((XmlSchemaElement) item, null, nsPrefix, null);
      }
    }
  }


  private QName processElement(XmlSchemaElement elementItem, String entry, String nsPrefix, AbstractEntry parent) {
    if (elementItem.getSchemaType() instanceof XmlSchemaSimpleType) {
      return processSimple((XmlSchemaSimpleType) elementItem.getSchemaType(), elementItem.getName(), nsPrefix, parent);
    } else if (elementItem.getSchemaType() instanceof XmlSchemaComplexType) {
      return processComplex((XmlSchemaComplexType) elementItem.getSchemaType(), elementItem.getName(), nsPrefix, parent);
    } else {
      throw new Error("Unable to process element " + elementItem.getClass());
    }
  }

  private QName processSimple(XmlSchemaSimpleType simpleItem, String entryName, String nsPrefix, AbstractEntry parent) {
    if (entryName == null && simpleItem.getName() == null) {
      throw new Error("No name for simple entry");
    }
    entryName = entryName != null ? entryName : simpleItem.getName();

    if (simpleItem.getContent() instanceof XmlSchemaSimpleTypeUnion) {
      return processSimpleUnion((XmlSchemaSimpleTypeUnion)simpleItem.getContent(), entryName, nsPrefix, parent);
    } else if (simpleItem.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
      return processSimpleRestriction((XmlSchemaSimpleTypeRestriction)simpleItem.getContent(), entryName, nsPrefix, parent);
    } else {
      throw new Error("Unhandled simple type " + simpleItem.getClass());
    }
  }

  // TODO Implement
  private QName processSimpleUnion(XmlSchemaSimpleTypeUnion union, String entryName,String nsPrefix, AbstractEntry parent) {
    MessageEntry messageEntry = new MessageEntry(entryName, nsPrefix);
    messageEntry.addField(new ProtoField("auto_value"));
    entryContainer.addEntry(messageEntry);
    return new QName(namespaces.getUri(messageEntry.getNamespacePrefix()), messageEntry.getTitle(), messageEntry.getNamespacePrefix());
  }

  private QName processSimpleRestriction(XmlSchemaSimpleTypeRestriction restriction, String entryName, String nsPrefix, AbstractEntry parent) {
    List<XmlSchemaFacet> facets = restriction.getFacets();

    if (isEnumFacetList(facets)) {
      EnumEntry enumEntry = new EnumEntry(entryName, nsPrefix);
      for (XmlSchemaFacet facet : facets) {
        ProtoField field = new ProtoField(facet.getValue().toString());
        enumEntry.addField(field);
      }
      if (enumEntry.hasFields()) {
        entryContainer.addEntry(enumEntry);
        return new QName(namespaces.getUri(enumEntry.getNamespacePrefix()), enumEntry.getTitle(), enumEntry.getNamespacePrefix());
      }
    } else if (parent == null) {
      MessageEntry messageEntry = new MessageEntry(entryName, nsPrefix);
      QName restrictionQName = restriction.getBaseTypeName(); // Always a STRING restriction
      messageEntry.addField(new ProtoField("auto_value", restrictionQName));
      entryContainer.addEntry(messageEntry);
      return new QName(namespaces.getUri(messageEntry.getNamespacePrefix()), messageEntry.getTitle(), messageEntry.getNamespacePrefix());
    }
    return null;
  }

  private QName processComplex(XmlSchemaComplexType complexItem, String entryName, String nsPrefix, AbstractEntry parent) {
    if (entryName == null && complexItem.getName() == null) {
      throw new Error("Complex item had no name");
    }
    entryName = entryName != null ? entryName : complexItem.getName();
    MessageEntry messageEntry = new MessageEntry(entryName, nsPrefix);

    processAttributes(complexItem.getAttributes(), complexItem.getAnyAttribute(), messageEntry, parent);
    processParticle(complexItem.getParticle(), messageEntry, parent);
    processContentModel(complexItem.getContentModel(), messageEntry, parent);

    if (messageEntry.hasFields()) {
      entryContainer.addEntry(messageEntry);
      return new QName(namespaces.getUri(messageEntry.getNamespacePrefix()), messageEntry.getTitle(), messageEntry.getNamespacePrefix());
    }
    return null;
  }

  private void processAttributes(List<XmlSchemaAttributeOrGroupRef> attributes, XmlSchemaAnyAttribute anyAttribute, AbstractEntry entry, AbstractEntry parent) {
    if (attributes != null && attributes.size() > 0) {
      for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
        if (attribute instanceof XmlSchemaAttribute) {
          String name = ((XmlSchemaAttribute) attribute).getName();
          QName attributeType = getAttributeTypeName(((XmlSchemaAttribute) attribute));
          entry.addField(new ProtoField(name, attributeType));
        } else {
          throw new Error("Unhandled attribute");
        }
      }
    }

    if (anyAttribute != null) {
      entry.addField(new ProtoField("any_attribute_value", null, true));
    }
  }

  // TODO handle repeated nested choice (AwardForParty) - Need to test this with a custom XSD
  private void processParticle(XmlSchemaParticle particle, AbstractEntry entry, AbstractEntry parent) {
    if (particle != null) {
      if (particle instanceof XmlSchemaSequence) {
        List<XmlSchemaSequenceMember> items = ((XmlSchemaSequence)particle).getItems();
        for (XmlSchemaSequenceMember item : items) {
          if (item instanceof XmlSchemaSequence || item instanceof XmlSchemaChoice) {
            processParticle((XmlSchemaParticle)item, entry, parent);
          } else {
            processItem(item, entry, parent);
          }
        }
      }

      if (particle instanceof XmlSchemaChoice) {
        List<XmlSchemaChoiceMember> items = ((XmlSchemaChoice)particle).getItems();
        for (XmlSchemaChoiceMember item : items) {
          if (item instanceof XmlSchemaSequence || item instanceof XmlSchemaChoice) {
            processParticle((XmlSchemaParticle)item, entry, parent);
          } else {
            processItem(item, entry, parent);
          }
        }
      }
    }
  }

  private void processItem(XmlSchemaObjectBase item, AbstractEntry entry, AbstractEntry parent) {
    if (item instanceof XmlSchemaElement) {
      XmlSchemaType type = ((XmlSchemaElement) item).getSchemaType();
      QName itemType = ((XmlSchemaElement) item).getSchemaTypeName();

      // Only evaluate an element that is a concrete definition as opposed to a leaf referencing an element
      if (type != null && itemType == null) {
        itemType = processElement((XmlSchemaElement)item, entry.getTitle(), entry.getNamespacePrefix(), entry);
      } else {
        itemType = ((XmlSchemaElement) item).getSchemaTypeName();
      }

      String name = ((XmlSchemaElement)item).getName();
      boolean repeated = ((XmlSchemaElement) item).getMaxOccurs() > 1;
      entry.addField(new ProtoField(name, itemType, repeated));
    } else if (item instanceof XmlSchemaAny) {
      boolean repeated = ((XmlSchemaAny) item).getMaxOccurs() > 1;
      entry.addField(new ProtoField("any_value", null, repeated));
    } else {
      throw new Error("Unhandled item " + item.getClass());
    }
  }

  private void processContentModel(XmlSchemaContentModel contentModel, AbstractEntry entry, AbstractEntry parent) {
    if (contentModel != null) {
      if (!(contentModel.getContent() instanceof XmlSchemaSimpleContentExtension)) {      // Assuming only simple extension, there are more
        throw new Error("Unhandled content " + contentModel.getContent().getClass());
      }

      XmlSchemaSimpleContentExtension content = (XmlSchemaSimpleContentExtension) contentModel.getContent();

      processAttributes(content.getAttributes(), content.getAnyAttribute(), entry, parent);
      entry.addField(new ProtoField("ext_value", content.getBaseTypeName()));
    }
  }

  // Default to string
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

  // TODO move to utils
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

  private boolean isPatternFacetList(List<XmlSchemaFacet> facets) {
    if (facets.size() <= 0) {
      return false;
    }
    for (XmlSchemaFacet facet : facets) {
      if (!(facet instanceof XmlSchemaPatternFacet)) {
        return false;
      }
    }
    return true;
  }
}

/*
- IngestXSD
- GenerateProto
    - Get all schemas (include, import) and collect all namespaces
        - Detect namespace collisions and circular references
    - Go over all the schemas and for each (schema/namespace?) store the candidates (enum/message)
        - Store in maps -> keep track of which file stores what, and store remember dependencies for the output
    -

    - For ENUM - SimpleType -> Containing an Enumeration
    - For MESSAGE - ComplexType -> Sequence -> Elements
                  - ComplexType -> SimpleContent -> Attributes
                  - Element -> ComplexType -> Choice / Sequence
    - Edge cases include
        - AnyAttribute and Any
        - Choices
 */

