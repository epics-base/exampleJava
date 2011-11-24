RDBSERVICE_README.txt

This is the README file of the rdbService module. rdbService is a 
minimal EPICS V4 service which demonstrates the use of EPICS V4 RPC support in pvService
for implementing a relational database service, and client that shows how
users can trivially get data from a db like Oracle.

Auth: Greg White, 13-Oct-2011 (greg@slac.stanford.edu) / gregory.white@psi.ch

EXAMPLE
-------
The following is an example of running the client. This examples gets details
about all the devices in the SwissFEL Test accelerator from the PSI Oracle database,
and prints them to the terminal. For brevity, only 6 rows of the output are included here:

  % ./rdbClientRunner swissfeltest:alldevices
        NAME         PREFIX  SUFFIX                 DESCRIPTION                    RESPONSIBLE    
    F10BC-VVPG24010    F10BC    VVPG                           valve gate DN 40     Lothar Schulz   
       F10BC-MCRY10    F10BC    MCRY                           corrector magnet    Marco Negrazus  
    F10BC-VCHB24010    F10BC    VCHB                           bellow DN 40/120     Lothar Schulz    
       F10BC-MCRX10    F10BC    MCRX                           corrector magnet   Marco Negrazius   
    F10BC-VCHA24010    F10BC    VCHA                             dipole chamber     Lothar Schulz  
       F10BC-MBND10    F10BC    MBND                                     dipole    Marco Negrazus   
  <rows and columns snipped for clarity>


SUMMARY
-------

rdbService is a minimum client and server implemented in the EPICS v4 RPC "framework" intended
to show programming required to implement a service that gets data out 
of a relational database, like Oracle. The server side receives a named request for 
data, such as "swissFEL:allQUADs" and finds the SQL SELECT (or equivalent) database 
query for that name. It then executes the database query, and returns the resulting 
table of data to the client. 
	RdbService illustrates mapping a JDBC ResultSet returned by a relational DB query,
to a PVStructure (in fact, specifically an early version of the NTTable normative type), 
extracting the data from a PVStructure (see use of GetHelper class) and a tabular data formatting
utility useful for printing the tabular contents of an NTTable (see use of NamedValues class).


FILES THAT COMPRISE THE RDB SERVICE EXAMPLE
-------------------------------------------
RdbServiceFactory.java          Java source code of the server side of the example
RdbClient.java                  Java source code of the client side of the example
rdbClientRunner                 A unix (bash) executable script to run the client side 
getRdb                          A wrapper for rdbClientRunner, with a more descriptive name
rdbServerRunner                 A unix (bash) executable script to run the server side 
rdbService.xml                  The EPICS V4 record database which personifies 
                                the RDB service.
rdb_setup.bash                  A unix (bash) source script which initializes the 
                                runtime environment (as written, for both client and server).
UnableToGetDataException        A simple Exception class used to indicate inability to get data.


PREREQUISITES
-------------
EPICS V4 components:
1. common  - for source/pvCommon_setup.bash
2. pvAccess - for PVAccess 
3. pvData   - for PVData 
4. pvIOC     - for JavaIOC 
5. pvService - for pvService.rpc Java classes
6. example   - for the rdbService java classes and config files.

CONFIGURATION
-------------
0. Most importantly, work out how your rdbService server side will translate the names
   it is given, like "myaccelerators:magnetdata" into a SQL SELECT statement. As shipped
   rdbService does this in RdbServiceFactory, by looking up the name it was given in 
   a trivial 2-column Oracle table called EIDA_NAMES (see RdbServiceFactory.java). Eg:

   sls:alldevices	         Select * from sls.alldevices_v
   sls:allmagnets	         Select * from sls.magnetdevice_v order by domain,ds
   sls:allmagnetdata	     Select * from sls.magnetddata_v order by type
   swissfeltest:sectiontree	 Select *  from swissfel.swissfel_test_tree_v

   Then edit your version of rdbServiceRunner to pass the right Java Properties (-Ds)
   
1. Edit your version of common/script/pvCommon_setup.bash, and set the value of JAVAIOC

2. Edit your version of exampleJava/src/rdbService/rdbSetup.bash and set it for your environment.
 
EXECUTION
---------
This section describes how you start server and client sides of the rdb Service. 
Start the server side first.

To start the rdbService server 
------------------------------
1. cd to the directory containing the rdbService source files:

   E.g. % cd ~/Development/epicsV4/workspace_hg/exampleJava/src/rdbService
  
2. If you have never started the server before (for instance you are deploying a fresh install), 
   then you'll need to edit rdb_setup.bash to change values of WORKSPACE and RDBXML (possibly others).


3. Start the server in one terminal 

   E.g. % ./rdbServerRunner
   
   Verify that the line beginning "installed records.." shows records from rdbService.xml, 
   and it looks successful. You should see, at the end of the startup echos, "Running server ..."
  
4. Terminate the server with a SIGTERM (like CTRL-C in its process window) - after
   you've tested it with the client below of course.
   
   
To run the rdbService example client
--------------------------------------------
In another window from the server:
 
