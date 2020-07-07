package com.google.ddex.xsdtoproto;

import com.google.common.flogger.FluentLogger;

import java.util.ArrayList;
import java.util.List;

public class XsdParseReporter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    List<String> processedSchemaNamespaces;
    List<String> warnings;

    public XsdParseReporter() {
        processedSchemaNamespaces = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    public void addProcessedSchema(String namespace) {
        logger.atInfo().log("Processed " + namespace);
        processedSchemaNamespaces.add(namespace);
    }

    public void addWarning(String warning) {
        logger.atWarning().log(warning);
        warnings.add(warning);
    }
}
