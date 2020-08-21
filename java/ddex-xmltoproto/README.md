# DDEX Xml to Proto Message

### DDEX Message Parsing
*In order for an XML message to be parsed, the version must be supported by the included schemas, or 
be manually added to the library using the schema parsing capabilities of the library. 
Instructions on how to do so can be found [here](../UPGRADE.md)*

To parse a DDEX message (ERN/MEAD), use the MessageParser class. 

Provide a Reader for XML message (message.xml) to MessageParser.parse().

```
File messageFile = // get File
FileReader fileIn = new FileReader(messageFile);
Message protoMessage = MessageParser.parse(fileIn);
```

The MessageParser will return a `com.google.protobuf.Message` which can be used directly or written to file.
The actual Message is an instance of the representative class (e.g. `ern42.ern.Ern.NewReleaseMessage`) which inherits from
`com.google.protobuf.Message`.