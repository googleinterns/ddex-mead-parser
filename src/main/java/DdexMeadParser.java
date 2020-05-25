import java.io.IOException;

public class DdexMeadParser {
  public static void main(String args[]) throws IOException {
    System.out.println("Launched XSD parse tester");

    ProtoSchemaBuilder protoBuilder = new ProtoSchemaBuilder(/* Options from cmd line */ );

    /* Definitely take this as input from cmd line */
    protoBuilder.ingestXsdFromPath("src/main/resources/meadex.xsd");
    protoBuilder.generateProto();
  }
}
