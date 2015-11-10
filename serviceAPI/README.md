# serviceAPI

This example contains a simple illustration of the client and server sides of an EPICS V4 "service"
(based on pvAccess RPC), and the use of EPICS V4 Normative Types NTURI and NTTABLE.

The service illustrated is a notional archive service based on fake data just for
making the example, so it's completely self contained and aims for simplicity.

## Building

In the example directory

    mvn package


## To start the server

    mrk> pwd
    /home/epicsv4/master/exampleJava/serviceAPI/shell
    mrk> ./serviceAPIServer
## To start the client
 
    mrk> pwd
    /home/epicsv4/master/exampleJava/serviceAPI/shell
    mrk> ./serviceAPIClient


