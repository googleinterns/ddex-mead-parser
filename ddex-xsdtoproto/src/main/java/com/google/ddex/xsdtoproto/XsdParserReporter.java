package com.google.ddex.xsdtoproto;

import com.google.common.flogger.FluentLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * The XsdParseReporter stores any warnings generated by the {@link
 * com.google.ddex.xsdtoproto.XsdParser}. A reporter should be instantiated and passed in as a
 * parameter to the parse function call.
 */
public interface XsdParserReporter {
  void addProcessedSchema(String namespace);

  void addWarning(String warning);

  void addLog(String log);

  public static class DefaultXsdParserReporter implements XsdParserReporter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    List<String> processedSchemaNamespaces;
    List<String> warnings;
    List<String> logs;

    /** Constructor. */
    public DefaultXsdParserReporter() {
      processedSchemaNamespaces = new ArrayList<>();
      warnings = new ArrayList<>();
    }

    /**
     * Record a schema namespace that has been parsed.
     *
     * @param namespace the namespace of the schema being processed
     */
    public void addProcessedSchema(String namespace) {
      logger.atInfo().log("Processed " + namespace);
      processedSchemaNamespaces.add(namespace);
    }

    /**
     * Record a warning.
     *
     * @param warning The warning
     */
    public void addWarning(String warning) {
      logger.atWarning().log(warning);
      warnings.add(warning);
    }

    /**
     * Record a log.
     *
     * @param log The warning
     */
    public void addLog(String log) {
      logger.atFine().log(log);
      logs.add(log);
    }
  }
}
