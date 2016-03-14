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


## Building

In the helloPutGet directory

    mvn package
    cd shell
    cp sourceEXAMPLE source
    # edit file source so that EPICSV4 is correctly defined



## To start helloPutGet

    mrk> pwd
    /home/epicsv4/master/exampleJava/helloPutGet/shell
    mrk> ./helloPutGet

## To start the  client
 
    mrk> pwd
    /home/epicsv4/master/exampleJava/helloPutGet/shell
    mrk> ./helloClient


