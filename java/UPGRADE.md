## Updating the Java CLI (Adding support for a new DDEX standard version)

Current supported versions of DDEX Standards 
<br/>
- **ERN <= 4.2** <br/>
- **MEAD <= 1.01** <br/>

In order to add support for a version of the ERN or MEAD 
that is not supported by default (as the standards are updated),
the end user will have to make manual edits to the library. 

There are two possible variations of "schema additions" that could occur.
1. Adding support for an entire major version of ERN or MEAD, such as adding support for MEAD 2.x
2. Adding support for a new minor version of ERN or MEAD, such as adding support for ERN 4.3

### 1. Collecting required XSD's
In order to generate the proper "super" Protocol Buffer schema, the schema conversion tool requires
all the schemas of the minor versions of the major version to be provided as input. 
<br/>
For example, to generate the schema
for ERN major version 4, the Java conversion tool requires the XSDs for both
- ERN 4.11
- ERN 4.2

The user must collect all these XSDs (including the avs.xsd files). <br/>

Many XSDs can be found on the DDEX website. <br/>
(Old standards) https://kb.ddex.net/display/HBK/Old+Versions+of+DDEX+Standards <br />
(More) https://kb.ddex.net/display/HBK/List+of+Standards+available+from+the+DDEX+Knowledge+Base <br />
(MEAD Proposal) https://kb.ddex.net/pages/viewpage.action?pageId=13470928 <br />

If the above links do not provide access to a desired schema, 
a simple Google search should be able to yield results for hidden documents on the DDEX wiki.  

### 2. Generating a Protocol Buffer Schema
To generate the Protocol Buffer schema, the Java CLI must be used. 
The C++ CLI does not support XSD conversion. 

Follow instructions on how to parse a `schema_set` [here](../java/ddex-convertercli/README.md). <br/>
If only a single XSD pair requires parsing, using the `schema` mode will suffice. 

At this point, the Protocol Buffer schema (.proto) files for the `schema_set` should be generated and located at either
the specified output directory, or at the default `./proto` folder (relative to the CLI .jar)
<br/>
For example, <br/>
```
\parent_folder
    ddex-convertercli-1.0-jar-with-dependencies.jar 
    \proto
        \ern
            \ern42
                ern.proto
                avs.proto
```

### 3. Adding the Java Classes to the XML to Proto Library

#### Option 1. Compile the Java Classes with Maven
This is the simpler option of the two. Place the `.proto` files in the `proto` directory under the appropriate folder.
The appropriate directory structure is `proto/namespace/class_packagename`
More specifically, given two Protocol Buffer schema files `avs.proto` and `ern.proto` from ERN version 4.2

```
\proto
    \ern
        \ern42
            ern.proto
            avs.proto
```

Protocol Buffer schema files located in the `proto` directory will be compiled by the 
`protoc-jar-maven-plugin`. If there are issues with this method, use option 2. 


#### Option 2. Compiling the Java Classes with `protoc`

Generate the Java classes using Google's `protoc` tool, which can be downloaded [here](https://developers.google.com/protocol-buffers/docs/downloads) <br/>
Refer to the [documentation](https://developers.google.com/protocol-buffers/docs/reference/java-generated) for more information on how. 

The result should be a set of C++ `*.java` files. 

Place the `*.java` files in `ddex-xmltoproto/src/main/java/generated/identifying_folder_name/namespace_name` <br/> 
More specifically, given two classes `Avs.java` and `Ern.java` 
- `Avs.java` should be placed in `ddex-xmltoproto/src/main/java/generated/ern42/avs`
- `Ern.java` should be placed in `ddex-xmltoproto/src/main/java/generated/ern42/ern`

### 4. Editing the MessageBuilderResolver Class
The generated classes are now accessible to source code. Edit the `MessageBuilderResolver.getBuilder(Document document)`
method (at the time of writing located at `ddex-xmltoproto/src/main/java/com/google/ddex/xmltoproto/MessageBuilderResolver.java:44`). 

In the if else statement, add a new case for the new major version number. <br/>
```
if (namespace.equals("ern") || namespace.equals("ernm")) {
    if (majorVersionNumber == 3) {
        return dynamicBuilderLoader("ern383.ern.Ern$NewReleaseMessage", "newBuilder");
    } 

    // INSERT NEW CODE
    else if (majorVersionNumber == 4) {
        return dynamicBuilderLoader("ern42.ern.Ern$NewReleaseMessage", "newBuilder");
    }
    // END INSERT NEW CODE

} else if (namespace.equals("mead")) {
    if (majorVersionNumber == 1) {
        return dynamicBuilderLoader("mead101.mead.Mead$MeadMessage", "newBuilder");
    }
}
```

The exact namespace of the class to add will depend on the version of ERN or MEAD being added. <br/>

In addition, if the root Message type changes, then the name of the class being accessed should correspond to that change.
(If in ERN 5, the root Message type is "NewMessage", then the Message to return would end be `ern5.ern.Ern$NewMessage`)

### 6. Rebuild the Project

Run `mvn clean install` in the java root directory
