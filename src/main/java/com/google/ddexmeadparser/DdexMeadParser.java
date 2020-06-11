package com.google.ddexmeadparser;

import com.google.protobuf.ByteString;
import org.apache.commons.cli.*;
import java.io.File;

public class DdexMeadParser {
    private final DdexMeadParserOptions runtimeOptions;

    public static void main(String[] args) {
        try {
            DdexMeadParser ddexMeadParser = new DdexMeadParser(args);
            ddexMeadParser.parseMead();
        } catch (InvalidOptionsException | MeadParseException e) {
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

    public void parseMead() throws MeadParseException {
        System.out.println("Started running the MEAD XML parser!");
        System.out.println("Running parse on file: " + runtimeOptions.inputMeadMessage.getName());

        // Get the MEAD message back from our inner class
        MeadConverter meadConverter = new MeadConverter();
        ByteString a = meadConverter.convert(runtimeOptions.inputMeadMessage);
        // Write the MEAD message to file
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
               // throw new InvalidOptionsException("XML file input does not exist.");
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
