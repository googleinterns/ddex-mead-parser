package com.google.ddex.convertercli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ConverterOptions {
  public File outputDirectory = null;
  public File inputFile = null;
  public List<File> inputFileList = new ArrayList<>();
  public boolean inputIsDirectory = false;
  public String inputType = null;

  private CommandLine commandLineInput;

  public ConverterOptions(String[] args) throws InvalidOptionsException {
    setCommandLineInput(args);
    populateOptions();
    validateOptions();
  }

  public static void showCommandUsage() {
    Options commandOptions = buildCommandOptions();
    final String HELP_CMD_SYNTAX = "ConverterCli [OPTIONS] input_file";
    final String HELP_HEADER = "Convert input DDEX XML formats to ProtoBuf";

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(HELP_CMD_SYNTAX, HELP_HEADER, commandOptions, null);
  }

  private void setCommandLineInput(String[] args) throws InvalidOptionsException {
    try {
      Options commandOptions = buildCommandOptions();
      CommandLineParser commandParser = new DefaultParser();
      commandLineInput = commandParser.parse(commandOptions, args);
    } catch (ParseException e) {
      throw new InvalidOptionsException("Error trying to parse command input.", e);
    }
  }

  private static Options buildCommandOptions() {
    Options options = new Options();
    options.addOption(
            Option.builder("o")
                    .longOpt("outputDirectory")
                    .hasArg()
                    .argName("path")
                    .required(false)
                    .desc("the output directory of the serialized protobuf message(s)")
                    .build());
    options.addOption(
            Option.builder("d")
                    .longOpt("directory")
                    .desc("specify this option if the input argument is a directory of input_files")
                    .build());
    options.addOption(
            Option.builder()
                    .longOpt("inputType")
                    .hasArg()
                    .argName("type")
                    .required(true)
                    .desc("message | schema | schema_set")
                    .build());
    return options;
  }

  private void populateOptions() throws InvalidOptionsException {
    if (commandLineInput.hasOption("outputDirectory")) {
      outputDirectory = new File(commandLineInput.getOptionValue("outputDirectory"));
    } else {
      outputDirectory = new File("output");
    }
    if (commandLineInput.hasOption("directory")) {
      inputIsDirectory = true;
    }
    if (commandLineInput.hasOption("inputType")) {
      inputType = commandLineInput.getOptionValue("inputType");
    } else {
      throw new InvalidOptionsException("Invalid or missing inputType.");
    }

    String[] args = commandLineInput.getArgs();
    if (args.length == 1) {
      File meadXml = new File(args[0]);
      if (meadXml.exists()) {
        inputFile = meadXml;
      } else {
        throw new InvalidOptionsException(meadXml.getAbsolutePath() + " XML file input does not exist.");
      }
    } else if (args.length > 1) {
      for (String arg : args) {
        File meadXsd = new File(arg);
        if (meadXsd.exists()) {
          inputFileList.add(meadXsd);
        } else {
          throw new InvalidOptionsException("XML file input does not exist.");
        }
      }
    }
  }

  private void validateOptions() throws InvalidOptionsException {
    if (inputFile == null && inputFileList.size() == 0) {
      throw new InvalidOptionsException("No input files or folder specified.");
    }

    if (!inputType.equals("message") && !inputType.equals("schema") && !inputType.equals("schema_set")) {
      throw new InvalidOptionsException("Invalid inputType specified: " + inputType);
    }
  }
}
