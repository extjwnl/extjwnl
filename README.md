# About

extJWNL (Extended Java WordNet Library) is a Java API for creating, reading and updating dictionaries in WordNet format. extJWNL supports:
* writing dictionaries
* encodings (including UTF-8)
* Java generics
* huge dictionaries
* instance dictionaries
* Maven support
* documentation improvements
* improved unit test coverage
* enhanced database support
* simplified configuration
* multiple other improvements and fixes

# Getting started

In the pom.xml:

```xml
<dependency>
    <groupId>net.sf.extjwnl</groupId>
    <artifactId>extjwnl</artifactId>
    <version>1.7</version>
</dependency>
<dependency>
    <groupId>net.sf.extjwnl</groupId>
    <artifactId>extjwnl-data-wn31</artifactId>
    <version>1.0</version>
</dependency>
```

In the code:

```java
Dictionary d = Dictionary.getDefaultResourceInstance();
```

extJWNL contains Examples.java with examples of API use.