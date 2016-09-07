# exampleLink

This example implements a PVRecord that accesses another PVRecord.

Specific explanations and instructions can be found in the documentation of the parent 
module `exampleJava`.

## Building

In the exampleJava/exampleLink directory

    mvn install


## To start the exampleLink

In the exampleJava/exampleLink directory

On Linux

    ./scripts/exampleLinkMain

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\exampleLinkMain.bat

## To start the exampleLinkClient

In the exampleJava/exampleLink directory

On Linux

    ./scripts/exampleLinkClient

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\exampleLinkClient.bat

## To start doubleArrayMain

In the exampleJava/exampleLink directory

On Linux

    ./scripts/doubleArrayMain

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\doubleArrayMain.bat

In order to exampleLinkMain use this exampleLinkMain must be started with the arguments

    pva doubleArrayExternal false

And exampleLinkClient must be started with the arguments

    pva doubleArrayExternal

    

