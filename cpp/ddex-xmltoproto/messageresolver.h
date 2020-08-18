#ifndef DDEX_MEAD_PARSER_MESSAGERESOLVER_H
#define DDEX_MEAD_PARSER_MESSAGERESOLVER_H

#include <google/protobuf/message.h>
#include "lib/pugixml/pugixml.hpp"
#include "generated/ern42/ern.pb.h"

namespace google {
namespace ddex {
    class MessageResolver {
        MessageResolver();
        ~MessageResolver();
        public:
            static google::protobuf::Message* getMessage(std::istream&);
            static google::protobuf::Message* getMessage(pugi::xml_document&);
        protected: 
            static pugi::xml_document getDocument(std::istream&);
            static pugi::xml_node getRootNode(pugi::xml_document&);
            static int getMajorVersionNumber(pugi::xml_node&);
            static int getVersionNumber(pugi::xml_node&);
            static std::string getSchemaLocationString(pugi::xml_node&);
            static std::string getNamespace(pugi::xml_node&);
    };
}}


#endif //DDEX_MEAD_PARSER_MESSAGERESOLVER_H
