exampleJava   [![Build Status](https://travis-ci.org/epics-base/exampleJava.svg?branch=master)](https://travis-ci.org/epics-base/exampleJava)

This project provides a set of examples of client and server code for pvAccess.
Each example can be built separately by building in the example subdirectory.
The complete set of examples can also be built by building in exampleJava itself.

Each example can be used as a template for building other applications:

* Choose an example that is similar to the desired application.
* Copy the complete example to where the desired application should reside.
* Edit pom.xml. This is biggest problem.
* Edit the new application.


## Building via exampleJava

In directory exampleJava

    mvn package


## Building An Individual Example

In the example directory

    mvn package


## Examples

To see a brief description of the examples see documentation/exampleJava.html.
