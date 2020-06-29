package com.google.ddexmeadparser.experimental;

import com.google.ddexmeadparser.SchemaAbstractEntry;
import com.google.ddexmeadparser.SchemaConversionException;
import com.google.ddexmeadparser.SchemaEntryMap;
import com.google.ddexmeadparser.SchemaField;

import java.util.*;
import com.google.common.flogger.FluentLogger;

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
   * Add schema.
   *
   * @param schema the schema
   */
  public void addSchema(SchemaEntryMap schema) {
    String versionAsString = Integer.toString(schema.getVersion());
    schemas.put(versionAsString, schema);
  }

  /**
   * Merge schema entry map.
   *
   * @return the schema entry map
   * @throws SchemaConversionException the schema conversion exception
   */
  public SchemaEntryMap merge() throws SchemaConversionException {
    List<String> processingOrder = getProcessingOrder();
    SchemaEntryMap workingEntryMap = schemas.get(processingOrder.get(0));

    if (processingOrder.size() == 1) {
      return workingEntryMap;
    }

    for (int i = 0; i < processingOrder.size() - 1; i++) {
      mergeStep(workingEntryMap, schemas.get(processingOrder.get(i + 1)));
    }

    return workingEntryMap;
  }

  private void mergeStep(SchemaEntryMap workingEntryMap, SchemaEntryMap nextEntryMap)
      throws SchemaConversionException {
    if (!workingEntryMap
        .getNamespacePrefixEntryMap()
        .keySet()
        .containsAll(nextEntryMap.getNamespacePrefixEntryMap().keySet())) {
      throw new SchemaConversionException(
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

  private SchemaAbstractEntry mergeSingleEntry(
      SchemaAbstractEntry workingEntry, SchemaAbstractEntry newEntry)
      throws SchemaConversionException {
    if (!workingEntry.getNamespacePrefix().equals(newEntry.getNamespacePrefix())
        || !workingEntry.getTitle().equals(newEntry.getTitle())) {
      throw new SchemaConversionException("Entry data mismatch.");
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
          logger.atInfo().log(
              "Field: "
                  + fieldName
                  + " Different types "
                  + localPartOld
                  + " -> "
                  + localPartNew
                  + " in type "
                  + workingEntry.getTitle());
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
  public List<String> getProcessingOrder() {
    List<String> processingOrder = new ArrayList<>(schemas.keySet());
    processingOrder.sort(String::compareToIgnoreCase);
    return processingOrder;
  }
}
