# powerSupply


This is an example of creating a service that requires a somewhat complicated top level PVStructure.

The example record has the structure:

    powerSupply
    structure 
        alarm_t alarm
            int severity
            int status
            string message
        time_t timeStamp
            long secondsPastEpoch
            int nanoseconds
            int userTag
        structure power
            double value
        structure voltage
            double value
        structure current
            double value

The record is meant to be accessed via a channelPutGet request.
The client can provide values for power.value and voltage.value.
The process routine computes current.value.


## Building

In the exampleJava/powerSupply directory

    mvn install


## To start examplePowerSupplyMain

In the exampleJava/powerSupply directory

On Linux

    ./scripts/powerSupplyMain

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\powerSupplyMain.bat

## To start powerSupplyMonitor

There is also an example to monitor changes in the powerSupply.

In the exampleJava/powerSupply directory

On Linux

    ./scripts/powerSupplyMonitor

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\powerSupplypowerSupplyMonitor.bat

   
## To start powerSupplyClient

In the exampleJava/powerSupply directory

On Linux

    ./scripts/powerSupplyClient

On Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (where your Java is)
    .\scripts\powerSupplyClient.bat


This client calls channelPutGet several times specifying different value for power and voltage.

The last call sets the voltage to 0, which results in process throwing an exception.

