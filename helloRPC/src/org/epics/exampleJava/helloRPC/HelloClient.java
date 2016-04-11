package org.epics.exampleJava.helloRPC;
/**
 * HelloClient is a simple example of an EPIVS V4 client, demonstrating support for a
 * a client/server environment using the ChannelRPC channel type of EPICS V4.  
 */

import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
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
 */
public class HelloClient
{
	// Create the "data interface" required to send data to the hello service. That is,
	// define the client side API of the hello service.
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private final static Structure requestStructure =
		fieldCreate.createStructure(
				new String[] { "personsname" },
				new Field[] { fieldCreate.createScalar(ScalarType.pvString) });

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
		
		try
		{
			// Create an RPC client to the "helloService" service
			// (the connection has already started in background).
			RPCClientImpl client = new RPCClientImpl("helloService");
	
			// Create the data instance used to send data to the server. That is,
			// instantiate an instance of the "introspection interface" for the data interface of
			// the hello server. The data interface was defined statically above.
			PVStructure pvArguments =
				PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
			
			// Get the value of the first input argument to this executable and use it 
			// to set the data to be sent to the server through the introspection interface. 
			String name = args.length > 0 ? args[0] : "anonymous";
			pvArguments.getStringField("personsname").put(name);
				
			try
			{
				// Create an RPC request and block until response is received. There is
				// no need to explicitly wait for connection; this method takes care of it.
				// In case of an error, an exception is throw.
				PVStructure pvResult = client.request(pvArguments, REQUEST_TIMEOUT);
				
				// Extract the result using the introspection interface of the returned 
				// datum, and print it. This particular service never returns a null result.
				String res = pvResult.getStringField("greeting").get();
				System.out.println(res);
			}
			catch (RPCRequestException ex)
			{
				// The client connected to the server, but the server request method issued its 
				// standard summary exception indicating it couldn't complete the requested task.
				System.err.println("Acquisition of greeting was not successful, " +
						"service responded with an error: " + ex.getMessage());
			}
			catch (IllegalStateException ex)
			{
				// The client failed to connect to the server. The server isn't running or
				// some other network related error occurred.
				System.err.println("Acquisition of greeting was not successful, " +
						"failed to connect: "+ ex.getMessage());
			}
			
			// Disconnect from the service client.
			client.destroy();
		}
		finally
		{
			// Stop pvAccess client, so that this application exits cleanly.
			org.epics.pvaccess.ClientFactory.stop();
		}
	}
}
