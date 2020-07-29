package com.google.ddex.convertercli;

import com.google.ddex.xmltoproto.MessageBuilderResolver;
import com.google.ddex.xmltoproto.MessageParserFactory;
import com.google.ddex.xsdtoproto.ProtoSchema;
import com.google.ddex.xsdtoproto.XsdParseException;
import com.google.ddex.xsdtoproto.XsdParser;
import com.google.ddex.xsdtoproto.XsdParserFactory;
import com.google.ddex.xsdtoproto.XsdSetMerger;
import com.google.ddex.xmltoproto.MessageParseException;
import com.google.ddex.xmltoproto.MessageParser;
import com.google.protobuf.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.Set;

import com.google.common.flogger.FluentLogger;

/** Command line tool for converting DDEX XSD and XML to Protocol Buffer. */
public class ConverterCli {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static void main(String[] args) {
    try {
      ConverterOptions options = new ConverterOptions(args);
      ConverterCli.parse(options);
    } catch (InvalidOptionsException | XsdParseException | MessageParseException | IOException e) {
      e.printStackTrace();
      ConverterOptions.showCommandUsage();
    }
  }

  private static void parse(ConverterOptions options) throws IOException, XsdParseException, MessageParseException, InvalidOptionsException {
    switch (options.getInputType()) {
      case "message":
        parseXml(options.getInputFile());
        break;
      case "schema":
        parseXsd(options.getInputFile());
        break;
      case "schema_set":
        parseXsdSet(options.getInputFileList());
        break;
      default:
        throw new InvalidOptionsException("Invalid inputType specified: " + options.getInputType());
    }
  }

  private static void parseXml(File inputFile) throws MessageParseException, IOException {
    logger.atInfo().log("Started message parse on: " + inputFile.getName());
    FileReader fileIn = new FileReader(inputFile);
    Message.Builder messageBuilder = MessageBuilderResolver.getBuilder(inputFile);
    Message protoMessage = MessageParserFactory.newInstant().parse(fileIn, messageBuilder);

    writeMessage(protoMessage, inputFile);
  }

  private static void parseXsd(File inputFile) throws XsdParseException, IOException {
    logger.atInfo().log("Started schema parse on: " + inputFile.getName());
    FileReader fileIn = new FileReader(inputFile);
    ProtoSchema protoSchema = XsdParserFactory.newInstant().parse(fileIn);

    writeSchema(protoSchema);
  }

  private static void parseXsdSet(List<File> inputFileList) throws XsdParseException, IOException {
    XsdSetMerger schemaSetMerger = new XsdSetMerger();

    for (File schemaFile : inputFileList) {
      logger.atInfo().log("Started schema set parse on folder: " + schemaFile.getName());
      FileReader fileIn = new FileReader(schemaFile);
      ProtoSchema schema = XsdParserFactory.newInstant().parse(fileIn);
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
      File file = new File("./proto/" + rootNamespace + "/" + packageName + "/" + namespace + ".proto");
      file.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(file, false)) {
        writer.write(schema.getSchemaStringMap().get(namespace));
      }
      logger.atInfo().log(schema.getSchemaStringMap().get(namespace));
    }
  }

  private static void writeMessage(Message message, File inputFile) throws IOException {
    // Get filename without extension
    String name = inputFile.getName().replaceFirst("[.][^.]+$", "");;

    File file = new File("./message/message_" + name);
    file.getParentFile().mkdirs();
    try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
      message.writeTo(outputStream);
    }
    logger.atInfo().log(message.toString());
  }
}
