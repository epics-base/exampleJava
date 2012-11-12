package illustrations.multiplierService;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

/**
 * The MultiplierServiceServer is a bare bones EPICS V4 RPC server intended 
 * to illustrate use the NTScalar normative type through a trivial multiplication service.
 *
 * <p> multiplierService is a minimum client and server implemented in the EPICS v4 RPC "framework" intended
 * to show programming required to implement sending an example of an NTScalar normative type 
 * from the server back to the client. The client demonstrates examination of the datum
 * returned to check for conformance to NTScalar.</p>
 *
 * @see the document EPICS V4 Normative Types
 *  
 * @author Greg White, PSI/SLAC, 1-Oct-2012 
 *
 */
public class MultiplierService {

	private final static String SERVICE_NAME = "multiplierService";
	
	// Create a data type conforming to an NTScalar, by using the pvData introspection interface.
	// This introspection interface can be used later to instantiate objects conforming to the
	// type it describes.
	//
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private final static Structure resultStructure = fieldCreate.createStructure( "uri:ev4:nt/2012/pwd:NTScalar", 
					new String[] { "value", "descriptor" },
					new Field[] { fieldCreate.createScalar(ScalarType.pvDouble),
							      fieldCreate.createScalar(ScalarType.pvString) } );
	
	static class SumReturningNTServiceImpl implements RPCService
	{
		@Override
		public PVStructure request(PVStructure args) throws RPCRequestException {
			
			Double a = args.getDoubleField("a").get();
			Double b = args.getDoubleField("b").get();
		
			// Instantiate an instance of a resultStructure, using the pvData data interface, and
			// assign values to its member fields.
			//
			PVStructure result = PVDataFactory.getPVDataCreate().createPVStructure(resultStructure);
			result.getDoubleField("value").put(a * b);
			result.getStringField("descriptor").put("The product of arguments a and b");
			
			// Return the instance and its introspection interface.
			return result;
		}
	}
	
	public static void main(String[] args) throws CAException
	{

		RPCServer server = new RPCServer();

		// You can register as many services as you want to here ...
		server.registerService(SERVICE_NAME, new SumReturningNTServiceImpl());

		server.printInfo();
		System.out.println(SERVICE_NAME + " is operational.");
		server.run(0);
	}

}
