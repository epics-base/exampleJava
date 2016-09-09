# helloPutGet

The example implements a simple service that has a top level pvStructure:

    structure
        structure argument
            string value
        structure result
            string value
            time_t timeStamp
                long secondsPastEpoch
                int nanoseconds
                int userTag


It is designed to be accessed via a channelPutGet request.
The client sets argument.value
When the record processes it sets result.value to "Hello " 
concatenated with argument.value.
Thus if the client sets argument.value equal to "World"
result.value will be "Hello World".
In addition the timeStamp is set to the time when process is called.</p>

Specific explanations and instructions can be found in the documentation of the parent 
module `exampleJava`.


## Building

In the helloPutGet directory

    mvn install

## To start helloPutGet

There are wrapper scripts for Windows and Linux in the `scripts` directory.
From the current directory (where this file is located), run

on Linux:

    ./scripts/helloPutGetMain

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (or where your Java is)
    .\scripts\helloPutGetMain.bat
    

## To start the  client

on Linux:
 
    ./scripts/helloPutGetClient

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (or where your Java is)
    .\scripts\helloPutGetClient.bat
    

