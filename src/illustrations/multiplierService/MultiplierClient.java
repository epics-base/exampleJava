package illustrations.multiplierService;

import org.epics.pvaccess.easyPVA.*;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

/**
* The MultiplierServiceClient is a client for a bare bones EPICS V4 RPC server intended 
* to illustrate use the NTScalar normative type through a trivial multiplication service.
*  
* @author Greg White, PSI/SLAC, 1-Oct-2012 
*
*/
public class MultiplierClient 
{
	// Create a data type used to send a request to the service. We do this by in fact 
	// creating an introspection interface to a data type, giving its structure. 
	// We will use this introspection interface to create an instance request structure
	// the multiplier service understands; a structure with two fields, named "a" and "b".
	//
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private final static Structure requestStructure =
		fieldCreate.createStructure(
				new String[] { "a", "b" },
				new Field[] { fieldCreate.createScalar(ScalarType.pvDouble),
							  fieldCreate.createScalar(ScalarType.pvDouble) } );
	
	public static void main(String[] args) throws Throwable 
	{
		EasyPVA easyPVA = EasyPVAFactory.get();

		// Create an instance of the data type, and populate it.
		PVStructure request = PVDataFactory.getPVDataCreate().
			createPVStructure(requestStructure);
		request.getDoubleField("a").put(12.3);
		request.getDoubleField("b").put(45.6);
		
		System.out.println("request = \n" + request + "\n"); 
		
		// Call the multiplier service sending the request in a structure 
		PVStructure result = easyPVA.createChannel("multiplierService").createRPC().request(request);
			
		// Well written clients would examine the returned structure via its introspection interface, 
		// to check whether its identifier says that it is a normative type, and the type we expected.
		if (!result.getStructure().getID().equals("uri:ev4:nt/2012/pwd:NTScalar")) 
		{
		    System.err.println("Unexpected data identifier returned from multiplierService: " + 
		    		"Expected Normative Type ID uri:ev4:nt/2012/pwd:NTScalar, but got "
		        + result.getStructure().getID());
		    System.exit(-1);
		}

		// Simply extract the required value field returned. And print it.
		double product = result.getDoubleField("value").get();
		System.out.println( "value = " + product);

		// See if there was also the descriptor subField, and if so, get it and print it.
		PVString descriptorpv = (PVString)result.getSubField("descriptor");
		if ( descriptorpv != null)
			System.out.println( "descriptor = " + descriptorpv.get());
		
		// Or just print everything we got:
        System.out.println("\nWhole result structure toString =\n" + result);

        System.exit(0);
	}
}
