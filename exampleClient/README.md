# exampleClient

Provides a set of examples that use pvaClientJava

In order to run the examples, **database** must also be built and then the IOC database must be started as follows:

    mrk> pwd
    /home/epicsv4/master/exampleJava/database/shell
    mrk> ./exampleDatabase

[developerGuide.html ](http://epics-pvdata.sourceforge.net/informative/developerGuide/developerGuide.html)provides tututorial information on how to use pvaClient


## Building

In the example directory

    mvn package
    cd shell
    cp sourceEXAMPLE source
    # edit file source so that EPICSV4 is correctly defined

## ExamplePvaClientGet

This has a number of examples.
Each example connects to a server via providers **pva** and **ca**.

### ExampleDouble

This shows both a short and long way to get data from a scalar channel.
The short way throws an execption if the request fails.
The long way allows the client more control of looking for problems and blocking.


### ExampleDoubleArray

Like exampleDouble except the data is a scalarArray.


## ExamplePvaClientMonitor

This is an example of creating a monitor on a channel.
It monitors a scalar double field.
It also issues puts to the same channel so that it can make the monitors occur.


## ExamplePvaClientPut

This has the folllowing examples.

### examplePut

This shows use of get, put, and monitor.

### ExamplePvaClientPut

This does a put and then a get.

## ExamplePvaClientProcess

This example makes a process request to a channel

## ExamplePvaClientMultiDouble

This is an example of using pvaClientMultiChannel,
pvaClientMultiGetDouble, pvaClientMultiPutDouble, and pvaClientMultiMonitorDouble.


## ExamplePvaClientNTMulti

This is an example of using pvaClientMultiChannel to get data as an NTMultiChannel

## HelloWorldPutGet

This is an example of issuing a channelPutGet.

## HelloWorldRPC

This is an example of issuing a channelRPC request.
It does **not** use pva.

