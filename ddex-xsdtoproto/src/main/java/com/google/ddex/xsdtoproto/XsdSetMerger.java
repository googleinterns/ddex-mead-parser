package com.google.ddex.xsdtoproto;

import com.google.common.flogger.FluentLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The XsdSetMerger allows for multiple {@link com.google.ddex.xsdtoproto.ProtoSchema}'s of the same
 * major version to be parsed and merged into an umbrella schema. This umbrella schema will contain
 * all the fields from every included version.
 */
public class XsdSetMerger {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  Map<String, ProtoSchemaEntryMap> schemas;

  /** Instantiates a new Xsd Set Merger. */
  public XsdSetMerger() {
    schemas = new HashMap<>();
  }

  /**
   * Add schema definition to set merger before calling merge.
   *
   * @param schema A schema
   */
  public void addSchema(ProtoSchema schema) {
    String versionAsString = Integer.toString(schema.getVersionNumber());
    schemas.put(versionAsString, schema.getProtoSchemaEntryMap());
  }

  /**
   * Merge the added schemas to build the super schema.
   *
   * @return The super schema
   * @throws XsdParseException If any problem occurred when merging schemas
   */
  public ProtoSchema merge() throws XsdParseException {
    List<String> processingOrder = getProcessingOrder();
    ProtoSchemaEntryMap workingEntryMap = schemas.get(processingOrder.get(0));

    if (processingOrder.size() == 1) {
      return new ProtoSchema(workingEntryMap);
    }

    for (int i = 0; i < processingOrder.size() - 1; i++) {
      mergeStep(workingEntryMap, schemas.get(processingOrder.get(i + 1)));
    }

    return new ProtoSchema(workingEntryMap);
  }

  private List<String> getProcessingOrder() {
    List<String> processingOrder = new ArrayList<>(schemas.keySet());
    processingOrder.sort(String::compareToIgnoreCase);
    return processingOrder;
  }

  private void mergeStep(ProtoSchemaEntryMap workingEntryMap, ProtoSchemaEntryMap nextEntryMap)
      throws XsdParseException {
    if (!workingEntryMap
        .getNamespacePrefixEntryMap()
        .keySet()
        .containsAll(nextEntryMap.getNamespacePrefixEntryMap().keySet())) {
      throw new XsdParseException(
          "Incompatible namespaces in schema merge. "
              + "\n A: "
              + workingEntryMap.getNamespacePrefixes()
              + "\n B: "
              + nextEntryMap.getNamespacePrefixes());
    }

    workingEntryMap.setVersion(nextEntryMap.getVersion());
    workingEntryMap.setRootNamespacePrefix(nextEntryMap.getRootNamespacePrefix());

    for (String namespace : nextEntryMap.getNamespacePrefixes()) {
      Map<String, ProtoSchemaAbstractEntry> oldEntries =
          workingEntryMap.getNamespacePrefixEntryMap().get(namespace);
      Map<String, ProtoSchemaAbstractEntry> newEntries =
          nextEntryMap.getNamespacePrefixEntryMap().get(namespace);

      for (String type : newEntries.keySet()) {
        if (!oldEntries.containsKey(type)) {
          workingEntryMap.addEntry(newEntries.get(type));
        } else {
          ProtoSchemaAbstractEntry oldEntry = oldEntries.get(type);
          ProtoSchemaAbstractEntry newEntry = newEntries.get(type);
          ProtoSchemaAbstractEntry updatedEntry = mergeSingleEntry(oldEntry, newEntry);
          workingEntryMap.addEntry(updatedEntry);
        }
      }

      for (String type : oldEntries.keySet()) {
        if (!newEntries.containsKey(type)) {
          ProtoSchemaAbstractEntry deprecatedEntry =
              setDeprecatedEntry(
                  oldEntries.get(type),
                  nextEntryMap.getRootNamespacePrefix() + nextEntryMap.getVersion());
          workingEntryMap.addEntry(deprecatedEntry);
        }
      }
    }
  }

  private ProtoSchemaAbstractEntry setDeprecatedEntry(
      ProtoSchemaAbstractEntry deprecatedEntry, String deprecatedAt) {
    deprecatedEntry.setVersionAnnotation(
        deprecatedEntry.getVersionAnnotation() + " -> Removed in " + deprecatedAt);
    return deprecatedEntry;
  }

  private ProtoSchemaAbstractEntry mergeSingleEntry(
      ProtoSchemaAbstractEntry workingEntry, ProtoSchemaAbstractEntry newEntry)
      throws XsdParseException {
    if (!workingEntry.getNamespacePrefix().equals(newEntry.getNamespacePrefix())
        || !workingEntry.getTitle().equals(newEntry.getTitle())) {
      throw new XsdParseException("Entry mismatch.");
    }

    workingEntry.setVersionAnnotation(
        workingEntry.getVersionAnnotation() + " -> " + newEntry.getVersionAnnotation());
    Map<String, ProtoSchemaField> oldFields = workingEntry.getFieldMap();
    Map<String, ProtoSchemaField> newFields = newEntry.getFieldMap();

    for (String fieldName : newFields.keySet()) {
      if (!oldFields.containsKey(fieldName)) {
        workingEntry.addField(newEntry.getFieldMap().get(fieldName));
      } else {
        String localPartOld = oldFields.get(fieldName).getFieldType().getLocalPart();
        String localPartNew = newFields.get(fieldName).getFieldType().getLocalPart();
        if (!localPartNew.equals(localPartOld)) {
          logger.atInfo().log(
              "Field: "
                  + fieldName
                  + " type updated: "
                  + localPartOld
                  + " -> "
                  + localPartNew
                  + " in type "
                  + workingEntry.getTitle());
          ProtoSchemaField updatedField = newEntry.getFieldMap().get(fieldName);
          updatedField.setVersionAnnotation(
              updatedField.getVersionAnnotation()
                  + " -> Updated in "
                  + newEntry.getVersionAnnotation());
          workingEntry.addField(updatedField);
        }
      }
    }

    for (String fieldName : oldFields.keySet()) {
      if (!newFields.containsKey(fieldName)) {
        ProtoSchemaField deprecatedField =
            setDeprecatedField(oldFields.get(fieldName), newEntry.getVersionAnnotation());
        workingEntry.addField(deprecatedField);
      }
    }

    return workingEntry;
  }

  private ProtoSchemaField setDeprecatedField(
      ProtoSchemaField deprecatedField, String deprecatedAt) {
    deprecatedField.setVersionAnnotation(
        deprecatedField.getVersionAnnotation() + " -> Removed in " + deprecatedAt);
    deprecatedField.setDeprecated(true);
    return deprecatedField;
  }
}
