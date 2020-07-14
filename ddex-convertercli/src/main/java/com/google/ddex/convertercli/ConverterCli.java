package com.google.ddex.convertercli;

import com.google.ddex.xsdtoproto.ProtoSchema;
import com.google.ddex.xsdtoproto.XsdParseException;
import com.google.ddex.xsdtoproto.XsdParser;
import com.google.ddex.xsdtoproto.XsdSetMerger;
import com.google.ddex.xmltoproto.MessageParseException;
import com.google.ddex.xmltoproto.MessageParser;
import com.google.protobuf.Message;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Set;

import com.google.common.flogger.FluentLogger;

/** Command line tool for converting DDEX XSD and XML to Protocol Buffer. */
public class ConverterCli {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final ConverterOptions runtimeOptions;

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    try {
      ConverterOptions options = new ConverterOptions(args);
      new ConverterCli(options);
    } catch (InvalidOptionsException | XsdParseException | MessageParseException | IOException e) {
      ConverterOptions.showCommandUsage();
      e.printStackTrace();
    }
  }

  private ConverterCli(ConverterOptions options) throws IOException, XsdParseException, MessageParseException, InvalidOptionsException {
    runtimeOptions = options;
    switch (runtimeOptions.inputType) {
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
        throw new InvalidOptionsException("Invalid inputType specified: " + runtimeOptions.inputType);
    }
  }

  private void parseXml() throws MessageParseException, IOException {
    logger.atInfo().log("Started message parse on: " + runtimeOptions.inputFile.getName());
    FileReader fileIn = new FileReader(runtimeOptions.inputFile);
    Message.Builder messageBuilder = MessageBuilderResolver.getBuilder(runtimeOptions.inputFile);
    Message protoMessage = MessageParser.parse(fileIn, messageBuilder);

    logger.atInfo().log(protoMessage.toString());
  }

  private void parseXsd() throws XsdParseException, IOException {
    logger.atInfo().log("Started schema parse on: " + runtimeOptions.inputFile.getName());
    FileReader fileIn = new FileReader(runtimeOptions.inputFile);
    ProtoSchema protoSchema = XsdParser.parse(fileIn);

    writeSchema(protoSchema);
  }

  private void parseXsdSet() throws XsdParseException, IOException {
    XsdSetMerger schemaSetMerger = new XsdSetMerger();

    for (File schemaFile : runtimeOptions.inputFileList) {
      logger.atInfo().log("Started schema set parse on folder: " + schemaFile.getName());
      FileReader fileIn = new FileReader(schemaFile);
      ProtoSchema schema = XsdParser.parse(fileIn);
      schemaSetMerger.addSchema(schema);
    }

    ProtoSchema finalSchema = schemaSetMerger.merge();
    writeSchema(finalSchema);
  }

  private void writeSchema(ProtoSchema schema) throws IOException {
    String rootNamespace = schema.getRootNamespace();
    String packageName = schema.getPackageName();

    Set<String> namespaces = schema.getSchemaStringMap().keySet();
    for (String namespace : namespaces) {
      File file = new File("./proto/" + rootNamespace + "/" + packageName + "/" + namespace + ".proto");
      file.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(file, false)) {
        writer.write(schema.getSchemaStringMap().get(namespace));
      }
    }
  }
}
