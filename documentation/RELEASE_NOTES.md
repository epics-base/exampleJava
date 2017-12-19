Release Notes for exampleJava
=============================

## EPICS 7 Release 4.7

* exampleClient: GetForever, PutForever, MonitorForever no longer exist.
  These are replaced by Get, Put, and Monitor
* Get, Put, PutGet, and Monitor are new. They all work even if channel is not connected when
  they are started.
* exampleLink: This now has examples for a get, put, and monitor link.
  Each works even if linked channel does not exist when ExampleLinkMain is started.

## EPICS V4 Release 4.6

### Version 4.2

* All example code from the pvDatabaseJava and pvaClientJava modules of EPICS has 
  been moved to exampleJava.
  exampleJava now acts as a collecting module for example applications.
* Each example is a separate Maven module, in a subdirectory of exampleJava.
  Each POM inherits from the POM of exampleJava, which in turn inherits from the 
  EPICS parent POM (located in epicsCoreJava).
  The exampleJava POM functions as the aggregator for all examples, 
  i.e. all examples are Maven submodules.
* The documentation structure has been simplified by moving the static HTML 
  docs of all example modules to the parent module exampleJava. 
  Generated Javadoc are still kept with their respective modules.

## EPICS V4 Release 4.5

### Version 4.0

The exampleJava module contains two example data services that show the use of 
EPICS V4 to implement fast middleware data services that take arguments and 
return structured data.

### Version 4.0.4

* Modified for compatibility with new pvAccess/pvData APIs.

### Prior to Version 4.0.4

There are no release notes prior to version 4.0.4 of exampleJava.
