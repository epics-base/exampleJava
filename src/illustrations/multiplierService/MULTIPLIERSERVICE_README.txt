MULTIPLIERSERVICE_README.txt

This is the README file of the multiplierService module. multiplierService is a 
trivial EPICS V4 service which demonstrates the simplest use of an EPICS V4 Normative Type.
Specifically, it demonstrates the NTScalar.

Auth: Greg White, 2-Oct-2012 (greg@slac.stanford.edu) / gregory.white@psi.ch


EXAMPLE
-------
The following is an example of running the client. 

request = 
structure 
    double a 12.3
    double b 45.6

value = 560.88
descriptor = The product of arguments a and b

Whole result structure toString =
uri:ev4:nt/2012/pwd:NTScalar 
    double value 560.88
    string descriptor The product of arguments a and b


SUMMARY
-------

multiplierService is a minimum client and server implemented in the EPICS v4 RPC "framework" intended
to show programming required to implement sending an example of an NTScalar normative type 
from the server back to the client. The client demonstrates examination of the datum
returned to check for conformance to NTScalar.

For a description of Normative Types, see document EPICS V4 Normative Types.


FILES THAT COMPRISE THE MULTIPLIER SERVICE EXAMPLE
-------------------------------------------
MultiplierService.java   - The server side of the trivial service, implemented as ChannelRPC.
MultiplierClient.java    - The client side of the trivial service.


REQUISITES
----------
EPICS V4 components. Basically, check these repos out of EPICS V4 Mercurial
1. pvAccessJava - for PVAccess 
2. pvDataJava   - for PVData 
3. easyPVA      - for the EasyPVA API. Presently easyPVA is in repo alphaJava

CONFIGURATION
-------------
There is no significant configuration. 
 
EXECUTION
---------
This section describes how you start server and client sides of the multiplier Service. 
Start the server side first.

To start the mutiplierService server 
------------------------------
1. cd to the directory containing the multiplierService source files:

   E.g. % cd ~/Development/epicsV4/workspace_hg_beta2/exampleJava/src/illustrations/multiplierService
  
2. Start the server in one terminal 

   E.g. % ./multiplierServerRunner
   
   You should see, at the end of the startup echos, and finally, "multiplierService is operational."  
   
To run the multiplierService example client
--------------------------------------------
In another window from the server:
 
1. cd to the directory containing the multiplierService files (both client and server are in the same dir 
   for demo purposes)
   
   E.g. % cd ~/Development/epicsV4/workspace_hg_beta2/exampleJava/src/illustrations/multiplierService

3. Execute the client side example runner script multiplierClientRunner.
   
   E.g. % ./multiplierClientRunner 
   

IMPLEMENTATION DETAILS
======================
There are no serious implementation details.

PERFORMANCE
===========
No performance measurements have been done. 

BIBLIOGRAPHY
============
[1] At the time of writing, the Normative Types doc is at 
http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html


