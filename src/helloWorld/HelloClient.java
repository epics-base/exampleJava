package helloWorld;
/**
 * HelloClient is a simple example of an E4C client demonstrating support for a
 * a client/server environment in EPICS V4.  
 */

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Status;
import org.epics.rpc.ServiceClient;
import org.epics.rpc.ServiceClientFactory;
import org.epics.rpc.ServiceClientRequester;

/**
 * HelloClient is a main class that illustrates a simple example of an E4C
 * client/server interaction, through the classic Hello World pattern.
 * 
 * HelloClient passes the argument it was given to the helloServer, which
 * constructs and returns a simple greeting. The helloClient receives the
 * greeting, and prints it.
 * 
 * @author 13-Sep-2011, Greg White (greg@slac.stanford.edu)
 * @version 15-Nov-2011, Greg White (greg@slac.stanford.edu) 
 * Modifications for pvAccess/pvData changes w.r.t. modification of shape of 
 * argument PVStructure call-to-call.
 */
public class HelloClient 
{

	/**
	 * main establishes the connection to the helloServer, constructs the
	 * mechanism to pass parameters to the server, calls the server in the EV4
	 * 2-step way, gets the response from the helloServer, unpacks it, and
	 * prints the greeting.
	 * 
	 * @param nameofpersontogreet
	 */
	public static void main(String[] args) 
	{
		// PVStructure pvArguments = null; // The API of the helloService. 
		                                // Also known as its "introspecton interface"
		PVString pvPerson = null;       // The argument of the service we're goint to set.
		
		// Start PVAccess and construct a client connection object
		org.epics.ca.ClientFactory.start();
		Client client = new Client();

		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	    FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	    Field[] fields = new Field[1];
	    fields[0] = fieldCreate.createScalar("personsname",ScalarType.pvString);
	    PVStructure pvArguments = pvDataCreate.createPVStructure(null, "arguments", fields);
		// Connect to the given Service, and retrieve its argument interface
		try
		{
			client.connect("helloService");
			pvPerson = pvArguments.getStringField("personsname");
		}
		catch ( Exception ex )
		{
			System.err.println("Unable to contact helloService. Exiting");
			System.exit(1);	
		}
		
		// Make request. 
		pvPerson.put(args[0]);
		PVStructure pvResult = client.request(pvArguments);

		// If getting the result was successful, extract the value of the
		// expected String field named "greeting" from the returned structure,
		// and print it.
		if (pvResult != null) 
		{
			String res = pvResult.getStringField("greeting").get();
			System.out.println(res);
		} 
		else
			System.out.println("Acquisition of greeting was not successful");

		// Termination: destroy this instance client's resources, stop pvAccess cleanly, and exit.
		client.destroy();
		org.epics.ca.ClientFactory.stop();
		System.exit(0);
	}

	private static class Client implements ServiceClientRequester 
	{
		private ServiceClient serviceClient = null;
		private PVStructure pvResult = null;

		// Connect and wait until connected, or timeout
		void connect(String serivceNameToWhichToConnect) 
		{
			serviceClient = 
				ServiceClientFactory.create(serivceNameToWhichToConnect, this);
			serviceClient.waitConnect(5.0); // 5.0 second timeout to find the service	
		}

		// Cleanup
		void destroy() 
		{
			serviceClient.destroy();
		}

		// Send a request and wait until done
		PVStructure request(PVStructure pvArguments) 
		{
			serviceClient.sendRequest( pvArguments );
			serviceClient.waitRequest();
			return pvResult;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.epics.pvService.client.ServiceClientRequester#connectResult(org
		 * .epics.pvData.pv.Status, org.epics.pvData.pv.PVStructure,
		 * org.epics.pvData.misc.BitSet)
		 */
		@Override
		public void connectResult(Status status) 
		{
			if (!status.isOK()) 
			{
				throw new RuntimeException("Connect error "
						+ status.getMessage());
			}
			return;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.epics.pvService.client.ServiceClientRequester#requestResult(org
		 * .epics.pvData.pv.Status, org.epics.pvData.pv.PVStructure)
		 */
		@Override
		public void requestResult(Status status, PVStructure pvResult) 
		{
			if (!status.isOK()) 
			{
				throw new RuntimeException("request error "
						+ status.getMessage());
			}
			this.pvResult = pvResult;
			return;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.epics.pvData.pv.Requester#getRequesterName()
		 */
		@Override
		public String getRequesterName() 
		{
			return "helloServiceClient";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.epics.pvData.pv.Requester#message(java.lang.String,
		 * org.epics.pvData.pv.MessageType)
		 */
		@Override
		public void message(String message, MessageType messageType) 
		{
			System.out.printf("%n%s %s%n", messageType.toString(), message);
		}
	}
}
