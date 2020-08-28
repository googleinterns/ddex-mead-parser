#include <iostream>
#include <google/protobuf/message.h>
#include <google/protobuf/descriptor.h>
#include <pugixml.hpp>

#include "messageparser.h"


namespace google {
namespace ddex {

    MessageParser::MessageParser() {};
    MessageParser::~MessageParser() {};

    void MessageParser::parse(std::istream& xml_istream, google::protobuf::Message* message) {
        pugi::xml_document doc;
        pugi::xml_parse_result result = doc.load(xml_istream);

        pugi::xml_node root = MessageParser::getRootNode(doc);
        MessageParser::mergeMessage(root, message);

        std::cout << "Result description : " << result.description() << std::endl;
        std::cout << "Final message: " << std::endl << message->DebugString() << std::endl;
    }

    pugi::xml_node MessageParser::getRootNode(pugi::xml_document& doc) {
        return doc.first_child();
    }

    void MessageParser::mergeMessage(pugi::xml_node& node, google::protobuf::Message* message) {
        const google::protobuf::Descriptor* desc = message->GetDescriptor();

        nestParserGeneratedXmlTags(node, desc);
        nestNodeAttributes(node);

        for (pugi::xml_node child : node.children()) {
            std::string name = camelToSnake(child.name());
            const google::protobuf::FieldDescriptor* fieldDescriptor = desc->FindFieldByName(name);
            if (fieldDescriptor != nullptr) {
                if (fieldDescriptor->is_repeated()) {
                    handleRepeated(child, message, fieldDescriptor);
                } else {
                    handleSingle(child, message, fieldDescriptor);
                }
            }
        }
    }

    void MessageParser::handleRepeated(
            pugi::xml_node& node,
            google::protobuf::Message* message,
            const google::protobuf::FieldDescriptor* fieldDescriptor) {
        pugi::xml_text node_value = node.text();

        const google::protobuf::Reflection* ref = message->GetReflection();
        google::protobuf::FieldDescriptor::CppType cpp_type = fieldDescriptor->cpp_type();
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_INT32) {
            ref->AddInt32(message, fieldDescriptor, ((google::protobuf::int32)node_value.as_int()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_INT64) {
            ref->AddInt64(message, fieldDescriptor, ((google::protobuf::int64)node_value.as_llong()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_UINT32) {
            ref->AddUInt32(message, fieldDescriptor, ((google::protobuf::uint32)node_value.as_uint()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_UINT64) {
            ref->AddUInt64(message, fieldDescriptor, ((google::protobuf::uint64)node_value.as_ullong()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_DOUBLE) {
            ref->AddDouble(message, fieldDescriptor, node_value.as_double());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_FLOAT) {
            ref->AddFloat(message, fieldDescriptor, node_value.as_float());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_BOOL) {
            ref->AddBool(message, fieldDescriptor, node_value.as_bool());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_STRING) {
            ref->AddString(message, fieldDescriptor, node_value.as_string());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_MESSAGE) {
            MessageParser::mergeMessage(node, ref->AddMessage(message, fieldDescriptor));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_ENUM) {
            // ERROR
        }
    }

    void MessageParser::handleSingle(
            pugi::xml_node& node,
            google::protobuf::Message* message,
            const google::protobuf::FieldDescriptor* fieldDescriptor)
    {
        pugi::xml_text node_value = node.text();
        const google::protobuf::Reflection* ref = message->GetReflection();
        google::protobuf::FieldDescriptor::CppType cpp_type = fieldDescriptor->cpp_type();

        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_INT32) {
            ref->SetInt32(message, fieldDescriptor, ((google::protobuf::int32)node_value.as_int()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_INT64) {
            ref->SetInt64(message, fieldDescriptor, ((google::protobuf::int64)node_value.as_llong()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_UINT32) {
            ref->SetUInt32(message, fieldDescriptor, ((google::protobuf::uint32)node_value.as_uint()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_UINT64) {
            ref->SetUInt64(message, fieldDescriptor, ((google::protobuf::uint64)node_value.as_ullong()));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_DOUBLE) {
            ref->SetDouble(message, fieldDescriptor, node_value.as_double());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_FLOAT) {
            ref->SetFloat(message, fieldDescriptor, node_value.as_float());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_BOOL) {
            ref->SetBool(message, fieldDescriptor, node_value.as_bool());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_STRING) {
            ref->SetString(message, fieldDescriptor, node_value.as_string());
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_MESSAGE) {
            MessageParser::mergeMessage(node, ref->MutableMessage(message, fieldDescriptor));
        }
        if (cpp_type == google::protobuf::FieldDescriptor::CppType::CPPTYPE_ENUM) {
            // ERROR
        }
    }

    void MessageParser::nestNodeAttributes(pugi::xml_node& node) {
        while (node.first_attribute() != NULL) {
            // MUST RENAME THE NODES TO BE IN GUAVA FORMAT CAMEL AND ALSO IGNORE CONTAINS ":"
            pugi::xml_attribute attr = node.first_attribute();
            std::string attribute_name(attr.name());

            if (attribute_name.find(":") == std::string::npos) {
                pugi::xml_node new_node = node.append_child(attr.name());
                new_node.text().set(attr.value());
            }
            node.remove_attribute(attr);
        }
    }


    void MessageParser::nestParserGeneratedXmlTags(pugi::xml_node& node, const google::protobuf::Descriptor* desc) {
        if (desc == NULL) {
            return;
        }

        std::string parserGeneratedTag = "";
        if (desc->FindFieldByName("ext_value") != NULL) {
            parserGeneratedTag = "ext_value";
        } else if (desc->FindFieldByName("enum_value") != NULL) {
            parserGeneratedTag = "enum_value";
        } else if (desc->FindFieldByName("auto_value") != NULL) {
            parserGeneratedTag = "auto_value";
        }

        if (parserGeneratedTag != "") {
            std::string curVal = node.text().as_string();
            node.set_value("");
            pugi::xml_node new_node = node.append_child(parserGeneratedTag.c_str());
            new_node.text().set(curVal.c_str());
        }
    }

    std::string MessageParser::camelToSnake(const std::string& in) {
        std::string snake_string = "";
        for (std::string::size_type i = 0; i < in.size(); i++) {
            char c = in[i];
            if (isupper(c)) {
                if (i != 0) snake_string += "_";
                snake_string += tolower(c);
            } else {
                snake_string += c;
            }
        }
        return snake_string;
}
}}