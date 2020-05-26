import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Attr;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtoSchemaBuilder {
  private final NamespaceManager namespaces;
  private final List<XmlSchema> schemaSet;
  private final List<WrappedXmlSchema> wrappedSchemaSet;
  private final CandidateContainer candidateContainer;

  private XmlSchema baseSchema;

  private boolean xsdIngested;

  public ProtoSchemaBuilder() {
    namespaces = new NamespaceManager();
    candidateContainer = new CandidateContainer();
    schemaSet = new ArrayList<>();
    wrappedSchemaSet = new ArrayList<>();

    xsdIngested = false;
  }

  public void ingestXsdFromPath(String path) throws IOException {
    File initialFile = new File(path);
    InputStream xsdFile = new FileInputStream(initialFile);

    XmlSchemaCollection schemaCol = new XmlSchemaCollection();

    baseSchema = schemaCol.read(new StreamSource(xsdFile));
    schemaSet.add(baseSchema);
    xsdIngested = true;
  }

  public void generateProto() {
    if (!xsdIngested) {
      System.out.println("No XSD to process");
      return;
    }

    initializeSchemaSet();
    processSchemaSet();
    buildProtoBySchema();
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
        // TODO handle redefines? May not be needed for MEAD specifically
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
      System.out.println("Processing wrapped schema -> " + wSchema.getPrefix() + " " + wSchema.getUri());
      processSchema(wSchema);
    }
  }

  private void processSchema(WrappedXmlSchema wSchema) {
    String nsPrefix = wSchema.getPrefix();
    List<XmlSchemaObject> schemaItems = wSchema.getSchema().getItems();

    for (XmlSchemaObject item : schemaItems) {
      if (item instanceof XmlSchemaSimpleType) {
        processSimple((XmlSchemaSimpleType) item, null, nsPrefix);
      } else if (item instanceof XmlSchemaComplexType) {
        processComplex((XmlSchemaComplexType) item, null, nsPrefix);
      } else if (item instanceof XmlSchemaElement) {
        processElement((XmlSchemaElement) item, null, nsPrefix);
      }
    }
  }

  private void processSimple(XmlSchemaSimpleType simpleItem, String candidateName, String nsPrefix) {
    if (candidateName == null && simpleItem.getName() == null) {
      throw new Error("No name for simple candidate");
    }
    candidateName = candidateName != null ? candidateName : simpleItem.getName();

    XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) simpleItem.getContent();
    QName restrictionQName = restriction.getBaseTypeName(); // Always a STRING restriction
    List<XmlSchemaFacet> facets = restriction.getFacets();

    if (isEnumFacetList(facets)) {
      EnumCandidate enumCandidate = new EnumCandidate(candidateName, nsPrefix);

      for (XmlSchemaFacet facet : facets) {
        ProtoField field = new ProtoField(facet.getValue().toString());
        enumCandidate.addField(field);
      }

      if (enumCandidate.hasFields()) {
        candidateContainer.addCandidate(enumCandidate);
      }
    }
  }

  private void processComplex(XmlSchemaComplexType complexItem, String candidateName, String nsPrefix) {
    if (candidateName == null && complexItem.getName() == null) {
      System.out.println("NO NAME??");
    }
    candidateName = candidateName != null ? candidateName : complexItem.getName();
    System.out.println(candidateName);

    MessageCandidate messageCandidate = new MessageCandidate(candidateName, nsPrefix);

    processAttributes(complexItem.getAttributes(), complexItem.getAnyAttribute(), messageCandidate);
    processParticle(complexItem.getParticle(), messageCandidate);
    processContentModel( complexItem.getContentModel(), messageCandidate);

    if (messageCandidate.hasFields()) {
      candidateContainer.addCandidate(messageCandidate);
    }
  }

  private void processAttributes(List<XmlSchemaAttributeOrGroupRef> attributes, XmlSchemaAnyAttribute anyAttribute, EntryCandidate candidate) {
    if (attributes != null && attributes.size() > 0) {
      for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
        if (attribute instanceof XmlSchemaAttribute) {
          String name = ((XmlSchemaAttribute) attribute).getName();
          QName attributeType = getAttributeTypeName(((XmlSchemaAttribute) attribute));
          candidate.addField(new ProtoField(name, attributeType));
        } else {
          throw new Error("Unhandled attribute");
        }
      }
    }

    if (anyAttribute != null) {
      candidate.addField(new ProtoField("any_attribute_value", null, true));
    }
  }

  // TODO handle repeated nested choice (AwardForParty)
  private void processParticle(XmlSchemaParticle particle, EntryCandidate candidate) {
    if (particle != null) {
      System.out.println("P RESOLVED");
      System.out.println(particle);

      if (particle instanceof XmlSchemaSequence) {
        List<XmlSchemaSequenceMember> items = ((XmlSchemaSequence)particle).getItems();
        for (XmlSchemaSequenceMember item : items) {
          if (item instanceof XmlSchemaSequence || item instanceof XmlSchemaChoice) {
             processParticle((XmlSchemaParticle)item, candidate);
          } else {
            processItem(item, candidate);
          }
        }
      }

      if (particle instanceof XmlSchemaChoice) {
        List<XmlSchemaChoiceMember> items = ((XmlSchemaChoice)particle).getItems();
        for (XmlSchemaChoiceMember item : items) {
          if (item instanceof XmlSchemaSequence || item instanceof XmlSchemaChoice) {
            processParticle((XmlSchemaParticle)item, candidate);
          } else {
            processItem(item, candidate);
          }
        }
      }
    }
  }

  private void processItem(XmlSchemaObjectBase item, EntryCandidate candidate) {
    if (item instanceof XmlSchemaElement) {
      String name = ((XmlSchemaElement)item).getName();
      QName itemType = ((XmlSchemaElement) item).getQName();
      boolean repeated = ((XmlSchemaElement) item).getMaxOccurs() > 1;
      candidate.addField(new ProtoField(name, itemType, repeated));
    } else if (item instanceof XmlSchemaAny) {
      boolean repeated = ((XmlSchemaAny) item).getMaxOccurs() > 1;
      candidate.addField(new ProtoField("any_value", null, repeated));
    } else {
      throw new Error("Unhandled item " + item.getClass());
    }
  }

  private void processContentModel(XmlSchemaContentModel contentModel, EntryCandidate candidate) {
    if (contentModel != null) {
      if (!(contentModel.getContent() instanceof XmlSchemaSimpleContentExtension)) {      // Assuming only simple extension, there are more
        throw new Error("Unhandled content " + contentModel.getContent().getClass());
      }

      XmlSchemaSimpleContentExtension content = (XmlSchemaSimpleContentExtension) contentModel.getContent();

      System.out.println("C RESOLVED");
      System.out.println(content);

      processAttributes(content.getAttributes(), content.getAnyAttribute(), candidate);
      candidate.addField(new ProtoField("extension_value"));
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

  private void processElement(XmlSchemaElement elementItem, String candidateName, String nsPrefix) {
    if (elementItem.getSchemaType() instanceof XmlSchemaComplexType) {
      processComplex((XmlSchemaComplexType) elementItem.getSchemaType(), elementItem.getName(), nsPrefix);
    } else if (elementItem.getSchemaType() instanceof XmlSchemaSimpleType) {
      processSimple((XmlSchemaSimpleType) elementItem.getSchemaType(), elementItem.getName(), nsPrefix);
    } else {
      System.err.println("Did not process an element");
    }
  }

  // TODO redo the function, separately as serializer and cleaner class
  private String buildProtoBySchema() {
    List<String> namespaces = candidateContainer.getNamespacePrefixes();
    Map<String, List<EntryCandidate>> namespaceMap =
        candidateContainer.getNamespacePrefixCandidateMap();

    for (String name : namespaces) {
      System.out.println("Serialize .proto for " + name);
      String protoString = "syntax = \"proto2\";\npackage " + name + ";\n";

      for (EntryCandidate cand : namespaceMap.get(name)) {
        int counter = 0;
        protoString += "enum " + cand.getTitle() + " {\n";
        for (ProtoField field : cand.getFields()) {
          protoString += "\t" + field.getFieldValue() + " = " + counter++ + ";\n";
        }
        protoString += "}\n";
      }

      // System.out.println(protoString);
    }
    return "";
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
