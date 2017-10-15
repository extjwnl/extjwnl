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
<dependency>
    <groupId>net.sf.extjwnl</groupId>
    <artifactId>extjwnl</artifactId>
    <version>1.9.3</version>
</dependency>
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