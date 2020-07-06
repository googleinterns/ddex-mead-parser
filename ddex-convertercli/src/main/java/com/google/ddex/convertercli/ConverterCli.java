package com.google.ddex.convertercli;

import com.google.ddex.xsdtoproto.ProtoSchema;
import com.google.ddex.xsdtoproto.SchemaParseException;
import com.google.ddex.xsdtoproto.SchemaParser;
import com.google.ddex.xsdtoproto.SchemaSetMerger;
import com.google.ddex.xmltoproto.MessageParseException;
import com.google.ddex.xmltoproto.MessageParser;
import com.google.protobuf.Message;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Set;

import com.google.common.flogger.FluentLogger;

public class ConverterCli {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final ConverterOptions runtimeOptions;

  public static void main(String[] args) {
    try {
      ConverterOptions options = new ConverterOptions(args);
      new ConverterCli(options);
    } catch (InvalidOptionsException | SchemaParseException | MessageParseException | IOException e) {
      ConverterOptions.showCommandUsage();
      e.printStackTrace();
    }
  }

  private ConverterCli(ConverterOptions options) throws IOException, SchemaParseException, MessageParseException {
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
        // TODO What to put here
    }
  }

  private void parseXml() throws MessageParseException, IOException {
    logger.atInfo().log("Started message parse on: " + runtimeOptions.inputFile.getName());
    Message.Builder messageBuilder = MessageBuilderResolver.getBuilder(runtimeOptions.inputFile);
    Message protoMessage = MessageParser.parse(runtimeOptions.inputFile, messageBuilder);

    // Write output proto message files
    logger.atInfo().log(protoMessage.toString());
  }

  private void parseXsd() throws SchemaParseException, IOException {
    logger.atInfo().log("Started schema parse on: " + runtimeOptions.inputFile.getName());
    FileReader fileIn = new FileReader(runtimeOptions.inputFile);
    ProtoSchema protoSchema = SchemaParser.parse(fileIn);

    // Write schema to file
    writeSchema(protoSchema);
  }

  private void parseXsdSet() throws SchemaParseException, IOException {
    SchemaSetMerger schemaSetMerger = new SchemaSetMerger();

    for (File schemaFile : runtimeOptions.inputFileList) {
      logger.atInfo().log("Started schema set parse on folder: " + schemaFile.getName());
      FileReader fileIn = new FileReader(schemaFile);
      ProtoSchema schema = SchemaParser.parse(fileIn);
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
      // TODO Use output directory?
      File file = new File("./src/main/proto/" + rootNamespace + "/" + packageName + "/" + namespace + ".proto");
      file.getParentFile().mkdirs();

      FileWriter writer = new FileWriter(file, false);
      writer.write(schema.getSchemaStringMap().get(namespace));
    }
  }
}
