# DDEX Converter Command Line Interface 
### Overview
The command line tool has three modes of operation
1. Parse a DDEX XML message
2. Parse a DDEX XSD schema
3. Parse a set of DDEX XSD schema (of the same major version of a standard)

To use:
```
usage: java -jar ddex-convertercli [OPTIONS] input_file(s)
Convert input DDEX XML formats to ProtoBuf
 -d,--directory                specify this option if the input argument
                               is a directory of input_files
    --inputType <type>         message | schema | schema_set
 -o,--outputDirectory <path>   the output directory of the serialized
                               protobuf message(s)
```

