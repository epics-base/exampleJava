# helloService

helloService is the simplest possible illustration of the client and server sides of an EPICS V4 "service" (based on pvAccess RPC).

Specific explanations and instructions can be found in the documentation of the parent 
module `exampleJava`.

The service is the classical "hello world" implemented as a client executable, which a user runs giving their name, and a server which receives the user's name as an argument, and says hello.

## Building

In the hellowService directory

    mvn install
    

## To start helloService

on Linux:

    ./scripts/helloService

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (or where your Java is)
    .\scripts\<specificName>.bat


## To start the helloClient

on Linux:
 
    ./scripts/helloClient

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (or where your Java is)
    .\scripts\<specificName>.bat
