package helloWorld;
/**
 * HelloClient is a simple example of an E4C client demonstrating support for a
 * a client/server environment in EPICS V4.  
 */

import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.rpc.ServiceClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

/**
 * HelloClient is a main class that illustrates a simple example of an E4C
 * client/server interaction, through the classic Hello World pattern.
 * 
 * HelloClient passes the argument it was given to the helloServer, which
 * constructs and returns a simple greeting. The helloClient receives the
 * greeting, and prints it.
 * 
 * @author Greg White (greg@slac.stanford.edu)
 * @author Matej Sekoranja
 */
public class HelloClient
{
    private static final Logger logger = Logger.getLogger(HelloClient.class.getName());

	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	
	private final static Structure requestStructure =
		fieldCreate.createStructure(
				new String[] { "personsname" },
				new Field[] { fieldCreate.createScalar(ScalarType.pvString) }
				);
	
	private final static double REQUEST_TIMEOUT = 3.0;
	
	/**
	 * main establishes the connection to the helloServer, constructs the
	 * mechanism to pass parameters to the server, calls the server in the EV4
	 * 2-step way, gets the response from the helloServer, unpacks it, and
	 * prints the greeting.
	 * 
	 * @param args name of person to greet
	 */
	public static void main(String[] args) throws Throwable
	{
		// initialize nice console logging
		ConsoleLogHandler.defaultConsoleLogging(Level.INFO);

		// start pvAccess client
		org.epics.pvaccess.ClientFactory.stop();
		
		try
		{
			// create request structure
			PVStructure pvArguments =
				PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
			
			// set data
			String name = args.length > 0 ? args[0] : "anonymous";
			pvArguments.getStringField("personsname").put(name);
			
			// create an RPC client to the "helloService" service
			// connection has allready started in background
			ServiceClientImpl client = new ServiceClientImpl("helloService");
			
			try
			{
				// create an RPC request and block until response is received
				// no need to explicitly wait for connection, this method takes care of it
				// in case of an error, an exception is throw 
				PVStructure pvResult = client.request(pvArguments, REQUEST_TIMEOUT);
				
				// print the result, this particular service never returns a null result
				String res = pvResult.getStringField("greeting").get();
				logger.info(res);
			}
			catch (RPCRequestException rre)
			{
				logger.log(Level.SEVERE, "Acquisition of greeting was not successful.", rre);
			}
			catch (IllegalStateException rre)
			{
				logger.log(Level.SEVERE, "Acquisition of greeting was not successful, failed to connect.", rre);
			}
			
			// disconnect from the service
			client.destroy();
		}
		finally
		{
			// stop pvAccess client, so that the app cleanly exits
			org.epics.pvaccess.ClientFactory.stop();
		}
	}
}
