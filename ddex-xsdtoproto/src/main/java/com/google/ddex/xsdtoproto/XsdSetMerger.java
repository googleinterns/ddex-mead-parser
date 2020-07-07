package com.google.ddex.xsdtoproto;

import com.google.common.flogger.FluentLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XsdSetMerger {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  Map<String, ProtoSchemaEntryMap> schemas;

  public XsdSetMerger() {
    schemas = new HashMap<>();
  }

  /**
   * Add schema definition to set merger before calling merge.
   *
   * @param schema the schema
   */
  public void addSchema(ProtoSchema schema) {
    String versionAsString = Integer.toString(schema.getVersionNumber());
    schemas.put(versionAsString, schema.getProtoSchemaEntryMap());
  }

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
    }
  }


  private ProtoSchemaAbstractEntry mergeSingleEntry(
          ProtoSchemaAbstractEntry workingEntry, ProtoSchemaAbstractEntry newEntry)
      throws XsdParseException {
    if (!workingEntry.getNamespacePrefix().equals(newEntry.getNamespacePrefix())
        || !workingEntry.getTitle().equals(newEntry.getTitle())) {
      throw new XsdParseException("Entry data mismatch.");
    }

    Map<String, ProtoSchemaField> oldFields = workingEntry.getFieldMap();
    Map<String, ProtoSchemaField> newFields = newEntry.getFieldMap();

    for (String fieldName : newFields.keySet()) {
      if (!oldFields.containsKey(fieldName)) {
        workingEntry.addField(newEntry.getFieldMap().get(fieldName));
      } else {
        String localPartOld = oldFields.get(fieldName).getFieldType().getLocalPart();
        String localPartNew = newFields.get(fieldName).getFieldType().getLocalPart();
        if (!localPartNew.equals(localPartOld)) {
          logger.atInfo().log("Field: " + fieldName + " Different types "
                  + localPartOld + " -> " + localPartNew + " in type " + workingEntry.getTitle());
          workingEntry.addField(newEntry.getFieldMap().get(fieldName));
        }
      }
    }

    return workingEntry;
  }


  private List<String> getProcessingOrder() {
    List<String> processingOrder = new ArrayList<>(schemas.keySet());
    processingOrder.sort(String::compareToIgnoreCase);
    return processingOrder;
  }
}