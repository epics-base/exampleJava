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
    # edit source so that EPICSV4 is correctly defined

## examplePvaClientGet

This has a number of examples.

### exampleDouble

This shows both a short and long way to get data from a scalar channel.
The short way throws an execption if the request fails.
The long way allows the client more control of looking for problems and blocking.


### exampleDoubleArray

Like exampleDouble except the data is a scalarArray.

### exampleCADouble

This is like exampleDouble except it uses provider <b>ca</b>.

### exampleCADoubleArray


This is like exampleDoubleArray except it uses provider <b>ca</b>.



## examplePvaClientMonitor

This is an example of creating a monitor on a channel.
It monitors a scalar double field.
It also issues puts to the same channel so that it can make the monitors occur.


## examplePvaClientPut

This has the folllowing examples.

### examplePut

This shows use of get, put, and monitor.

### examplePVFieldPut

This does a put and then a get.

## examplePvaClientProcess

This example makes a process request to a channel

## examplePvaClientMultiDouble

This is an example of using pvaClientMultiChannel,
pvaClientMultiGetDouble, pvaClientMultiPutDouble, and pvaClientMultiMonitorDouble.


## examplePvaClientNTMulti

This is an example of using pvaClientMultiChannel to get data as an NTMultiChannel

## helloWorldPutGet

This is an example of issuing a channelPutGet.

## helloWorldRPC

This is an example of issuing a channelRPC request.
It does **not** use pva.
S

