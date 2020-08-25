# C++ DDEX Converter CLI
### Overview

The command line tools single function is to parse/convert ERN and MEAD messages.
Current stable on Unix systems. Windows WIP.

To use:
```
usage: ./converter_cli [OPTIONS] input_file(s)
Convert input DDEX XML formats to Protobuf
 -d,--directory                specify this option if the input argument
                               is a directory of input_files
    --inputType=<type>         message | schema | schema_set
 -o,--outputDirectory=<path>   the output directory of the serialized
                               protobuf message(s)
```


### Parsing a DDEX XML message
The conversion tool can be used to convert an XML message to a Protobuf message.
In order to parse a message, it needs to be a supported version. Currently, ERN 3, ERN 4 and MEAD 1 are supported.

```
java -jar ddex-convertercli --inputType=message INPUT_FILE
```

The output will be located at ``/message`` by default, but the output directory can be set manually using the 
``-o / --outputDirectory`` option. 

If a message is not a supported version, the converter can be used to parse a schema and add a new 
supported version
