/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

package org.epics.exampleJava.helloRPC;
/**
 * HelloClient is a simple example of an EPIVS V4 client, demonstrating support for a
 * a client/server environment using the ChannelRPC channel type of EPICS V4.  
 */

import org.epics.nt.NTURI;
import org.epics.nt.NTURIBuilder;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;

/**
 * HelloClient is a main class that illustrates a simple example of an EPICS V4
 * client/server interaction, through the classic Hello World pattern.
 * 
 * <p>HelloClient passes the argument it was given to the helloServer, which
 * constructs and returns a simple greeting. The helloClient receives the
 * greeting, and prints it.</p>
 * 
 * @author Greg White (greg@slac.stanford.edu)
 * @version Matej Sekoranja, Sep-2012, 
 *          Converted to beta 2.
 * @version Greg White, (greg@slac.stanford.edu), 6-Nov-2012, 
 *          Cleanup and simplification.
 * @version Marty Kraimer 10-April-2019
 *          Use NTURI         
 */
public class HelloClient
{
    // Set a pvAccess connection timeout, after which the client gives up trying 
    // to connect to server.
    private final static double REQUEST_TIMEOUT = 3.0;

    /**
     * The main establishes the connection to the helloServer, constructs the
     * mechanism to pass parameters to the server, calls the server in the EV4
     * 2-step way, gets the response from the helloServer, unpacks it, and
     * prints the greeting.
     * 
     * @param args - the name of person to greet
     */
    public static void main(String[] args) 
    {
        // Start the pvAccess client side.
        org.epics.pvaccess.ClientFactory.start();
        // Create an RPC client to the "helloService" service
        // (the connection has already started in background).
        RPCClientImpl client = new RPCClientImpl("helloService");
        try {
            NTURIBuilder nturiBuilder = NTURI.createBuilder();
            Structure requestStructure = nturiBuilder.addQueryString("personsname").createStructure();
            // Create the data instance used to send data to the server. That
            // is,
            // instantiate an instance of the "introspection interface" for the
            // data interface of
            // the hello server. The data interface was defined statically
            // above.
            PVStructure pvArguments = PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);

            System.out.println(pvArguments);
            // Get the value of the first input argument to this executable and
            // use it
            // to set the data to be sent to the server through the
            // introspection interface.
            String name = args.length > 0 ? args[0] : "anonymous";
            pvArguments.getSubField(PVString.class, "query.personsname").put(name);
            // Create an RPC request and block until response is received. There
            // is
            // no need to explicitly wait for connection; this method takes care
            // of it.
            // In case of an error, an exception is throw.
            PVStructure pvResult = client.request(pvArguments, REQUEST_TIMEOUT);

            // Extract the result using the introspection interface of the
            // returned
            // datum, and print it. This particular service never returns a null
            // result.
            String res = pvResult.getStringField("greeting").get();
            System.out.println(res);
            
            System.out.println("The following should fail");
            requestStructure =
                    nturiBuilder.addQueryString("junk").createStructure();
            pvArguments = PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
            pvResult = client.request(pvArguments, REQUEST_TIMEOUT);
            res = pvResult.getStringField("greeting").get();
            System.out.println(res);
        } catch (RPCRequestException ex) {
            // The client connected to the server, but the server request method
            // issued its
            // standard summary exception indicating it couldn't complete the
            // requested task.
            System.err.println("Acquisition of greeting was not successful, " + "service responded with an error: "
                    + ex.getMessage());
        } catch (Exception ex) {
            // The client failed to connect to the server. The server isn't
            // running or
            // some other network related error occurred.
            System.err
                    .println("Acquisition of greeting was not successful, " + "failed to connect: " + ex.getMessage());
        }

        // Disconnect from the service client.
        client.destroy();
    }
}
