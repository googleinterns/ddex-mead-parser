package com.google.ddexmeadparser;

import com.google.protobuf.Message;
import mead10.Mead;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URI;
import java.net.URISyntaxException;

public class MeadBuilderResolver {
    public static Message.Builder getBuilder(Document document) throws MeadConversionException {
        Node root = MeadConverter.getRootNode(document);
        int versionNumber = getMeadVersionNumber(root);
        int majorVersionNumber = getMeadMajorVersionNumber(root);

        if (majorVersionNumber == 4) {
            return ern42.Ern.ern_NewReleaseMessage.newBuilder();
        } else if (majorVersionNumber == 3) {
            return ern383.Ern.NewReleaseMessage.newBuilder();
        } else {
            throw new MeadConversionException("Unsupported message version " + versionNumber + ". Temporarily blocking issue");
        }
    }

    private static int getMeadMajorVersionNumber(Node root) throws MeadConversionException {
        String schemaLocation = root.getAttributes().getNamedItem("xmlns:ern").getNodeValue();
        try {
            String uri = new URI(schemaLocation).getPath();
            String schemaVersion = uri.substring(uri.lastIndexOf('/') + 1);
            return Integer.parseInt(schemaVersion.substring(0, 1));
        } catch (URISyntaxException e) {
            throw new MeadConversionException("Malformed URI for schema location. Could not determine version number", e);
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
