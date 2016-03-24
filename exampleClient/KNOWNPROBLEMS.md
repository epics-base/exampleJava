# exampleClient Known Problems

## BaseV3Channel.java

A null pointer exception is thrown when a second attempt is made to connect to the same channel.


## examplePvaClientMultiDouble

Sometimes this is successful and sometimes just shows

    _____examplePvaClientMultiDouble starting_______
    _example provider pva channels [Ljava.lang.String;@42a57993_

## examplePvaClientNTMulti

Sometimes this is successful and sometimes just shows

    _____examplePvaClientNTMultiDouble starting_______
    _example provider pva channels [Ljava.lang.String;@42a57993_

On the server I saw

    Waiting for exit: Running server...
    Mar 24, 2016 10:47:09 AM org.epics.pvaccess.impl.remote.codec.AbstractCodec processHeader
    WARNING: Invalid header received from client /10.0.0.37:42270, disconnecting...
    Mar 24, 2016 10:47:28 AM org.epics.pvaccess.server.impl.remote.handlers.DestroyChannelHandler handleResponse
    WARNING: Trying to destroy a channel that no longer exists (SID: 2, CID: 2, client: /10.0.0.37:42276).


I also saw the following failure

        _____examplePvaClientNTMultiDouble starting_______
    _example provider pva channels [Ljava.lang.String;@42a57993_
    pvStructure
    epics:nt/NTMultiChannel:1.0 
    any[] value 
        any 
            double value 0.0
        any 
            string value value 0.0
        any 
            double[] value [0.0,1.0,2.0,3.0,4.0]
        any 
            string[] value [value 0.00,value 0.01,value 0.02,value 0.03,value 0.04]
    string[] channelName [PVRdouble,PVRstring,PVRdoubleArray,PVRstringArray]
    alarm_t alarm
        int severity 0
        int status 0
        string message 
    time_t timeStamp
        long secondsPastEpoch 0
        int nanoseconds 0
        int userTag 0
    int[] severity [0,0,0,0]
    int[] status [0,0,0,0]
    string[] message [,,,]
    long[] secondsPastEpoch [1458827604,1458827604,1458827604,1458827604]
    int[] nanoseconds [516000000,517000000,517000000,518000000]
    int[] userTag [0,0,0,0]
    boolean[] isConnected [true,true,true,true]
    Exception in thread "main" java.lang.NullPointerException
	at org.epics.pvaClient.PvaClientNTMultiData.endDeltaTime(PvaClientNTMultiData.java:233)
	at org.epics.pvaClient.PvaClientNTMultiMonitor.poll(PvaClientNTMultiMonitor.java:149)
	at org.epics.pvaClient.PvaClientNTMultiMonitor.waitEvent(PvaClientNTMultiMonitor.java:160)
	at org.epics.exampleClient.ExamplePvaClientNTMulti.example(ExamplePvaClientNTMulti.java:125)
	at org.epics.exampleClient.ExamplePvaClientNTMulti.main(ExamplePvaClientNTMulti.java:147)



    _____examplePvaClientMultiDouble starting_______
    _example provider pva channels [Ljava.lang.String;@42a57993_

## HelloWordRPC

Does not terminate.


