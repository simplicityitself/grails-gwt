# Grails GWT Plugin

## Introduction

The Google Web Toolkit (GWT) is an advanced AJAX framework that allows you to develop rich user interfaces in Java, thus taking advantage of type-checking and code re-use. GWT will then compile your Java code and generate fast, cross-platform Javascript that can be included in any web page you choose.
The plugin makes it easy to incorporate GWT code into your GSP pages, and it also simplifies the handling of RPC requests on the server. If you have not used GWT before, please read the documentation on the GWT website.


This is the source of the plugin.

The plugin host page is at http://grails.org/plugin/gwt

Documentation can be found at http://simplicityitself.github.com/grails-gwt/guide/

The Google Group can be found at https://groups.google.com/group/grails-gwt


## Installation

Add the plugin, plus the extended-dependency-manager plugin for accessing the current dependency manager.

```
plugins {
  build ":extended-dependency-manager:0.5.5"
  compile ":gwt:1.0", {
    transitive=false
  }
}
```

