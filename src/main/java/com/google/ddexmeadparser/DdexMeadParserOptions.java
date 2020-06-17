package com.google.ddexmeadparser;

import java.io.File;

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
