/**
 * helloWorld is meant to illustrate the simplest example of a remote procedure call (RPC)
 * style interaction between a client and a server using EPICS V4. 
 */
package services.helloWorld;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;

/**
 * HelloService is an example of a trivial EPICS V4 Remote Procedure Call (RPC) server side.
 * 
 * <p>As written, HelloService would respond to a ChannelRPC request for the EPICS V4 channel 
 * named "helloService".</p>
 * 
 * @author Greg White (greg@slac.stanford.edu)
 * @version Matej Sekoranja, Sep-2012 converted to beta 2.
 */
public class HelloService
{

	// All EPICS V4 services return PVData objects (by definition). Create the
	// factory object that will allow you to create the returned PVData object
	// later.
	//
	private static final PVDataCreate pvDataCreate = 
		PVDataFactory.getPVDataCreate();
	private static final FieldCreate fieldCreate = 
		FieldFactory.getFieldCreate();

	// This service result structure type definition.
	private final static Structure resultStructure = fieldCreate
			.createStructure(
					new String[] { "greeting" },
					new Field[] { fieldCreate.createScalar(ScalarType.pvString) });

	/**
	 * Implementation of RPC service.
	 */
	static class HelloServiceImpl implements RPCService
	{
		public PVStructure request(PVStructure args) throws RPCRequestException
		{
			// Extract the arguments. Just one in this case.
			// Report an error by throwing a RPCRequestException.
			PVString inputPersonNameField = args.getStringField("personsname");
			if (inputPersonNameField == null)
				throw new RPCRequestException(StatusType.ERROR,
						"PVString field with name 'personsname' expected.");

			// Create the result structure of the data interface.
			PVStructure result = pvDataCreate
					.createPVStructure(resultStructure);

			// Extract from the constructed data interface the value of
			// "greeting" field. The value we'll return, is "Hello" concatenated
			// to the value of the input parameter called "personsname".
			PVString greetingvalueField = result.getStringField("greeting");
			greetingvalueField.put("Hello " + inputPersonNameField.get());

			return result;
		}
	}

	/**
	 * Main is the entry point of the HelloService server side executable. 
	 * @param args None
	 * @throws CAException
	 */
	public static void main(String[] args) throws PVAException
	{
		RPCServer server = new RPCServer();

		// register our service as "helloService"
		server.registerService("helloService", new HelloServiceImpl());

		server.printInfo();
		server.run(0);
	}

}
