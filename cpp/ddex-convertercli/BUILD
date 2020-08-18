cc_binary(
    name = "converter_cli",
    srcs = ["convertercli.cc"],
    includes =  ["*"],
    linkopts = [
        "-lprotobuf",
    ],
    deps = [
        "//ddex-xmltoproto:ddex_xmltoproto",
        "@com_google_protobuf//:protobuf",
    ],
    linkstatic=False,
)

