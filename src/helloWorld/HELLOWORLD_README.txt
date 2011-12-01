HELLOWORLD_README.txt

This is the README file of the helloWorld service.

Auth: Greg White, 19-Sep-2011 (greg@slac.stanford.edu)
Mod:  Greg White, 1-Dec-2011 (greg@slac.stanford.edu), removed dependency on pvService.


EXMAPLE
-------
The following is an example of running the client, giving the argument "Jessica"

          % ./helloClientRunner Jessica
          Hello Jessica

SUMMARY
-------

helloWorld is a minimum client and server in the EPICS v4 services framework, intended
to show the basic programming required to implement a service that just takes an argument
and returns a string.


FILES THAT COMPRISE THE HELLOWORLD EXAMPLE
------------------------------------------
HelloServiceFactory.java          Java source code of the server side of the example
HelloClient.java                  Java source code of the client side of the example
helloClientRunner                 A unix (bash) executable script to run the client side 
helloServerRunner                 A unix (bash) executable script to run the server side 
helloService.xml                  The EPICS V4 record database which personifies 
                                  the hello service.
helloWorld_setup.bash             A unix (bash) source script which initializes the 
                                  runtime environment (as written, for both client and server).

PREREQUISITES
-------------
EPICS V4 components:
1. common  - for source/pvCommon_setup.bash
2. pvAccessJava - for PVAccess 
3. pvDataJava   - for PVData 
4. pvIOCJava    - for JavaIOC rpc
5. exampleJava      - for the example java classes and config files.

SETUP
-----
1. Visit your version of common/script/pvCommon_setup.bash, a set the value of JAVAIOC
2. Visit your version of exampleJava/src/helloWorld/helloWorld_setup.bash and edit for your environment.

EXECUTION
---------
This section describes how you start server and client sides of the hello Service. 
Start the server side first.

To start the Hello World server
-------------------------------
 * cd to the directory containing helloWorld

   E.g. % cd ~/Development/epicsV4/workspace_hg/exampleJava/src/helloWorld
  
 * Edit helloWorld_setup.bash to change WORKSPACE and HELLO.

 * Start the server in one terminal 

   E.g. % ./helloServerRunner
  
 * Terminate the server with a SIGTERM (like CTRL-C in its process window) - after
   you've tested it with the client below of course.
   
   
To run a Hello World Client
---------------------------
 * In another window from the server:
 
 * cd to the directory containing helloWorld (both client and server are in the same dir 
   for demo purposes)
   
   E.g. % cd /Users/greg/Development/epicsV4/workspace_hg/exampleJava/src/helloWorld
 
 * Execute the client side demo, optionally giving your name!
 
   E.g. % ./helloClientRunner greg
        Hello greg
 
 