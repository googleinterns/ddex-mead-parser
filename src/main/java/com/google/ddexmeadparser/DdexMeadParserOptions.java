package com.google.ddexmeadparser;

import java.io.File;

/**
 * Options for the DdexMeadParser set via command line.
 */
public class DdexMeadParserOptions {
    enum inputTypeValue {
        MESSAGE,
        SCHEMA
    }

    public File outputDirectory = null;
    public File inputFile = null;
    public boolean inputIsDirectory = false;
    public inputTypeValue inputType = null;
}
