import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.XmlFormat;

import ern.Ern;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class DdexMeadParser {
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException, Descriptors.DescriptorValidationException {
    // Build custom object representation of XSD (Xml schema) to be used by the ProtoDescriptorBuilder to create the file descriptor
    ProtoSchemaBuilder protoBuilder = new ProtoSchemaBuilder(/* Options from cmd line */ );
    protoBuilder.ingestXsdFromPath("src/prototype/resources/ern42/release-notification.xsd");
    EntryContainer entryContainer = protoBuilder.parseXsd();

    // Create protobuf FileDescriptor for the schema
    ProtoDescriptorBuilder protoDescriptorBuilder = new ProtoDescriptorBuilder();
    Descriptors.FileDescriptor fileDescriptor = protoDescriptorBuilder.buildFileDescriptor(entryContainer);

    // OPTIONALLY write the protobuf schema to file
    ProtoSchemaWriter protoSchemaWriter = new ProtoSchemaWriter();
    protoSchemaWriter.serialize(entryContainer);

    // Write an (automatically) merge-able version of the message XML
    XmlFixer xmlFixer = new XmlFixer(fileDescriptor);
    String rootName = xmlFixer.fixFromPath("src/prototype/resources/3 MixedMedia.xml");

    // Get builder for message being parsed | TODO Add null check
    Descriptors.Descriptor rootMessage = fileDescriptor.findMessageTypeByName(rootName);
    DynamicMessage.Builder builder = DynamicMessage.newBuilder(rootMessage);

    // Merge transformed XML with generated builder
    File initialFile = new File("src/prototype/resources/3 MixedMedia.xmlr");
    InputStream asXml = new FileInputStream(initialFile);
    XmlFormat xmlFormat = new XmlFormat();
    xmlFormat.merge(asXml, builder);

    // Output the proto message
    // Now that we have the message we can message.writeTo() to an output stream
    DynamicMessage message = builder.build();
    ByteString messageByteString = message.toByteString();

    Message output = null;
    if (rootName.equals("NewReleaseMessage")) {
      output = Ern.NewReleaseMessage.parseFrom(messageByteString);
    } else if (rootName.equals("PurgeReleaseMessage")) {
      output = Ern.PurgeReleaseMessage.parseFrom(messageByteString);
    }

    System.out.println(output.getClass());
  }
}
