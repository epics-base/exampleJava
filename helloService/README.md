# helloService

helloService is the simplest possible illustration of the client and server sides of an EPICS V4 "service" (based on pvAccess RPC).

The service is the classical "hello world" implemented as a client executable, which a user runs giving their name, and a server which receives the user's name as an argument, and says hello.

## Building

In the hellowService directory

    mvn package


## To start helloService

    mrk> pwd
    /home/epicsv4/master/exampleJava/helloService/shell
    mrk> ./helloService

## To start the helloClient
 
    mrk> pwd
    /home/epicsv4/master/exampleJava/helloService/shell
    mrk> ./helloClient
