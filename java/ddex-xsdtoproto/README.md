# DDEX Xsd to Proto Schema


***Important Note***: The XSD parser requires that the schemaLocation attribute declare the location of the file relative to the 
location of the .jar. This attribute will likely ***NEED TO BE CHANGED*** manually before using the converter. 
The file structure of the XSD schema definition is. 
```
- parent_folder
    - ern.xsd
    - avs.xsd
```

The standard import statement will declare 
```xml
<!-- File: ddex-convertercli/src/main/resources/ern36/ern.xsd  -->

<xs:import namespace="http://ddex.net/xml/avs/avs"
           schemaLocation="avs.xsd"/>
```
Change the schemaLocation attribute to point to the file in one of two way
- the path of `avs.xsd` relative to the `ddex-convertercli.jar`
- the absolute system path of `avs.xsd`

---


### Multi DDEX Schema Parsing
The XsdParser and XsdSetMerger classes can be used together to parse a set of minor versions for 
some DDEX standard to form a super schema. We can parse every minor version of ERN 3 using
the XsdParser in the same manner as Single DDEX Schema Parsing. 

**This should be the method used to generate a schema for a major version of a DDEX Standard when
there is more than one subversion available**

Then, use XsdSetMerger.addSchema() to add each minor version schema to the merger. 

```
XsdSetMerger schemaSetMerger = new XsdSetMerger();

for (File schemaFile : fileList) {
    FileReader fileIn = new FileReader(schemaFile);
    ProtoSchema schema = XsdParser.parse(fileIn);
    schemaSetMerger.addSchema(schema);
}

ProtoSchema superSchema = schemaSetMerger.merge();
// Write the schema to file
```

The resulting super schema fully defines all fields and types for every version of the supplied schema. 
The XsdSetMerger should only be used to merge ProtoSchema's of the same standard and major version.
(ERN 3.x or MEAD 1.x, but not both at the same time)


### Single DDEX Schema Parsing
To parse a single schema, ERN 4.2 for example, the XsdParser class can be used.
Given a directory containing the ERN 4.2 schema definition...

```
- containing_directory
    - release-notification.xsd
    - avs.xsd
```
Provide a Reader for root xsd (release-notification.xsd) to XsdParser.parse().

```
File schemaFile = // get File
FileReader fileIn = new FileReader(schemaFile);
ProtoSchema schema = XsdParser.parse(fileIn);
```
**Note: If there are imports in the root xsd (which there likely are), the paths of those imports must be changed to be relative to the application, and not the root xsd)**

**If the converter still fails due to a missing import, edit the (ern.xsd/mead.xsd) files' import statements to an absolute path to the associated avs.xsd** <br>

The XsdParser will return a ProtoSchema that can be used to write a .proto schema to file.

```
ProtoSchema schema = XsdParser.parse(fileIn);
Set<String> namespaces = schema.getSchemaStringMap().keySet();
for (String namespace : namespaces) {
    String dotProtoString = schema.getSchemaStringMap().get(namespace)
    // Write the schema to file
}

```
