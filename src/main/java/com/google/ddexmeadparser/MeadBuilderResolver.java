package com.google.ddexmeadparser;

import com.google.protobuf.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URI;
import java.net.URISyntaxException;

public class MeadBuilderResolver {
    public static Message.Builder getBuilder(Document document) throws MeadConversionException {
        Node root = MeadConverter.getRootNode(document);
        int versionNumber = getMeadVersionNumber(root);

        if (versionNumber == 42 || versionNumber == 411) {
            return ern42.Ern.NewReleaseMessage.newBuilder();
        } else if (versionNumber == 35 || versionNumber == 381 || versionNumber == 382 || versionNumber == 383) {
            return ern383.Ern.NewReleaseMessage.newBuilder();
        } else {
            throw new MeadConversionException("Unsupported message version " + versionNumber + ". Temporarily blocking issue");
        }
    }

    private static int getMeadVersionNumber(Node root) throws MeadConversionException {
        String schemaLocation = root.getAttributes().getNamedItem("xmlns:ern").getNodeValue();
        try {
            String uri = new URI(schemaLocation).getPath();
            String schemaVersion = uri.substring(uri.lastIndexOf('/') + 1);
            return Integer.parseInt(schemaVersion);
        } catch (URISyntaxException e) {
            throw new MeadConversionException("Malformed URI for schema location. Could not determine version number", e);
        }
    }
}
