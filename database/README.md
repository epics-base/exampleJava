# database

A pvAccess server that has PVRecords


## Building

In the example directory

    mvn package



## To start the example

    mrk> pwd
    /home/epicsv4/master/exampleJava/database/shell
    mrk> ./exampleDatabase

## database/src/org/epics/exampleDatabase

This directory has the following files:

### ExampleHello.java
   
Code for an example that is accessed via channelPutGet.

### ExampleHelloRPC.java
   
Code for an example that is accesed via channelRPC.

### ExampleDatabase.java
  
Code that creates many PVRecords.    
Most are soft records but also exampleHello and exampleHelloRPC.



