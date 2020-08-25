## DDEX XSD and Generated Protocol Buffer Schema Comparison

The definitions of the ERN and MEAD standards are relatively simple XSD files.<br>
ERN versions <= 3.5 split the definitions into numerous (around 6) XSD files. <br>
In ERN versions > 3.5 and existing versions of MEAD, there is a main XSD file, and a single imported 
AVS (allowable values set) XSD that defines enum values. 

This document will focus on the current **(main.xsd + avs.xsd)** definition format. This is not a 
standard method of defining the standards, and there are no guarantees that the format will stay the same 
in the future. The purpose of this review is to clarify why some fields exist and how the XSD to Protocol Buffer Schema
converter handles differences in the XSD and Protocol Buffer Schema languages. 

### Namespaces

The XSD files defines the namespaces in use as attributes of the top level `xs:schema` tag.
```xml
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:ern="http://ddex.net/xml/ern/36"
           xmlns:avs="http://ddex.net/xml/avs/avs"
           targetNamespace="http://ddex.net/xml/ern/36"
           elementFormDefault="unqualified"
           attributeFormDefault="unqualified">
           ...
```
The `targetNamespace` attribute self assigns all the other type definitions in the file to be members of the `targetNamespace`
namespace. Child definitions of the above schema node would be in the `ern` namespace, and we see that
`avs` and `xs` will also be used. 

The generated .proto schema defines namespaces as `packages`. In order to convert the namespacing of the XSD
to .proto, the converter follows the format 

```
ROOT_NAMESPACE = The namespace of the root XSD. Currently either "ern", "ernm", or "mead".
VERSION_NUMBER = The digit only format of the full version number. For example
                 the version number for ERN 4.1.1 would be 411, and ERN 3.6 would be 36.
NAMESPACE      = The namespace of the current XSD file. 

ROOT_NAMESPACE+VERSION_NUMBER.NAMESPACE
```

The .proto file for the above XSD would declare the package attribute as such

```protobuf
package ern36.ern;
```

### Imports

Imports defined by the XSD files simply import external XSD definitions of tags to be used in the current file. 

```xml
<!-- File: ddex-convertercli/src/main/resources/ern36/ern.xsd  -->

<xs:import namespace="http://ddex.net/xml/avs/avs"
           schemaLocation="ddex-convertercli/src/main/resources/ern36/avs.xsd"/>
```
---
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

This statement imports the definitions in the `avs.xsd` file and allows the definitions in
`ern.xsd` to declare themselves as instances of enums defined in the `avs.xsd`

```xml
<xs:element name="TerritoryCode" maxOccurs="unbounded" type="avs:TerritoryCode">
    ...
```

The .proto import uses the filename of the imported file. The converter names the file after the namespace which it contains, in the 
case of importing `avs` into `ern`, the .proto import statement is:

```protobuf
// File: ern.proto

import "avs.proto";
```

To reference types defined by an imported .proto, the Type is prefixed by the package name of the 
imported .proto. 

```protobuf
// File: ern.proto

message AdditionalTitle {
	optional ern36.avs.CurrentTerritoryCode applicable_territory_code = 1;
}
```

`avs.proto` defines CurrentTerritoryCode and `ern.proto` is able to reference this type. 

### Messages / Types

The main XSD defines all its types at the top-level. Each type is declared as an 
`xs:element`, `xs:complexType` or `xs:simpleType` 
The simplified structure of the XSD is, 

```xml
<xs:schema>
    <xs:element name="TypeOne">
        ...
    </xs:element>
    <xs:complexType name="TypeTwo">
        ...
    </xs:complexType>
    <xs:simpleType name="TypeThree">
        ...
    </xs:simpleType>
</xs:schema>
```
This structure makes generated Message types in the .proto schema relatively simple. Each top-level 
tag in the XSD is converted to a message definition in the output .proto. 

```protobuf
message TypeOne {
  //...
}
message TypeTwo {
  //...
}
message TypeThree {
  //...
}
```

### Fields

Converting the declared child tags of each top-level `xs:element`/`xs:complexType`/`xs:simpleType` to Protocol Buffer schema fields
is more nuanced.
<br>

**The originally UpperCase names are converted to snake_case** <br>
A tag with the `name="AdditionalTitleType"` becomes `additional_title_type` in the .proto.
This is a style convention of .proto. 
   
**A tag with the attribute `"maxOccurs=unbounded"` is labelled `repeated`. 
All other tags are labelled `optional`** <br>

**Enums (defined in avs.xsd) are replaced by a single string field**<br>
To avoid file size bloat, sets of enums defined by the `avs.xsd` are replaced by a single string field
called `enum_value`.
```xml
<xs:simpleType name="AdditionalTitleType">
      <xs:restriction base="xs:string">
         <xs:enumeration value="AlternativeTitle"></xs:enumeration>
         <xs:enumeration value="FormalTitle"></xs:enumeration>
         <xs:enumeration value="OriginalTitle"></xs:enumeration>
         ...
```
The .proto definition for the above top-level XSD definition would be
```proto
message AdditionalTitleType {
    optional string enum_value = 1;
}
```

### XSD Extensions and Attributes

XSD allows for definitions to extend other definitions. This is used by the DDEX standard to add attributes to existing
type definitions. The DDEX XSD files declare attributes to extend these base types. 

```xml
<xs:complexType name="FormValue">
    <xs:annotation>
        <xs:documentation source="ddex:Definition">A Composite containing details of a form of a Work.</xs:documentation>
    </xs:annotation>
    <xs:simpleContent>
        <xs:extension base="avs:Form">
            <xs:attribute name="Namespace" type="xs:string">
                <xs:annotation>
                    <xs:documentation source="ddex:Definition">The Namespace of the vocal register. This is represented in an XML schema as an XML Attribute.</xs:documentation>
                </xs:annotation>
            </xs:attribute>
            <xs:attribute name="UserDefinedValue" type="xs:string">
                <xs:annotation>
                    <xs:documentation source="ddex:Definition">A UserDefined value of the vocal register. This is represented in an XML schema as an XML Attribute.</xs:documentation>
                </xs:annotation>
            </xs:attribute>
        </xs:extension>
    </xs:simpleContent>
</xs:complexType>
```

In the generated .proto, the parsed output of this type definition is.

```protobuf
message FormValue {
	optional string namespace = 1;
	optional string user_defined_value = 2;
	optional mead101.avs.Form ext_value = 3;
}
```

The converter "wraps" the extended definition as an `ext_value` field with the base type. 
XSD attributes defined in the extension are converted to fields. 
This means that a tag defined by the above XSD,  
 
```xml
<FormValue Namespace="NS_VALUE" UserDefinedValue="UDV_VALUE"> 
    VALUE 
</FormValue>
```

...is transformed structurally to

```xml
<FormValue>
    <Namespace> NS_VALUE </Namespace>
    <UserDefinedValue> UDV_VALUE </UserDefinedValue>
    <ExtValue> VALUE </Ext_value>   
</FormValue>
```

in order to preserve all information in the .proto schema. 


### Example

The project includes .proto schemas for older versions of ERN and MEAD. 
They can all be found at `java/proto`.

For example: MEAD 1.0
- [mead.proto](java/proto/mead/mead101/mead.proto)
- [avs.proto](java/proto/mead/mead101/avs.proto)