/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

package org.epics.exampleJava.helloRPC;
import org.epics.nt.NTURI;
import org.epics.nt.NTURIBuilder;
import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientRPC;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;

/**
 * HelloClient is a main class that illustrates a simple example of an EPICS V4
 * client/server interaction, using PvaClient
 *
 * @author Marty Kraimer 2019.04
 * @version Original
 */
public class HelloPvaClient
{
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
        PvaClient pva= PvaClient.get("pva");
        try
        {
            NTURIBuilder nturiBuilder = NTURI.createBuilder();
            Structure requestStructure =
                    nturiBuilder.addQueryString("personsname").createStructure();
            // Create the data instance used to send data to the server. That is,
            // instantiate an instance of the "introspection interface" for the data interface of
            // the hello server. The data interface was defined statically above.
            PVStructure pvArguments =
                    PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
            PvaClientChannel pvaChannel = pva.createChannel("helloService");
            PvaClientRPC clientRPC = pvaChannel.createRPC();

            // Get the value of the first input argument to this executable and use it 
            // to set the data to be sent to the server through the introspection interface. 
            String name = args.length > 0 ? args[0] : "anonymous";
            pvArguments.getStringField("query.personsname").put(name);
            // Create an RPC request and block until response is received. There is
            // no need to explicitly wait for connection; this method takes care of it.
            // In case of an error, an exception is throw.
            PVStructure pvResult = clientRPC.request(pvArguments);
            // Extract the result using the introspection interface of the returned 
            // datum, and print it. This particular service never returns a null result.
            String res = pvResult.getStringField("greeting").get();
            System.out.println(res);
            
            System.out.println("The following should fail");
            requestStructure =
                    nturiBuilder.addQueryString("junk").createStructure();
            pvArguments = PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
            pvResult = clientRPC.request(pvArguments);
            res = pvResult.getStringField("greeting").get();
            System.out.println(res);
        }
        catch (RPCRequestException ex)
        {
            // The client connected to the server, but the server request method issued its 
            // standard summary exception indicating it couldn't complete the requested task.
            System.err.println("Acquisition of greeting was not successful, " +
                    "service responded with an error: " + ex.getMessage());
        }
        catch (Exception ex)
        {
            // The client failed to connect to the server. The server isn't running or
            // some other network related error occurred.
            System.err.println("RPC request failed , " + ex.getMessage());
        } 
    }
}
