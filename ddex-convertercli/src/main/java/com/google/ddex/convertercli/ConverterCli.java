package com.google.ddex.convertercli;

import com.google.common.flogger.FluentLogger;
import com.google.ddex.xmltoproto.MessageBuilderResolver;
import com.google.ddex.xmltoproto.MessageParseException;
import com.google.ddex.xmltoproto.MessageParser;
import com.google.ddex.xsdtoproto.ProtoSchema;
import com.google.ddex.xsdtoproto.XsdParseException;
import com.google.ddex.xsdtoproto.XsdParser;
import com.google.ddex.xsdtoproto.XsdSetMerger;
import com.google.protobuf.Message;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/** Command line tool for converting DDEX XSD and XML to Protocol Buffer. */
public class ConverterCli {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static ConverterOptions options;

  /**
   * Private constructor since the ConverterCli should only be used via Command Line.
   */
  private ConverterCli() {
  }

  public static void main(String[] args) {
    try {
      ConverterCli.options = new ConverterOptions(args);
      ConverterCli.parse();
    } catch (InvalidOptionsException | XsdParseException | MessageParseException | IOException e) {
      e.printStackTrace();
      ConverterOptions.showCommandUsage();
    }
  }

  private static void parse()
      throws IOException, XsdParseException, MessageParseException, InvalidOptionsException {
    switch (options.getInputType()) {
      case "message":
        parseXml();
        break;
      case "schema":
        parseXsd();
        break;
      case "schema_set":
        parseXsdSet();
        break;
      default:
        throw new InvalidOptionsException("Invalid inputType specified: " + options.getInputType());
    }
  }

  private static void parseXml() throws MessageParseException, IOException {
    logger.atInfo().log("Started message parse on: " + options.getInputFile().getName());
    FileReader fileIn = new FileReader(options.getInputFile());
    Message.Builder messageBuilder = MessageBuilderResolver.getBuilder(options.getInputFile());
    Message protoMessage = MessageParser.parse(fileIn, messageBuilder);

    writeMessage(protoMessage);
  }

  private static void parseXsd() throws XsdParseException, IOException {
    logger.atInfo().log("Started schema parse on: " + options.getInputFile().getName());
    FileReader fileIn = new FileReader(options.getInputFile());
    ProtoSchema protoSchema = XsdParser.parse(fileIn);

    writeSchema(protoSchema);
  }

  private static void parseXsdSet() throws XsdParseException, IOException {
    XsdSetMerger schemaSetMerger = new XsdSetMerger();

    for (File schemaFile : options.getInputFileList()) {
      logger.atInfo().log("Started schema set parse on folder: " + schemaFile.getName());
      FileReader fileIn = new FileReader(schemaFile);
      ProtoSchema schema = XsdParser.parse(fileIn);
      schemaSetMerger.addSchema(schema);
    }

    ProtoSchema finalSchema = schemaSetMerger.merge();
    writeSchema(finalSchema);
  }

  private static void writeSchema(ProtoSchema schema) throws IOException {
    String rootNamespace = schema.getRootNamespace();
    String packageName = schema.getPackageName();

    Set<String> namespaces = schema.getSchemaStringMap().keySet();

    for (String namespace : namespaces) {
      File file = getSchemaOutputFile(rootNamespace, packageName, namespace);
      file.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(file, false)) {
        writer.write(schema.getSchemaStringMap().get(namespace));
      }
      logger.atInfo().log(schema.getSchemaStringMap().get(namespace));
    }
  }

  private static File getSchemaOutputFile(
      String rootNamespace, String packageName, String namespace) {
    File outputFile;
    if (options.getOutputDirectory() != null) {
      outputFile =
          new File(
              options.getOutputDirectory().getAbsolutePath()
                  + "/"
                  + rootNamespace
                  + "/"
                  + packageName
                  + "/"
                  + namespace
                  + ".proto");
    } else {
      outputFile =
          new File("./proto/" + rootNamespace + "/" + packageName + "/" + namespace + ".proto");
    }
    return outputFile;
  }

  private static void writeMessage(Message message) throws IOException {
    // Get filename without extension
    String fileName = options.getInputFile().getName().replaceFirst("[.][^.]+$", "") + ".pb";
    File file = getMessageOutputFile(fileName);
    file.getParentFile().mkdirs();
    try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
      message.writeTo(outputStream);
    }
    logger.atInfo().log(message.toString());
  }

  private static File getMessageOutputFile(String fileName) {
    File outputFile;
    if (options.getOutputDirectory() != null) {
      outputFile = new File(options.getOutputDirectory().getAbsolutePath() + "/" + fileName);
    } else {
      outputFile = new File("./message/" + fileName);
    }
    return outputFile;
  }
}
