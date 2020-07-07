package com.google.ddex.xmltoproto;

import com.google.common.flogger.FluentLogger;

import java.util.ArrayList;
import java.util.List;

public class MessageParseReporter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    List<String> warnings;

    public MessageParseReporter() {
        warnings = new ArrayList<>();
    }

    public void addWarning(String warning) {
        logger.atWarning().log(warning);
        warnings.add(warning);
    }
}