1. cd to the directory containing the rdbService files (both client and server are in the same dir 
   for demo purposes)
   
   E.g. % cd ~/Development/epicsV4/workspace_hg/exampleJava/src/rdbService
 
2. Execute the client side example runner script rdbClientRunner, giving as an argument the name
   of some query you know is understood by the server. In this example "LCLS:elementInfo.byZ" is 
   theis "key" understood by the rdbService to lookup the SQL SELECT string which is itself 
   the SQL query the service executes to get the data. 
 
   E.g. % ./rdbClientRunner  swissfeltest:alldiag
   
   Alternatively, use the getRdb wrapper of the client side executable:
   
   E.g. % getRdb swissfeltest:alldiag
   

IMPLEMENTATION DETAILS
======================
At PSI there is an Oracle database for SLS and anther for SWissFEL projects. A single oracle "schema"
named EIDA has been granted read access to both of these. The example rdbService uses that 
schema to access data in the other 2 schema. 

The name to SQL query mapping is stored, in this example, within the EIDA schema itself (though
other implementations may like to keep the mapping elsewhere, such as in a directory service). 
Specifically, EIDA_NAMES is a table in EIDA, which just has 2 important columns, the "names" of
queries (what a user asks for), and the SQL statement equivalent to that query (the SQL
that the rdb Server executes when the user asks for a given name. Here is an extract from EIDA_NAMES:

NAME                        QRY
------------------------    ---------------------------------------------------------------
sls:alldevices	            Select * from sls.alldevices_v
sls:allmagnets	            Select * from sls.magnetdevice_v order by domain,ds
sls:allmagnetdata	        Select * from sls.magnetddata_v order by type
swissfeltest:sectiontree	Select * from swissfel.swissfel_test_tree_v
swissfeltest:alldevices	    Select * from swissfel.feltest_heilig_v order by PREFIX,"Z0(M)"
<many rows snipped for clarity>

So, when a user asks for, for example, swissfeltest:alldevices, the rdbService first looks
in EIDA_NAMES to find the QRY whose NAME is swissfeltest:alldevices, and then it executes the
SQL statement it finds 'Select * from swissfel.feltest_heilig_v order by PREFIX,"Z0(M)". Simple as that.

Eg:
[gregsmac:exampleJava/src/rdbService] greg% ./rdbClientRunner swissfeltest:alldevices | more
        NAME         PREFIX  SUFFIX                 DESCRIPTION                    RESPONSIBLE    
    F10BC-VVPG24010    F10BC    VVPG                           valve gate DN 40     Lothar Schulz   
       F10BC-MCRY10    F10BC    MCRY                           corrector magnet    Marco Negrazus  
    F10BC-VCHB24010    F10BC    VCHB                           bellow DN 40/120     Lothar Schulz    
       F10BC-MCRX10    F10BC    MCRX                           corrector magnet   Marco Negrazius   
    F10BC-VCHA24010    F10BC    VCHA                             dipole chamber     Lothar Schulz  
       F10BC-MBND10    F10BC    MBND                                     dipole    Marco Negrazus   
<rows and columns snipped for clarity>

PERFORMANCE
-----------
No serious performance measurements have been done. Note that the server side has been
written for illustrative clarity as an EPICS V4 example, not for performance. But for example, 
see the mid-sized sample below: *including* client image startup (ie starting a java vm and executing the 
client), this SwissFel ORACLE db query took about 3/4s user time, to request, execute 
the SQL, get ResultSet from oracle, return it to the client as a PVStructure, and client 
side to format, about 560 rows of data.

* Small size sample: 2610 characters, 18 rows, takes 1/2 second. Probably mostly image startup.
[gregsmac:exampleJava/src/rdbService] greg% time ./rdbClientRunner swissfeltest:sectiontree > /dev/null
0.541u 0.104s 0:02.20 29.0%     0+0k 9+2io 373pf+0w
[gregsmac:exampleJava/src/rdbService] greg% time ./rdbClientRunner swissfeltest:sectiontree | wc
      18     188    2610
* Mid size sample: ~600 rows in 3/4 second.
[gregsmac:exampleJava/src/rdbService] greg% time ./rdbClientRunner swissfeltest:alldevices > /dev/null
0.774u 0.076s 0:00.61 137.7%    0+0k 0+0io 8pf+0w
[gregsmac:exampleJava/src/rdbService] greg% ./rdbClientRunner swissfeltest:alldevices | wc
     567    9556  120204
* Large size sample: 60M characters (all the pvname data) in SLS, took 13 seconds:
[gregsmac:exampleJava/src/rdbService] greg% time ./rdbClientRunner sls:allpvnames >/dev/null
13.270u 2.201s 0:38.66 40.0%    0+0k 21+72io 8pf+0w
[gregsmac:exampleJava/src/rdbService] greg% ./rdbClientRunner sls:allpvnames | wc
  348848 3136676 60350704

So, extremely roughly speaking, a rdbClient user sees 1/2 Megabyte query result data per second, 
based on simple unoptimized Oracle queries executed by unoptimized JDBC, and, importantly, including the 
reformatting for printing that application would probably skip or do itself.


