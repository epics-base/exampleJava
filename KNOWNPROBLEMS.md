# Known Problems

## helloRPC

* This is left almost unchanged since release 4.5.
* helloService does not terminate.

## rdbService and serviceAPI

These are left almost unchanged since release 4.5.

**Greg:** Please look at these examples and decide what should be done.



## examplePvaClientMultiDouble

The C++ implementation allows a mixture of any numeric scaler.
The Java version currently only supports double.

## helloRPC

1) The CPP version allows a client to issueConnect and waitConnect.
The Java version does not support this.

2) The Java implemantation does not terminate.


## Use of provider ca

When the Java implementation uses provider **ca** numeric arrays are filled out with 0s.


## powerSupply

The powerSupplyClient ends with setting voltage to 0.
This causes the PVRecord to throw an exception, which pvAccess passes back to the client.
All appears OK except that if another client has a monitor on the PVRecord,
that client no longer gets an monitor updates.



