import java.io.IOException;

public class DdexMeadParser {
  public static void main(String args[]) throws IOException {
    System.out.println("Launched XSD parse tester");

    ProtoSchemaBuilder protoBuilder = new ProtoSchemaBuilder(/* Options from cmd line */ );
    /* Definitely take this as input from cmd line */
    protoBuilder.ingestXsdFromPath("src/main/resources/release-notification.xsd");
    CandidateContainer candidateContainer = protoBuilder.parseXsd();

    ProtoWriter protoWriter = new ProtoWriter(/* Optios from cmd line */);
    protoWriter.serialize(candidateContainer);
  }
  // TODO Smart imports
  // TODO Smart circular dep
  // TODO Smart field tracking per message
  //
}
