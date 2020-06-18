package com.google.ddexmeadparser;
import com.google.protobuf.Message;

import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class DdexMeadParser {
    private final DdexMeadParserOptions runtimeOptions;

    public static void main(String[] args) {
        try {
            DdexMeadParser ddexMeadParser = new DdexMeadParser(args);
            ddexMeadParser.exec();
        } catch (InvalidOptionsException | MeadConversionException | SchemaConversionException e) {
            e.printStackTrace();
        }
    }

    // For use by command line users
    public DdexMeadParser(String[] args) throws InvalidOptionsException {
        Options commandOptions = buildCommandOptions();
        try {
            CommandLineParser commandParser = new DefaultParser();
            CommandLine commandLineInput = commandParser.parse(commandOptions, args);
            runtimeOptions = buildRuntimeOptions(commandLineInput);
        } catch (ParseException e) {
            showCommandUsage(commandOptions);
            throw new InvalidOptionsException("Parse exception occurred: " + e.getMessage(), e);
        } catch (InvalidOptionsException e) {
            showCommandUsage(commandOptions);
            throw e;
        }
    }

    public void exec() throws MeadConversionException, SchemaConversionException, InvalidOptionsException {
        if (runtimeOptions.inputType == DdexMeadParserOptions.inputTypeValue.MESSAGE) {
            parseMead();
        } else if (runtimeOptions.inputType == DdexMeadParserOptions.inputTypeValue.SCHEMA) {
            parseSchema();
        }
    }

    public void parseMead() throws MeadConversionException, InvalidOptionsException {
        System.out.println("Started mead message parse on file: " + runtimeOptions.inputFile.getName());
        Document document = getDocument(runtimeOptions.inputFile);

        MeadConverter meadConverter = new MeadConverter();
        Message message = meadConverter.convert(document);

        // Write output proto message files
        System.out.println(message.toString());
    }

    public void parseSchema() throws SchemaConversionException, InvalidOptionsException {
        System.out.println("Started schema parse on file: " + runtimeOptions.inputFile.getName());
        StreamSource xsdFile = getStreamSource(runtimeOptions.inputFile);

        SchemaConverter schemaConverter = new SchemaConverter();
        SchemaEntryMap schemaEntryList = schemaConverter.convert(xsdFile);

        ProtoWriter protoWriter = new ProtoWriter();
        try {
            protoWriter.serialize(schemaEntryList);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private Document getDocument(File file) throws InvalidOptionsException {
        try {
            if (!file.exists() || file.isDirectory()) {
                throw new FileNotFoundException("XML file input does not exist or is a directory.");
            }
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new InvalidOptionsException("Exception occurred when getting document: " + e.getMessage(), e);
        }
    }

    private StreamSource getStreamSource(File file) throws InvalidOptionsException {
        try {
            if (!file.exists() || file.isDirectory()) {
                throw new FileNotFoundException("XSD input does not exist or is a directory.");
            }
            return new StreamSource(new FileInputStream(runtimeOptions.inputFile));
        } catch (IOException e) {
            throw new InvalidOptionsException("Exception occurred when getting document: " + e.getMessage(), e);
        }
    }

    private static Options buildCommandOptions() {
        Options options = new Options();
        options.addOption(Option.builder("o")
                .longOpt("outputDirectory")
                .hasArg()
                .argName("path")
                .required(false)
                .desc("the output directory of the serialized protobuf message(s)")
                .build()
        );
        options.addOption(Option.builder("d")
                .longOpt("directory")
                .desc("specify this option if the input argument is a directory of input_files")
                .build()
        );
        options.addOption(Option.builder()
                .longOpt("inputType")
                .hasArg()
                .argName("type")
                .required(true)
                .desc("message | schema")
                .build()
        );
        return options;
    }

    private static DdexMeadParserOptions buildRuntimeOptions(CommandLine commandLineInput) throws InvalidOptionsException {
        DdexMeadParserOptions runtimeOptions = new DdexMeadParserOptions();
        if (commandLineInput.hasOption("outputDirectory")) {
            runtimeOptions.outputDirectory = new File(commandLineInput.getOptionValue("outputDirectory"));
        } else {
            runtimeOptions.outputDirectory = new File("output");
        }
        if (commandLineInput.hasOption("directory")) {
            runtimeOptions.inputIsDirectory = true;
        }
        if (commandLineInput.hasOption("inputType") && commandLineInput.getOptionValue("inputType").equals("message")) {
            runtimeOptions.inputType = DdexMeadParserOptions.inputTypeValue.MESSAGE;
        } else if (commandLineInput.hasOption("inputType") && commandLineInput.getOptionValue("inputType").equals("schema")) {
            runtimeOptions.inputType = DdexMeadParserOptions.inputTypeValue.SCHEMA;
        } else {
            throw new InvalidOptionsException("Invalid or missing inputType.");
        }

        String[] args = commandLineInput.getArgs();
        if (args.length == 1) {
            File meadXml = new File(args[0]);
            if (meadXml.exists()) {
                runtimeOptions.inputFile = meadXml;
            } else {
                throw new InvalidOptionsException("XML file input does not exist.");
            }
        } else {
            throw new InvalidOptionsException("Expected 1 argument.");
        }

        return runtimeOptions;
    }

    private static void showCommandUsage(Options commandOptions) {
        final String HELP_CMD_SYNTAX = "DdexMeadParser [OPTIONS] input_file";
        final String HELP_HEADER = "Convert input MEAD XML to protobuf Message";

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(HELP_CMD_SYNTAX, HELP_HEADER, commandOptions, null);
    }
}
