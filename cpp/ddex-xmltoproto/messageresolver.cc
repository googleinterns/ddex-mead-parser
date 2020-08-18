#include <google/protobuf/message.h>

#include "messageresolver.h"
#include "generated/ern42/ern.pb.h"
#include "generated/mead101/mead.pb.h"

namespace google {
namespace ddex {

    MessageResolver::MessageResolver() {}
    MessageResolver::~MessageResolver() {}

    google::protobuf::Message *MessageResolver::getMessage(std::istream &xml_istream) {
        pugi::xml_document doc;
        pugi::xml_parse_result result = doc.load(xml_istream);
        std::cout << result.description() << std::endl;
        return getMessage(doc);
    }

    google::protobuf::Message *MessageResolver::getMessage(pugi::xml_document &doc) {
        pugi::xml_node root = getRootNode(doc);
        int major_version_number = getMajorVersionNumber(root);
        int version_number = getVersionNumber(root);
        std::string ns = getNamespace(root);

        std::cout << "MVN: " << major_version_number << " VN: " << version_number << " NS: " << ns << std::endl;

        if (ns == "ern" || ns == "ernm") {
            if (major_version_number == 3) {
                // Compile the class and add here
            } else if (major_version_number == 4)  {
                return new ern411::ern::NewReleaseMessage;
            }
        } else if (ns == "mead") {
            if (major_version_number == 1) {
                return new mead101::mead::MeadMessage;
            }
        }
        google::protobuf::Message *ret = new mead101::mead::MeadMessage;
        
        return ret;
    }

    int MessageResolver::getMajorVersionNumber(pugi::xml_node &node) {
        std::string schema_location = getSchemaLocationString(node);
        std::string version_number_string = schema_location.substr(schema_location.find_last_of("/") + 1);
        version_number_string = version_number_string.substr(0,1);

        return stoi(version_number_string);
    }

    int MessageResolver::getVersionNumber(pugi::xml_node &node) {
        std::string schema_location = getSchemaLocationString(node);
        std::string version_number_string = schema_location.substr(schema_location.find_last_of("/") + 1);

        return stoi(version_number_string);
    }

    std::string MessageResolver::getSchemaLocationString(pugi::xml_node &node) {
        if (node.attribute("xmlns:ern") != NULL) {
            return ((std::string) node.attribute("xmlns:ern").value());
        } else if (node.attribute("xmlns:ernm") != NULL) {
            return ((std::string) node.attribute("xmlns:ernm").value());
        } else if (node.attribute("xmlns:mead") != NULL) {
            return ((std::string) node.attribute("xmlns:mead").value());
        }
    }

    std::string MessageResolver::getNamespace(pugi::xml_node &node) {
        std::string node_name = node.name();
        std::string ns = "";
        if (node_name.find(":") != std::string::npos) {
            ns = node_name.substr(0, node_name.find(":"));
        }
        return ns;
    }

    pugi::xml_node MessageResolver::getRootNode(pugi::xml_document &doc) {
        return doc.first_child();
    }

    pugi::xml_document MessageResolver::getDocument(std::istream &xml_istream) {
        pugi::xml_document doc;
        pugi::xml_parse_result result = doc.load(xml_istream);
        return doc;
    }

}}
