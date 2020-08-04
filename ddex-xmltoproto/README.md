# DDEX Xml to Proto Message

### DDEX Message Parsing
To parse a DDEX message (ERN/MEAD), the MessageParser class can be used. 

In order for an XML message to be parsed, the version must be supported by the included schemas, or 
be manually added to the library using the schema parsing capabilities of the library. 

Provide a Reader for XML message (message.xml) to MessageParser.parse().

```
File messageFile = // get File
FileReader fileIn = new FileReader(messageFile);
Message protoMessage = MessageParser.parse(fileIn);
```

The MessageParser will return a Protocol Buffer Message which can be used directly or written to file.
