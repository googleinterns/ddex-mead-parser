# DDEX Xml to Proto Message

### DDEX Message Parsing
*In order for an XML message to be parsed, the version must be supported by the included schemas, or 
be manually added to the library using the schema parsing capabilities of the library. 
Instructions on how to do so can be found [here](../UPGRADE.md)*

To convert a DDEX message (ERN/MEAD) to a Protocol Buffer message, use the `google::ddex::MessageParser` class. 

The parsing method is `google::ddex::MessageParser::parse(std::istream&, google::protobuf::Message*)` and requires
a `std::istream` and a `google::protobuf::Message*` as parameters.

`std::istream` - must be an istream of the target XML message to parse (message.xml). <br>
`google::protobuf::Message*` - must be an instance of the C++ class representation of the DDEX standard version.
In most cases, retrieve the `google::protobuf::Message*` by using the `google::ddex::MessageResolver` class. More information
in [Getting an instance of a Message](getting-an-instance-of-a-message).

```cpp

google::protobuf::Message* message = google::ddex::MessageResolver::getMessage(xml_istream_copy1);
google::ddex::MessageParser::parse(xml_istream_copy2, message);

```
*Note: There are two `istream`'s of the same xml message since the pugi library calls .close() on the stream after a single use* <br>
*Note: `google::ddex::MessageParser::parse` has no return value since the method mutates `message` directly.*

#### Getting an instance of a Message
To get an instance of a Message (`google::protobuf::Message*`) use `google::ddex::MessageResolver::getMessage`. <br>

The `google::ddex::MessageResolver::getMessage` methods accept an XML message as input, as either a `std::istream` or
`pugi::xml_document` directly. The input XML message must be a supported version of a DDEX standard. 

**If the version is not supported, follow instructions on updating the library [here](../UPGRADE.md)**