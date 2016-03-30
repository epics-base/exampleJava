# Known Problems

## helloRPC, rdbService, and serviceAPI

These are left almost unchanged since release 4.5.

**Greg:** Please look at these examples and decide what should be done.


## exampleClient Known Problems

### BaseV3Channel.java

**Good News** Matej fixed this problem

A null pointer exception is thrown when a second attempt is made to connect to the same channel.


### examplePvaClientMultiDouble and examplePvaClientNTMulti

**Good News** These now works.

The problem was:

    Sometimes this is successful and sometimes just shows

        _____examplePvaClientMultiDouble starting_______
        _example provider pva channels [Ljava.lang.String;@42a57993_

The problem was a deadly embrace in pvaClientJava.

I also spent time looking at pvaClientCPP.
It also had some possible deadly embrace problems.
I have fixed both but am not sure if I did it the best way possible.
I want to discuss this with Matej.


### examplePvaClientMultiDouble

The C++ implementation allows a mixture of any numeric scaler.
The Java version currently only supports double.

### examplePvaClientMonitor

The C++ implementation shows all fields have changed value.
The Java implementation only shows fields that have actually changed.

## helloWorldRPC

1) The CPP version allows a client to issueConnect and waitConnect.
The Java version does not support this.

2) The Java implemantation does not terminate.ex


### examplePvaClientNTMulti

When the Java implementation uses provider **ca** numeric arrays are filled out with 0s.

## exampleLink Known Problems

### ExampleLinkMain

Does not terminate.

## helloRPC Known Problems

### HelloService

Does not terminate on either Java or C++.



## powerSupply Known Problems

### powerSupplyMain

When client sets voltage to 0 the record throws an exception.
In the C++ version this just passes the exception to the client.
In the Java version it seems that the server is no longer working




