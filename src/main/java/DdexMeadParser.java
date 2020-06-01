import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.XmlFormat;
import ern.Ern;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class DdexMeadParser {
  public static void main(String args[]) throws IOException, ParserConfigurationException, SAXException, TransformerException {
    System.out.println("Launched XSD parse tester");
    ProtoSchemaBuilder protoBuilder = new ProtoSchemaBuilder(/* Options from cmd line */ );
    /* Definitely take this as input from cmd line */
    protoBuilder.ingestXsdFromPath("src/main/resources/release-notification.xsd");
    CandidateContainer candidateContainer = protoBuilder.parseXsd();

    ProtoWriter protoWriter = new ProtoWriter(/* Optios from cmd line */);
    protoWriter.serialize(candidateContainer);

    XmlFixer xmlFixer = new XmlFixer();
    xmlFixer.fixFromPath("src/main/resources/7 LongformMusicalWorkVideo.xml");

    Message.Builder builder = Ern.NewReleaseMessage.newBuilder();
    File initialFile = new File("src/main/resources/7 LongformMusicalWorkVideo.xmlr");
    InputStream asXml = new FileInputStream(initialFile);
    XmlFormat xmlFormat = new XmlFormat();
    xmlFormat.merge(asXml, builder);
  }
  // TODO Smart imports
  // TODO Smart circular dep
  // TODO Smart field tracking per message
  // protoc -I="src/main/proto" --java_out="src/main/java" "src/main/proto/ern/ern.proto"

  // TODO Issue with extensions needed an extra nest <ext_value></ext_value> - DONE TENTATIVE
  // TODO Issue with attributes in tags "Expected identifier. -" Should shift it into sub element, at same time as first issue fix - DONE TENTATIVE
  // TODO Issue with numbers / special chars in string? (starts with num / contains special chars)
  // TODO Issue with enum values not matching since I change them to Proto style and prepend Enum name
  // TODO Handle namespacing (ern:NewReleaseMessage)
}
