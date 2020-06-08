import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.googlecode.protobuf.format.XmlFormat;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class DdexMeadParser {
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException, Descriptors.DescriptorValidationException {
    ProtoSchemaBuilder protoBuilder = new ProtoSchemaBuilder(/* Options from cmd line */ );

    // Build custom representation of XML schema to be used by the DynamicProtoWriter
    protoBuilder.ingestXsdFromPath("src/main/resources/release-notification.xsd");
    EntryContainer entryContainer = protoBuilder.parseXsd();

    // Create protobuf FileDescriptor for the schema
    DynamicProtoWriter dynamicProtoWriter = new DynamicProtoWriter();
    List<DescriptorProtos.FileDescriptorProto> fileDescriptors = dynamicProtoWriter.serialize(entryContainer);
    Descriptors.FileDescriptor avsDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptors.get(1), new Descriptors.FileDescriptor[]{});
    Descriptors.FileDescriptor mainDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptors.get(0), new Descriptors.FileDescriptor[] { avsDescriptor });

    // Write an automatically merge-able version of the message XML
    XmlFixer xmlFixer = new XmlFixer(mainDescriptor);
    String rootName = xmlFixer.fixFromPath("src/main/resources/8 DjMix.xml");

    // Get builder for message being parsed | TODO Add null check
    Descriptors.Descriptor file = mainDescriptor.findMessageTypeByName(rootName);
    DynamicMessage.Builder builder = DynamicMessage.newBuilder(file);

    // Merge transformed XML with generated builder
    File initialFile = new File("src/main/resources/8 DjMix.xmlr");
    InputStream asXml = new FileInputStream(initialFile);
    XmlFormat xmlFormat = new XmlFormat();
    xmlFormat.merge(asXml, builder);

    // Output the proto message
    DynamicMessage message = builder.build();
    System.out.println(message.toString());
  }
}
