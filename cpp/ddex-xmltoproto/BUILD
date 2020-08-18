load("@rules_cc//cc:defs.bzl", "cc_binary", "cc_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "avs_proto",
    srcs = ["generated/mead101/avs.proto"],
)

proto_library(
    name = "mead_proto",
    srcs = ["generated/mead101/mead.proto"],
    deps = [":avs_proto"],
)

cc_proto_library(
    name = "mead_cc_proto",
    deps = [":mead_proto"],
)

proto_library(
    name = "ern_avs_proto",
    srcs = ["generated/ern42/avs.proto"],
)

proto_library(
    name = "ern_proto",
    srcs = ["generated/ern42/ern.proto"],
    deps = [":ern_avs_proto"],
)

cc_proto_library(
    name = "ern_cc_proto",
    deps = [":ern_proto"],
)

cc_library(
    name = "ddex_xmltoproto",
    srcs = [
        "messageparser.cc",
        "messageresolver.cc",
        "lib/pugixml/pugixml.cpp", 
        "lib/pugixml/pugixml.hpp", 
        "lib/pugixml/pugiconfig.hpp",
        "generated/ern42/ern.pb.cc",
        "generated/ern42/avs.pb.cc",
        "generated/mead101/mead.pb.cc",
        "generated/mead101/avs.pb.cc",
        ],
    hdrs = [
        "messageparser.h",
        "messageresolver.h",
        "generated/ern42/ern.pb.h",
        "generated/ern42/avs.pb.h",
        "generated/mead101/mead.pb.h",
        "generated/mead101/avs.pb.h",
    ],

    visibility = ["//visibility:public"],
)