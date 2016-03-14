# exampleJava

This project provides a set of examples of client and server code for pvAccess.
Each example can be built separately by building in the example subdirectory.
The complete set of examples, except for ChannelArchiverService can also be built by building in exampleCPP itself.

Each example can be used as a template for building other applications:

* Choose an example that is similar to the desired application.
* Copy the complete example to where the desired application should reside.
* Edit the new application.

## Building via exampleJava

** TBD **


## Building An Individual Example

In the example directory

    mvn package


## Brief summary of examples.

## Brief summary of examples.

### helloRPC

A very simple example of an EPICS V4 RPC service: This implements a
"Hello World" example of a service that is accessed via Channel RPC.

This is a good starting point for writing an RPC service.


### database

A pvAccess server that has PVRecords


### exampleClient

pvaClientJava examples that access the PVRecords in **database**.

In order to run the examples, **database** must also be built and then the
IOC database must be started as follows:

    mrk> pwd
    /home/epicsv4/master/exampleJava/database/shell
    mrk> ./exampleDatabase

### helloPutGet

An example of a PVRecord that implements a "Hello World" service that can be
accessed via ChannelPutGet.



### exampleLink

This implements a pvAccess server that has a PVRecord doubleArray and a PVRecord exampleLink that monitors changes to doubleArray. PVRecord exampleLink uses pvAccess client code to monitor for changes in doubleArray. It can use either provider local or pva to connect to doubleArray.



### powerSupply

This is an example of creating a PVRecord that uses a somewhat complicated top level PVStructure.
It simulates a power supply.

The example also has an example pvaClient for accessing the PVRecord.


###  arrayPerformance

This is an example that shows performance for an array of doubles.

arrayPerformanceMain implement a PVRecord that is a double array.
It has a process method with code that causes the array to be updated at selectable rates and sizes.!

It also has pvaClient examples that can get, put, and monitor the double array record.

### serviceAPI

Greg please describe.

### rdbService

Greg please describe.


