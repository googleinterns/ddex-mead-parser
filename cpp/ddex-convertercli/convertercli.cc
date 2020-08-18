#include <iostream>
#include <fstream>
#include <google/protobuf/message.h>
#include <cxxopts.hpp>

#include "ddex-xmltoproto/messageparser.h"
#include "ddex-xmltoproto/messageresolver.h"
#include "ddex-xmltoproto/generated/ern42/ern.pb.h"


void writeMessage(google::protobuf::Message* message) {

}

void parseXml(std::string input_file_path) {
    std::ifstream xml_istream_one(input_file_path);
    std::ifstream xml_istream_two(input_file_path);

    google::protobuf::Message* message = google::ddex::MessageResolver::getMessage(xml_istream_one);
    google::ddex::MessageParser::parse(xml_istream_two, message);

    writeMessage(message);
}

int main(int argc, char** argv) {
    std::string input_file_path = argv[argc - 1];

    cxxopts::Options options("DDEX XML parser");
    options.add_options()
            ("o,outputDirectory", "the output directory of the serialized protobuf message(s)", cxxopts::value<std::string>())
            ("d,directory", "specify this option if the input argument is a directory of input_files", cxxopts::value<bool>()->default_value("false"))
            ("h,help", "Print help");

    auto result = options.parse(argc, argv);
//    "/usr/local/google/home/seanliew/Downloads/ERN 4.1.1 Samples/3 MixedMedia.xml"
    if (result.count("outputDirectory")) {
        std::cout << "specified output directory: " << result["outputDirectory"].as<std::string>() << std::endl;
    }
    if (result.count("directory")) {
        std::cout << "specified boolean directory: " << result["directory"].as<bool>() << std::endl;
    }


    // sanitize the filename input, or better yet supply an istream
    parseXml(input_file_path);

    do {
        std::cout << '\n' << "Press a key to continue...";
    } while (std::cin.get() != '\n');
    return 1;
}





