# serviceAPI

This example contains a simple illustration of the client and server sides of an EPICS V4 "service"
(based on pvAccess RPC), and the use of EPICS V4 Normative Types NTURI and NTTABLE.

The service illustrated is a notional archive service based on fake data just for
making the example, so it's completely self contained and aims for simplicity.

## Building

In the example directory

    mvn install


## To start the server

On Linux

    ./scripts/serviceAPIServer

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\serviceAPIServer.bat


## To start the client

On Linux

    ./scripts/serviceAPIClient

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\serviceAPIClient.bat



