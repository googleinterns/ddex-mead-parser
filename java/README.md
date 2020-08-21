# Java DDEX XML/XSD to Protocol Buffer
**(Digital Data Exchange Standards) [ERN + MEAD] to Protocol Buffer Conversion Package**

### Build
To build the tool.

Run `mvn clean install` in the root directory

Maven will build all the sub packages. If you only need to rebuild a single sub-package: 
navigate to the subdirectory and call `mvn clean install` in the desired sub-package's root directory

### Javadoc
To generate the Javadoc for the tool.

Run `mvn javadoc:aggregate` in the root directory

### Codestyle Check
To run a checkstyle parse on the source code.

Run `mvn checkstyle:checkstyle-aggregate` in the root directory

### More Information
Using the Java DDEX Converter CLI: [ddex-convertercli/README.md](ddex-convertercli/README.md)<br>
Using the Java Xml to Proto Message Package: [ddex-xmltoproto/README.md](ddex-xmltoproto/README.md)<br>
Using the Java Xsd to Proto Schema Package: [ddex-xsdtoproto/README.md](ddex-xsdtoproto/README.md)
