package com.google.ddexmeadparser;

import mead.Mead.MeadMessage;
import java.io.File;

public class MeadConverter {
    public MeadMessage convert(File input) throws MeadParseException {
        MeadMessage.Builder messageBuilder = MeadMessage.newBuilder();
        return messageBuilder.build();
    }
}
