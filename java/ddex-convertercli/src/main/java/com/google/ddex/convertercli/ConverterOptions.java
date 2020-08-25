package com.google.ddex.convertercli;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

class ConverterOptions {
  private File outputDirectory = null;
  private File inputFile = null;
  private List<File> inputFileList = new ArrayList<>();
  private boolean inputIsDirectory = false;
  private String inputType = null;
  private CommandLine commandLineInput;

  private static final ImmutableSet<String> VALID_INPUT_TYPES =
      ImmutableSet.of("message", "schema", "schema_set");
  private static final Options DEFAULT_COMMAND_OPTIONS =
      new Options()
          .addOption(
              Option.builder("o")
                  .longOpt("outputDirectory")
                  .hasArg()
                  .argName("path")
                  .required(false)
                  .desc("the output directory of the serialized protobuf message(s)")
                  .build())
          .addOption(
              Option.builder("d")
                  .longOpt("directory")
                  .desc("specify this option if the input argument is a directory of input_files")
                  .build())
          .addOption(
              Option.builder()
                  .longOpt("inputType")
                  .hasArg()
                  .argName("type")
                  .required(true)
                  .desc("message | schema | schema_set")
                  .build());

  public static void showCommandUsage() {
    final String helpCmdSyntax = "ConverterCli [OPTIONS] input_file";
    final String helpHeader = "Convert input DDEX XML formats to Protobuf";

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(helpCmdSyntax, helpHeader, DEFAULT_COMMAND_OPTIONS, null);
  }

  public ConverterOptions(String[] args) throws InvalidOptionsException {
    setCommandLineInput(args);
    populateOptions();
    validateOptions();
  }

  File getOutputDirectory() {
    return outputDirectory;
  }

  File getInputFile() {
    return inputFile;
  }

  List<File> getInputFileList() {
    return inputFileList;
  }

  boolean isInputIsDirectory() {
    return inputIsDirectory;
  }

  String getInputType() {
    return inputType;
  }

  private void setCommandLineInput(String[] args) throws InvalidOptionsException {
    try {
      CommandLineParser commandParser = new DefaultParser();
      commandLineInput = commandParser.parse(DEFAULT_COMMAND_OPTIONS, args);
    } catch (ParseException e) {
      throw new InvalidOptionsException("Error trying to parse command input.", e);
    }
  }

  private File getFile(String pathName) throws InvalidOptionsException {
    File ddexFile = new File(pathName);
    if (ddexFile.exists()) {
      return ddexFile;
    } else {
      throw new InvalidOptionsException("XML file input does not exist.");
    }
  }

  private void populateOptions() throws InvalidOptionsException {
    if (commandLineInput.hasOption("outputDirectory")) {
      outputDirectory = new File(commandLineInput.getOptionValue("outputDirectory"));
    }
    if (commandLineInput.hasOption("directory")) {
      inputIsDirectory = true;
    }
    if (commandLineInput.hasOption("inputType")) {
      inputType = commandLineInput.getOptionValue("inputType");
    }

    String[] args = commandLineInput.getArgs();
    if (args.length == 1) {
      File ddexFile = getFile(args[0]);
      inputFile = ddexFile;
      inputFileList.add(ddexFile);
    } else if (args.length > 1) {
      for (String arg : args) {
        File ddexFile = getFile(arg);
        inputFileList.add(ddexFile);
      }
    } else {
      throw new InvalidOptionsException("Missing arguments.");
    }
  }

  private void validateOptions() throws InvalidOptionsException {
    if (!VALID_INPUT_TYPES.contains(inputType)) {
      throw new InvalidOptionsException("Invalid inputType specified: " + inputType);
    }
  }
}
