# exampleJava

This project provides a set of examples of client and server code for pvAccess.
Each example can be built separately by building in the example subdirectory.
The complete set of examples, except for ChannelArchiverService can also be built by building in exampleCPP itself.

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


## Brief summary of examples.



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

### helloRPC

A very simple example of an EPICS V4 RPC service: This implements a
"Hello World" example of a service that is accessed via Channel RPC.

This is a starting point for writing an RPC service without using **pvDatabaseJava**.

But note that example **database** has a similar example implemented via **pvDatabaseJava**, which allows clients to monitor the result of each request.


### exampleLink

This implements a pvAccess server that has a record doubleArray and a PVRecord exampleLink that monitors changes to doubleArray. PVRecord exampleLink uses pvAccess client code to monitor for changes in doubleArray.
It can use provider local, pva, or ca to connect to doubleArray.
The doubleArray can exist in the same IOC or, with provider pva or ca,
can exist in another IOC.



### powerSupply

This is an example of creating a PVRecord that uses a somewhat complicated top level PVStructure.
It simulates a power supply.

The example also has an example pvaClient for accessing the PVRecord.

### pvDatabaseRPC

An example of a pvDatabase PVRecord which also supports RPC services.

The record represents a 2-D position, but also provides an RPC service
which moves the position through a sequence of points before returning.
A channel to the record supports the usual operations (such as get, put and
monitor) but also supports Channel RPC, i.e. the RPC service has the same 
channel name as the position PV.

A client ("move") for calling the service is supplied.



###  arrayPerformance

This is an example that shows performance for an array of longs.

arrayPerformanceMain implement a PVRecord that is a long array.
It has a process method with code that causes the array to be updated at selectable rates and sizes.!

It also has pvaClient examples that can get, put, and monitor the double array record.

### serviceAPI

Start with its RDBSERVICE_README.txt. 

The rdbService example is a complete functional server for accessing SQL databases 
such Oracle via an EPICS V4 server, and returning the resulting table ResultSet 
data back to an EPICS V4 client. rdbService illustrates a number of facets of EPICS V4's,
use for writing high performance synchronous servers, plus some other stuff:

1) channelRPC functionality of EPICSV4

2) use of JDBC for accessing Oracle to execute a SQL query within a pattern that retries 
  the query and can rebuild the connection if any query fails - for high reliability and 
  to make sure if the database back end ever cycles, the EPICS v4 server need not be restarted.

3) Passing string messages back to the client asynchronously - while the server is still
processing the query - in the event of the server detecting an 
error or other detecting some other diagnostic.

4) Putting complex data into an EPICS V4 pvStructure (the basic complex data object 
of pvData), ready for returning results back to the EPICS V4 client. 
PvStructures are the basic mechanism for returning "structured" data between 
EPICS V4 clients and servers.

5) Client side calling the server, and getting the results. 

6) Unpacking complex data out of a pvStructure

7) Illustration of the idea of using the NTTable EPICS V4 "normative type" (though the 
definition of an NTTable is likely to change soon)

8) Helpers for transformation of array pvData to Java Vectors

9) Formatting pvStructure encoded data for printing.

10) Use of the NTURI normative type, the EPICS V4 standard for passing requests for 
data and arguments.


The "serviceapi" package in **src/org/epics/serviceAPI** is composed of a single class server and a single class
client, and is intended to illustrate the principles of RPC comms in EPICS V4. It shows
how one defines and populates the pvStructure conforming to the NTURI Normative Type, for making 
an RPC query to a pvAccess server, and it shows how the server defines and populates a table of data 
conforming to the NTTable Normative Type for encoding the reply. 


### rdbService

Start with its RDBSERVICE_README.txt 

rdbService is the classic hello world idea, implemented as a server and a client.
The client sends an argument "your name" to the server, which replies just 
saying "hello <that name>". The server is built on top of the RPCSever framework 
in pvAccessJava. 


