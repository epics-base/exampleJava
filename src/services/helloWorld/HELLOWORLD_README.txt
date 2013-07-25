HELLOWORLD_README.txt

This is the README file of the helloWorld service.

Auth: Greg White, 19-Sep-2011 (greg@slac.stanford.edu)
Mod:  Greg White, 1-Dec-2011 (greg@slac.stanford.edu), removed dependency on pvService.
      Greg White, 15-Oct-2012 (greg@slac.stanford.edu), updated for beta 2.      


EXMAPLE
-------
The following is an example of running the client, giving the argument "Jessica"

          % ./helloClientRunner Stefania
          Hello Stefania

SUMMARY
-------

helloWorld is a minimum client and server in the EPICS v4 services framework, intended
to show the basic programming required to implement a service that just takes an argument
and returns a string.


FILES THAT COMPRISE THE HELLOWORLD EXAMPLE
------------------------------------------
HelloService.java                 Java source code of the server side of the example
HelloClient.java                  Java source code of the client side of the example
helloClientRunner                 A unix (bash) executable script to run the client side 
helloServerRunner                 A unix (bash) executable script to run the server side 
helloWorld_setup.bash             A unix (bash) source script which initializes the 
                                  runtime environment (as written, for both client and server).

PREREQUISITES
-------------
EPICS V4 components:
0. caj and jca, prerequisities of pvAccessJava. If you acquired pvAccessJava by
   by maven, you will have the right versions in your maven repo.
2. pvAccessJava - for PVAccess 
3. pvDataJava   - for PVData 
4. exampleJava      - for the example java classes and config files.

SETUP
-----
Visit your version of exampleJava/src/services/helloWorld/helloWorld_setup.bash 
and edit for your environment. 

In particular, set the WORKSPACE variable to the name of the directory 
containing the exampleJava, pvDataJava and pvAccessJava modules. 

EXECUTION
---------
This section describes how you start server and client sides of the 
hello Service. Start the server side first.

To start the Hello World server
-------------------------------
 * cd to the directory containing helloWorld

   E.g. % cd ~/Development/epicsV4/workspace_hg_beta2/exampleJava/src/services/helloWorld
  
 * Start the server in one terminal 

   E.g. % ./helloServerRunner
  
   [You can terminate the server with a SIGTERM (like CTRL-C in its 
    process window) - after you've tested it with the client below of 
    course.]
   
   
To run a Hello World Client
---------------------------
 * In another window from the server:
 
 * cd to the directory containing helloWorld (both client and server are in the same dir 
   for demo purposes)
   
   E.g. % cd ~/Development/epicsV4/workspace_hg_beta2/exampleJava/src/services/helloWorld
 
 * Execute the client side demo, optionally giving your name!
 
   E.g. % ./helloClientRunner greg
        Hello greg
 
 