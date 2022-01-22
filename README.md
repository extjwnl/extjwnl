[![Build Status](https://travis-ci.org/extjwnl/extjwnl.png?branch=master)](https://travis-ci.org/extjwnl/extjwnl)

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
<!-- main library dependency -->
<dependency>
    <groupId>net.sf.extjwnl</groupId>
    <artifactId>extjwnl</artifactId>
    <version>2.0.5</version>
</dependency>
<!-- Princeton WordNet 3.1 data dependency -->
<dependency>
    <groupId>net.sf.extjwnl</groupId>
    <artifactId>extjwnl-data-wn31</artifactId>
    <version>1.2</version>
</dependency>
```

In the code:

```java
Dictionary d = Dictionary.getDefaultResourceInstance();
```

extJWNL contains [Examples.java](utilities/src/main/java/net/sf/extjwnl/utilities/Examples.java) with examples of API use.

# WordNet Data Dependencies
extJWNL can load WordNet data from resources on the classpath.
One way to do that is to add a dependency like illustrated above. 
There are several dependencies available that contain data from Princeton WordNet:
 * [extjwnl-data-wn21](https://github.com/extjwnl/extjwnl-data-wn21) WordNet 2.1 Unix version (text)
 * [extjwnl-data-wn30](https://github.com/extjwnl/extjwnl-data-wn30) WordNet 3.0 Unix version (text)
 * [extjwnl-data-wn31](https://github.com/extjwnl/extjwnl-data-wn31) WordNet 3.1 Unix version (text)
 * [extjwnl-data-wn31-map](https://github.com/extjwnl/extjwnl-data-wn31-map) WordNet 3.1 Unix version (serialized)
 * [extjwnl-data-mcr30](https://github.com/extjwnl/extjwnl-data-mcr30) Multilingual Central Repository 3.0 Unix version (text): 2016 release; currently only the Spanish portion

WordNet dependencies with text data contain original Princeton WordNet files augmented with extJWNL configuration file.
Multilingual Central Repository (MCR) dependencies contain extJWNL-compatible versions of MCR files augmented with extJWNL configuration file, generated exceptional forms, and inter-language index files.
Dependencies with serialized data contain Princeton WordNet files serialized into HashMaps 
and augmented with extJWNL configuration file.

Serialized data is larger, but might work faster for some use cases. 

If you use ```Dictionary.getDefaultResourceInstance()```, then make sure you use only one dependency.  

# Acknowledgements

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

extJWNL is supported by YourKit Open Source License. YourKit, LLC is the creator of
[YourKit Java Profiler](https://www.yourkit.com/java/profiler/index.jsp)
and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/index.jsp),
innovative and intelligent tools for profiling Java and .NET applications.


# Resources

* [Homepage](http://extjwnl.sourceforge.net)
* [Download](http://sourceforge.net/projects/extjwnl/files)
* [Documentation Wiki](https://github.com/extjwnl/extjwnl/wiki)
* [Mailing Lists](http://lists.sourceforge.net/lists/listinfo/extjwnl-announce)
* [Forums](http://sourceforge.net/projects/extjwnl/forums/)
* [RSS News Feed](http://sourceforge.net/export/rss2_projnews.php?group_id=386458)
* [Issues](https://github.com/extjwnl/extjwnl/issues)
