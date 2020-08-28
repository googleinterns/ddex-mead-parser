load("@rules_cc//cc:defs.bzl", "cc_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "avs_proto",
    srcs = ["proto/mead101/avs.proto"],
)

proto_library(
    name = "mead_proto",
    srcs = ["proto/mead101/mead.proto"],
    deps = [":avs_proto"],
)

cc_proto_library(
    name = "mead_cc_proto",
    deps = [":mead_proto"],
)

proto_library(
    name = "ern_avs_proto",
    srcs = ["proto/ern42/avs.proto"],
)

proto_library(
    name = "ern_proto",
    srcs = ["proto/ern42/ern.proto"],
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
        ],
    hdrs = [
        "messageparser.h",
        "messageresolver.h",
    ],
    includes = [
        "proto/"
    ],
    deps =  [
        ":ern_cc_proto",
        ":mead_cc_proto",
    ],

    visibility = ["//visibility:public"],
)