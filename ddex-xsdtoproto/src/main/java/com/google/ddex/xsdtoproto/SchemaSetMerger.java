package com.google.ddex.xsdtoproto;

import com.google.common.flogger.FluentLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The type Schema set merger. */
public class SchemaSetMerger {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  /** The Schemas. */
  Map<String, SchemaEntryMap> schemas;

  /** Instantiates a new Schema set merger. */
  public SchemaSetMerger() {
    schemas = new HashMap<>();
  }

  /**
   * Add schema definition to set merger before calling merge.
   *
   * @param schema the schema
   */
  public void addSchema(ProtoSchema schema) {
    String versionAsString = Integer.toString(schema.getVersionNumber());
    schemas.put(versionAsString, schema.getSchemaEntryMap());
  }

  /**
   * Merge schema definitions that have been added.
   *
   * @return the schema entry map
   * @throws SchemaParseException the schema conversion exception
   */
  public ProtoSchema merge() throws SchemaParseException {
    List<String> processingOrder = getProcessingOrder();
    SchemaEntryMap workingEntryMap = schemas.get(processingOrder.get(0));

    if (processingOrder.size() == 1) {
      return new ProtoSchema(workingEntryMap);
    }

    for (int i = 0; i < processingOrder.size() - 1; i++) {
      mergeStep(workingEntryMap, schemas.get(processingOrder.get(i + 1)));
    }

    return new ProtoSchema(workingEntryMap);
  }

  /**
   * Merges schema definition between current, and new version.
   *
   * @param workingEntryMap current definition - mutated in place
   * @param nextEntryMap new updated entries to merge into current
   * @throws SchemaParseException
   */
  private void mergeStep(SchemaEntryMap workingEntryMap, SchemaEntryMap nextEntryMap)
      throws SchemaParseException {
    if (!workingEntryMap
        .getNamespacePrefixEntryMap()
        .keySet()
        .containsAll(nextEntryMap.getNamespacePrefixEntryMap().keySet())) {
      throw new SchemaParseException(
          "Incompatible namespaces in schema merge. "
              + "\n A: "
              + workingEntryMap.getNamespacePrefixes()
              + "\n B: "
              + nextEntryMap.getNamespacePrefixes());
    }

    workingEntryMap.setVersion(nextEntryMap.getVersion());
    workingEntryMap.setRootNamespacePrefix(nextEntryMap.getRootNamespacePrefix());

    for (String namespace : nextEntryMap.getNamespacePrefixes()) {
      Map<String, SchemaAbstractEntry> oldEntries =
          workingEntryMap.getNamespacePrefixEntryMap().get(namespace);
      Map<String, SchemaAbstractEntry> newEntries =
          nextEntryMap.getNamespacePrefixEntryMap().get(namespace);

      for (String type : newEntries.keySet()) {
        if (!oldEntries.containsKey(type)) {
          workingEntryMap.addEntry(newEntries.get(type));
        } else {
          SchemaAbstractEntry oldEntry = oldEntries.get(type);
          SchemaAbstractEntry newEntry = newEntries.get(type);
          SchemaAbstractEntry updatedEntry = mergeSingleEntry(oldEntry, newEntry);
          workingEntryMap.addEntry(updatedEntry);
        }
      }
    }
  }

  /**
   * Merges entry fields between current, and new definitions.
   *
   * @param workingEntry current entry
   * @param newEntry new updated entry to merge into current
   * @return SchemaAbstractEntry merged entry
   * @throws SchemaParseException
   */
  private SchemaAbstractEntry mergeSingleEntry(
      SchemaAbstractEntry workingEntry, SchemaAbstractEntry newEntry)
      throws SchemaParseException {
    if (!workingEntry.getNamespacePrefix().equals(newEntry.getNamespacePrefix())
        || !workingEntry.getTitle().equals(newEntry.getTitle())) {
      throw new SchemaParseException("Entry data mismatch.");
    }

    Map<String, SchemaField> oldFields = workingEntry.getFieldMap();
    Map<String, SchemaField> newFields = newEntry.getFieldMap();

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

  /**
   * Gets processing order.
   *
   * @return the processing order
   */
  private List<String> getProcessingOrder() {
    List<String> processingOrder = new ArrayList<>(schemas.keySet());
    processingOrder.sort(String::compareToIgnoreCase);
    return processingOrder;
  }
}
