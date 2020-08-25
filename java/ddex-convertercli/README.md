# Java DDEX Converter CLI
### Overview
In order to use the jar, the Java Runtime Environment must be installed on the system. <br>

The command line tool has three modes of operation
1. Parse a DDEX XML message
2. Parse a DDEX XSD schema
3. Parse a set of DDEX XSD schema (of the same major version of a standard)

*Maven outputs two .jar files. As a standalone application, use the `-with-dependencies` .jar.*

To use:
```
usage: java -jar ddex-convertercli [OPTIONS] input_file(s)
Convert input DDEX XML formats to ProtoBuf
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


### Parsing DDEX XSD schema(s)
The conversion tool can be used to convert an XSD schema to a Protobuf schema.

```
java -jar ddex-convertercli --inputType=schema INPUT_FILE
java -jar ddex-convertercli --inputType=schema_set INPUT_FILE1 INPUTFILE2 ...
```

Each input file should be an XSD defining the ERN or MEAD standard. These XSD files contain imports for associated 
avs.xsd files. 

---
***Important Note***: The XSD parser requires that the schemaLocation attribute declare the location of the file relative to the 
location of the .jar. This attribute will likely ***NEED TO BE CHANGED*** manually before using the converter. 
The file structure of the XSD schema definition is. 
```
- parent_folder
    - ern.xsd
    - avs.xsd
```

The standard import statement will declare 
```xml
<!-- File: ddex-convertercli/src/main/resources/ern36/ern.xsd  -->

<xs:import namespace="http://ddex.net/xml/avs/avs"
           schemaLocation="avs.xsd"/>
```
Change the schemaLocation attribute to point to the file in one of two way
- the path of `avs.xsd` relative to the `ddex-convertercli.jar`
- the absolute system path of `avs.xsd`

---
