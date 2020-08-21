## Updating the C++ CLI (Adding support for a new DDEX standard version)

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

### 3. Compiling the C++ Classes with `protoc`
Generate the C++ classes using Google's `protoc` tool, which can be downloaded [here](https://developers.google.com/protocol-buffers/docs/downloads) <br/>
Refer to the [documentation](https://developers.google.com/protocol-buffers/docs/reference/cpp-generated) for more information on how. 

The result should be a set of C++ `*.pb.cc` and `*.pb.h` files. 

### 4. Adding the C++ Classes to the XML to Proto Library

- Place the `*.pb.cc` and `*.pb.h` files in `ddex-xmltoproto/generated/identifying_folder_name` <br/> 
(e.g. `ddex-xmltoproto/generated/ern42`).
- Add an `#include` statement in `ddex-xmltoproto/messageresolver.cc` that references the newly generated C++ header of the root Message type
<br/> (e.g. `#include "generated/ern42/ern.pb.h"`)

### 5. Editing the MessageResolver Class
The generated classes are now accessible to source code. Edit the `MessageResolver::getMessage(pugi::xml_document &doc)`
method (at the time of writing located at `ddex-xmltoproto/messageresolver.cc:20`). 

In the if else statement, add a new case for the new major version number. <br/>
```
if (ns == "ern" || ns == "ernm") {
    if (major_version_number == 3) {
        return new ern3::ern::NewReleaseMessage;
    } 

    // INSERT NEW CODE
    else if (major_version_number == 4)  {
        return new ern42::ern::NewReleaseMessage;
    }
    // END INSERT NEW CODE

} else if (ns == "mead") {
    if (major_version_number == 1) {
        return new mead101::mead::MeadMessage;
    }
}
```

The exact namespace of the class to add will depend on the version of ERN or MEAD being added. <br/>

In addition, if the root Message type changes, then the name of the class being accessed should correspond to that change.
(If in ERN 5, the root Message type is "NewMessage", then the Message to return would end be `ern5::ern::NewMessage`)

### 6. Rebuild the Project

Run `bazel build //ddex-converterli:converter_cli` at the cpp project root (the location of the `WORKSPACE` file)

