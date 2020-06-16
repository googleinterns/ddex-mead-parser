package com.google.ddexmeadparser;
import com.google.protobuf.Message;

import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class DdexMeadParser {
    private final DdexMeadParserOptions runtimeOptions;

    public static void main(String[] args) {
        try {
            DdexMeadParser ddexMeadParser = new DdexMeadParser(args);
            ddexMeadParser.parseMead();
        } catch (InvalidOptionsException | MeadConversionException e) {
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

    public void parseMead() throws MeadConversionException {
        System.out.println("Started running parse on file: " + runtimeOptions.inputMeadMessage.getName());
        Document document = getDocument(runtimeOptions.inputMeadMessage);

        MeadConverter meadConverter = new MeadConverter();
        Message message = meadConverter.convert(document);

        System.out.println(message.toString());
    }

    private Document getDocument(File file) throws MeadConversionException {
        try {
            if (!file.exists() || file.isDirectory()) {
                throw new MeadConversionException("XML file input does not exist or is a directory.");
            }
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MeadConversionException("Exception occurred when getting document: " + e.getMessage(), e);
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

        String[] args = commandLineInput.getArgs();
        if (args.length == 1) {
            File meadXml = new File(args[0]);
            if (meadXml.exists()) {
                runtimeOptions.inputMeadMessage = meadXml;
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
