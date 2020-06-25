package com.google.ddexmeadparser;

import java.io.File;

/**
 * Options for the DdexMeadParser set via command line.
 */
public class DdexMeadParserOptions {
    /**
     * The enum Input type value.
     */
enum inputTypeValue {
        /**
         *Message input type value.
         */
MESSAGE,
        /**
         *Schema input type value.
         */
SCHEMA
    }

    /**
     * The Output directory.
     */
public File outputDirectory = null;
    /**
     * The Input file.
     */
public File inputFile = null;
    /**
     * The Input is directory.
     */
public boolean inputIsDirectory = false;
    /**
     * The Input type.
     */
public inputTypeValue inputType = null;
}
