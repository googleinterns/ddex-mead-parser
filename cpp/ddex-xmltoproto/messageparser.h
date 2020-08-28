#ifndef DDEX_MEAD_PARSER_MESSAGEPARSER_H
#define DDEX_MEAD_PARSER_MESSAGEPARSER_H

#include <google/protobuf/message.h>
#include <pugixml.hpp>

namespace google {
namespace ddex {
    class MessageParser {
        MessageParser();
        ~MessageParser();
    public:
        static void parse(std::istream&, google::protobuf::Message*);
    protected:
        static pugi::xml_node getRootNode(pugi::xml_document&);
        static std::string camelToSnake(const std::string&);
        static void handleSingle(pugi::xml_node&, google::protobuf::Message*, const google::protobuf::FieldDescriptor*);
        static void handleRepeated(pugi::xml_node&, google::protobuf::Message*, const google::protobuf::FieldDescriptor*);
        static void mergeMessage(pugi::xml_node&, google::protobuf::Message*);
        static void nestNodeAttributes(pugi::xml_node&);
        static void nestParserGeneratedXmlTags(pugi::xml_node&, const google::protobuf::Descriptor*);
    };
}}


#endif //DDEX_MEAD_PARSER_MESSAGEPARSER_H
