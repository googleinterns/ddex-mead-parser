## DDEX XML/XSD to Protocol Buffer
**(Digital Data Exchange Standards) [ERN + MEAD] to Protocol Buffer Conversion**

### Overview
MEAD and ERN messages are transmitted in XML format. This tool allows users to convert
the XSD schemas and XML messages to Protocol Buffer format.

This README file contains instructions for general use of the conversion tools.

You can find more information about Protocol Buffer's here: 
[https://developers.google.com/protocol-buffers/](https://developers.google.com/protocol-buffers/)


### General Usage
The tool has two core functions: 

- Convert the XSD(s) representing a DDEX standard (e.g. ERN 3.8.1) to an equivalent Protocol Buffer schema
- Convert the XML(s) representing an ERN or MEAD message to an equivalent Protocol Buffer message

### Converting Standards' XSD Schema to Protocol Buffer
XSD's for ERN and MEAD can be found on the DDEX website.

(Old standards) https://kb.ddex.net/display/HBK/Old+Versions+of+DDEX+Standards <br />
(More) https://kb.ddex.net/display/HBK/List+of+Standards+available+from+the+DDEX+Knowledge+Base <br />
(MEAD Proposal) https://kb.ddex.net/pages/viewpage.action?pageId=13470928 <br />

Converting the DDEX XSD's is a prerequisite for converting messages, since each message conversion depends
on the corresponding Protocol Buffer schema (.proto) to exist. The Java tool handles conversion from XSD to 
Protocol Buffer schema. 

To use the Java **command line application** to parse XSD, more information can be found at: <br />
[java/ddex-convertercli/README.md](java/ddex-convertercli/README.md) <br />
To use the Java **library** to parse XSD, more information can be found at: <br />
[java/ddex-xsdtoproto/README.md](java/ddex-xsdtoproto/README.md) <br />

A successful conversion from XSD to Protocol Buffer will yield a folder containing `.proto` files representing the processed schema.
These `.proto` schema files can be compiled to either Java or C++ classes by the `protoc`compiler 
(refer [here](https://github.com/protocolbuffers/protobuf/blob/master/README.md) for instruction on how to install and use the `protoc` compiler). 

### Converting XML Messages to Protocol Buffer
Given an ERN or MEAD XML message, both the C++ and Java tools can be used to convert the message to 
Protocol Buffer. <br/>
Both versions include a command line wrapper for their respective library implementations.

To use the Java **command line application** to parse XML, more information can be found at: <br />
[java/ddex-convertercli/README.md](java/ddex-convertercli/README.md) <br />
To use the Java **library** to parse XML, more information can be found at: <br />
[java/ddex-xmltoproto/README.md](java/ddex-xmltoproto/README.md) <br />

To use the C++ **command line application** to parse XML, more information can be found at: <br />
[cpp/ddex-convertercli/README.md](cpp/ddex-convertercli/README.md) <br />
To use the C++ **library** to parse XML, more information can be found at: <br />
[cpp/ddex-xmltoproto/README.md](cpp/ddex-xmltoproto/README.md) <br />

**Note** - Conversion of ERN and MEAD messages are dependent on existing support for their schema versions (ERN <= 4.2, MEAD <= 1.01). 
To manually upgrade the XML to Protocol Buffer tools, instructions for both Java and C++ can be found at...
- Java: [java/UPGRADE.md](java/UPGRADE.md)
- C++: [cpp/UPGRADE.md](cpp/UPGRADE.md)

