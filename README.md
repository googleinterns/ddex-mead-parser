# DDEX XML/XSD to Proto
**(Digital Data Exchange - Media Enrichment and Description) to Protocol Buffer Converting Tool**



## Build
To build the tool.

Run `mvn clean install` in the root directory

Maven will build all the sub packages. If you only need to rebuild a single sub-package: 
navigate to the subdirectory and call `mvn clean install` in the desired sub-package's root directory


## Javadoc
To generate the Javadoc for the tool.

Run `mvn javadoc:aggregate` in the root directory

# Codestyle Check
To run a checkstyle parse on the source code.

Run `mvn checkstyle:checkstyle-aggregate` in the root directory
