# database

A pvAccess server that has a database of PVRecords.


## Building

In the exampleJava/database directory

    mvn install


## Running

In the exampleJava/database directory, run

on Linux:

    ./scripts/exampleDatabase

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\exampleDatabase.bat

## database/src/org/epics/exampleJava/exampleDatabase

This directory has the following files:

* ExampleHelloRecord.java 
Code for an example that is accessed via channelPutGet.

* ExampleHelloRPC.java
Code for an example that is accessed via channelRPC.

* ExampleDatabase.java
Code that creates many PVRecords.    
Most are soft records, but also exampleHello and exampleHelloRPC.
