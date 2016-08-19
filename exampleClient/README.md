# exampleClient

Provides a set of examples that use pvaClientJava.

## Building

In the exampleJava/exampleClient directory

    mvn install


## Running

The [developerGuide.html](http://epics-pvdata.sourceforge.net/informative/developerGuide/developerGuide.html)
provides tutorial information on how to use pvaClient.

All examples have wrapper scripts for Windows and Linux in the `scripts` directory.
In the exampleJava/exampleClient directory, run

on Linux:

    ./scripts/example<specificName>

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\example<specificName>.bat


### Prerequisites

In order to run the examples, **database** must also be built and the example database must be started.
In the exampleJava/database directory, run

on Linux:

    ./scripts/exampleDatabase

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\exampleDatabase.bat


### examplePvaClientGet

This has a number of examples.
Each example connects to a server via both providers **pva** (pvAccess)
and **ca** (Channel Access).


#### ExampleDouble

This shows both a short and long way to get data from a scalar channel.
The short way throws an execption if the request fails.
The long way allows the client more control of looking for problems and blocking.


#### ExampleDoubleArray

Like exampleDouble except the data is a scalarArray.


### examplePvaClientMonitor

This is an example of creating a monitor on a channel.
It monitors a scalar double field.
It also issues puts to the same channel so that it can make the monitors occur.


### examplePvaClientPut

This has the following examples.

#### ExamplePut

This shows use of get, put, and monitor.


### examplePvaClientPut

This does a put and then a get.


### examplePvaClientProcess

This example makes a process request to a channel


### examplePvaClientMultiDouble

This is an example of using pvaClientMultiChannel,
pvaClientMultiGetDouble, pvaClientMultiPutDouble, and pvaClientMultiMonitorDouble.


### examplePvaClientNTMulti

This is an example of using pvaClientMultiChannel to get data as an NTMultiChannel


### helloWorldPutGet

This is an example of issuing a channelPutGet.


### helloWorldRPC

This is an example of issuing a channelRPC request.
It does **not** use pva.


### getForever


### monitorForever
