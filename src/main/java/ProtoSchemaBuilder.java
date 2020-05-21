import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProtoSchemaBuilder {
    // Data containers
    private NamespaceManager namespaces;
    private XmlSchema baseSchema;
    private List<WrappedXmlSchema> allSchemas;
    private List<EntryCandidate> candidates;

    // Flags
    private boolean xsdIngested;

    public ProtoSchemaBuilder() {
        namespaces = new NamespaceManager();
        allSchemas = new ArrayList<>();
        candidates = new ArrayList<>();
        xsdIngested = false;
    }

    public void ingestXsdFromPath(String path) throws IOException {
        File initialFile = new File(path);
        InputStream xsdFile = new FileInputStream(initialFile);

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();

        baseSchema = schemaCol.read(new StreamSource(xsdFile));
        xsdIngested = true;
    }

    public void generateProto() {
        if (!xsdIngested) {
            System.out.println("No XSD to process");
            return;
        }

        initializeNamespaces();
        initializeSchemaList();
        processAllSchemas();
    }

    // Namespace mapper stores Prefix <-> Uri mapping - use for types
    private void initializeNamespaces() {
        namespaces = new NamespaceManager();
        NamespacePrefixList nsPrefixes = baseSchema.getNamespaceContext();
        namespaces.populateFromContext(nsPrefixes);
        namespaces.printMappings();
    }

    // TODO fix the import/include not being
    private void initializeSchemaList() {
        List<XmlSchema> linkedSchemas = new ArrayList<>();
        List<XmlSchemaObject> baseSchemaItems = baseSchema.getItems();

        for (XmlSchemaObject item : baseSchemaItems) {
            if (item instanceof XmlSchemaImport) {
                XmlSchema importedSchema = ((XmlSchemaImport)item).getSchema();
                linkedSchemas.add(importedSchema);
            }
            else if (item instanceof XmlSchemaInclude) {
                XmlSchema includedSchema = ((XmlSchemaInclude)item).getSchema();
                linkedSchemas.add(includedSchema);
            }
        }

        // Add all schemas to WrapperSchema list
        linkedSchemas.add(baseSchema);
        for (XmlSchema schema : linkedSchemas) {
            String targetNamespace = schema.getTargetNamespace();
            allSchemas.add(new WrappedXmlSchema(namespaces.getUri(targetNamespace), namespaces.getPrefix(targetNamespace), schema));
            System.out.println("Added schema uri: " + namespaces.getUri(targetNamespace) + "  - pref  " + namespaces.getPrefix(targetNamespace));
        }
    }


    private void processAllSchemas() {
        for (WrappedXmlSchema wSchema : allSchemas) {
            processSingleSchema(wSchema);
        }
    }

    private void processSingleSchema(WrappedXmlSchema wSchema) {
        String currentSchemaPrefix = wSchema.getPrefix();
        List<XmlSchemaObject> schemaItems = wSchema.getSchema().getItems();

        for (XmlSchemaObject item : schemaItems) {
            System.out.println(item.getClass() + " - " + currentSchemaPrefix);
            if (item instanceof XmlSchemaSimpleType) {
                candidates.add(buildSimpleType((XmlSchemaSimpleType)item, currentSchemaPrefix));
            } else if (item instanceof XmlSchemaComplexType) {
                //candidates.add(buildComplexType((XmlSchemaComplexType)item));
            } else if (item instanceof XmlSchemaElement) {
                //candidates.add(buildElement((XmlSchemaElement)item));
            }
        }
    }

    private EnumCandidate buildSimpleType(XmlSchemaSimpleType simpleNode, String nsPrefix) {
        String enumName = simpleNode.getName();

        EnumCandidate enumCandidate = new EnumCandidate(enumName, nsPrefix);

        XmlSchemaSimpleTypeContent content = simpleNode.getContent();
        if (content instanceof XmlSchemaSimpleTypeRestriction) {
            XmlSchemaSimpleTypeRestriction r = (XmlSchemaSimpleTypeRestriction)content;
            for (XmlSchemaFacet facet : r.getFacets()) {
                ProtoField field = new ProtoField(facet.getValue().toString());
                enumCandidate.addField(field);
            }
        } else {
            System.err.println("Unable to parse a SimpleType");
        }

        return enumCandidate;
    }
}
