import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;

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

  // TODO fix the import/include not being fully recursive
  private void initializeSchemaSet() {
    List<XmlSchemaExternal> schemaExternals = baseSchema.getExternals();

    for (XmlSchemaExternal external : schemaExternals) {
      if (external instanceof XmlSchemaImport) {
        XmlSchema importedSchema = ((XmlSchemaImport) external).getSchema();
        schemaSet.add(importedSchema);
      } else if (external instanceof XmlSchemaInclude) {
        XmlSchema includedSchema = ((XmlSchemaInclude) external).getSchema();
        schemaSet.add(includedSchema);
      } else if (external instanceof XmlSchemaRedefine) {
        XmlSchema redefinedSchema = ((XmlSchemaRedefine) external).getSchema();
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
    if (candidateName != null) {
      // System.out.println(candidateName);
    } else if (simpleItem.getName() != null) {
      candidateName = simpleItem.getName();
      // System.out.println(simpleItem.getName() + " " + simpleItem.getQName());
    } else {
      // TODO ERROR?
      System.out.println("NO NAME??");
    }

    XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) simpleItem.getContent();
    QName restrictionQName = restriction.getBaseTypeName();
    List<XmlSchemaFacet> facets = restriction.getFacets();

    if (isEnumFacetList(facets)) {
      EnumCandidate enumCandidate = new EnumCandidate(candidateName, nsPrefix);
      for (XmlSchemaFacet facet : facets) {
        ProtoField field = new ProtoField(facet.getValue().toString());
        enumCandidate.addField(field);
      }
      candidateContainer.addCandidate(enumCandidate);
    }
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

  private void processComplex(XmlSchemaComplexType complexItem, String candidateName, String nsPrefix) {
    if (candidateName != null) {
      // System.out.println(candidateName);
    } else if (complexItem.getName() != null) {
      candidateName = complexItem.getName();
      // System.out.println(complexItem.getName() + " " + complexItem.getQName());
    } else {
      // TODO ERROR?
      System.out.println("NO NAME??");
    }
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

  // TODO redo the function, maybe need a structure map to figure out the steps to take this is a
  // hack
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

      System.out.println(protoString);
    }
    return "";
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
