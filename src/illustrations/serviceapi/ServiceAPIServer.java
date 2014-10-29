package illustrations.serviceapi;


import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.epics.pvaccess.PVAException;

// Import pvaccess Remote Procedure Call interface
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;

// Import pvData Data Interface things we need
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

// Import pvData Introspection interface we need
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStringArray;

// Get the asynchronous status messaging system things we need
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.factory.StatusFactory;

/**
 * ServiceAPIServer is the server side of an EPICS V4 client server system that
 * illustrates the use of standardized EPICS V4 pvStructures for exchanging data in 
 * the classic case of a client making a parameterized request for data, and the 
 * servers response - that is, a so called "Remote Procedure Call" (or RPC). 
 * 
 * <p>The service uses the pattern of an archive client and server to make the illustration.</p>
 * 
 * <p> The server encodes its response in a pvStructure whose fields map to the components 
 * as defined in one of the EPICS V4 standard (aka "normative") types, named NTTable. See 
 * the EPICS V4 document
 * <a hreh="http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html#nttable">
 * EPICS V4 Normative Types</a>. </p> 
 * 
 * @author Greg White SLAC/PSI, 12-Nov-2012 
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">[1] http://www.ietf.org/rfc/rfc2396.txt</a>
 * @see <a href="http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html">
 * [2] EPICS V4 Normative Types</a>
 * 
 */
public class ServiceAPIServer {

	private final static String SERVICE_NAME = "miniArchiveServiceToDemoServiceInterface";
	
	// The example is of an Archive Service; make some fake archive data for just one PV
	//
	private static ArchiveData archiveData;
	private static String archivedPVname;
    private static DateFormat dateFormater;
    static {
    	archiveData = new ArchiveData(10);
        initArchiveData();
    }
    
    // Create async messages.
    //
	private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
	private static final Status noFieldStatus = 
		statusCreate.createStatus(StatusType.ERROR, "Missing required argument",null);
	private static final Status badParamSyntaxStatus = 
		statusCreate.createStatus(StatusType.ERROR, "Unable to parse starttime or endtime parameter",null);
	private static final Status noMatchingDataStatus = 
		statusCreate.createStatus(StatusType.ERROR, "No archive data timestamps match the conditions",null);
	private static final Status unrecognizedPVnameStatus = 
		statusCreate.createStatus(StatusType.ERROR, "No PV name known to service matches name received",null);


	// Create the introspection interface of the returned data, which is a PV data Structure 
	// of grammar "NTTable". The NTTable has prescribed field names ("labels" and "value"). 
	// The value field must be a structure of scalar arrays (declared above so we can use it here). 
	// The type of these arrays is not prescribed, though they must be same length.  
    //
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private final static Structure valueStructure = fieldCreate.createStructure(
			new String[] {"times", "readings"},
			new Field[] { fieldCreate.createScalarArray(ScalarType.pvString),
					      fieldCreate.createScalarArray(ScalarType.pvDouble)});
	private final static Structure resultStructure = 
			fieldCreate.createStructure( "epics:nt/NTTable:1.0", 
					new String[] { "labels", "value" },
					new Field[] { fieldCreate.createScalarArray(ScalarType.pvString),
							       valueStructure } );
	
	
	static class MiniArchiveServiceToDemoServiceInterfaceImpl implements RPCService
	{
		private Date calStarttime;       // The input starttime as a Date
		private Date calEndtime;         // The input enddate param as a Date

		public PVStructure request(PVStructure uri) throws RPCRequestException {

			// Strings that will hold the values of the arguments sent from the client.
			//
			String pvname;        // PV name for which archive data is requested that 
								  // the client sent in URI query "entity" argument.
			String starttime;     // Archive data is requested from this date/time 
			String endtime;       // Archive data is requested up to this date/time 
			
			try 
			{
				// Retrieve the PV name, and start time and end time, that the client sent.
				// The query parameter named "entity" contains <PV name>;<property>
				//
				pvname = uri.getStructureField("query").getStringField("entity").get();
				starttime = uri.getStructureField("query").getStringField("starttime").get();
				endtime = uri.getStructureField("query").getStringField("endtime").get();
			}
			catch ( Exception ex )
			{
				throw new RPCRequestException( noFieldStatus.getType(), 
						noFieldStatus.getMessage());	
			}
			
			// Process the arguments to get them in a form for this service.
			// E.g., get string arguments of date/time, into Calendar form.
			//
			dateFormater.setLenient(true);
			try 
			{
				calStarttime = dateFormater.parse(starttime);
				calEndtime = dateFormater.parse(endtime);
			}
			catch ( Exception ex )
			{
				throw new RPCRequestException( badParamSyntaxStatus.getType(), 
						badParamSyntaxStatus.getMessage());
			}

			
			// Create a data instance of a resultStructure, using the pvData Data interface methods, and
			// the data interface to this instance. This is how we set and get values to the instance.
			// The data instance is what we'll return to the 
			// caller (EPICS V4 will take care of returning the introspection interface along with it).
			// Then put the archive data into it. Roughly speaking, the data interface methods are 
			// composed of the "PV<type>" equivalents of the "<type>" objects of the introspection interface.
			// 
			PVStructure result = PVDataFactory.getPVDataCreate().createPVStructure(resultStructure);

			// First the labels array
			PVStringArray labelsArray = (PVStringArray) result.getScalarArrayField("labels",ScalarType.pvString);
			// Now the value structure
			PVStructure archiveDataTbl = result.getStructureField("value"); 		    
			PVStringArray datetimesArray = (PVStringArray) 
				archiveDataTbl.getScalarArrayField("times",ScalarType.pvString);
			PVDoubleArray readingsArray = (PVDoubleArray) 
				archiveDataTbl.getScalarArrayField("readings",ScalarType.pvDouble);
			
			// Populate the return NTTable, through the data interface we made to it. Start 
			// with just the labels, then actually select the archive data that is in the time 
			// range sent to the service.
			//
			labelsArray.put(0, 2, new String[] {"sampled time","sampled value"}, 0);
            if ( pvname.equalsIgnoreCase(archivedPVname) )
            {
                int j=0;    // Indexes archive data points found between start and end time. 
                int startj = 0;
	            for ( int i = 0; i < (archiveData.date.length); i++ ) 
	            {		
	            	if ( (( calStarttime == null) || (calStarttime.before(archiveData.date[i])) &&
	            		 (( calEndtime == null) || (calEndtime.after(archiveData.date[i])) )))
					{	
	            		// Copy one item at a time from a source to the output table array.
	            		// See the redingsArray.put below for how to copy a block at a time.
	            		String [] stringarray_ = new String[] {archiveData.date[i].toString()};
	            		datetimesArray.put(j, 1, stringarray_, 0);
	            		if ( j==0) startj = j;
	            		j++;
	                } 
				}
				if ( j== 0)
				{
					throw new RPCRequestException( noMatchingDataStatus.getType(), 
							noMatchingDataStatus.getMessage());	
				}
				// Copy in the block of archived values that matched start time to end time.
				readingsArray.put(0, j-startj, archiveData.value, j);
            }
            else
            {
            	throw new RPCRequestException( unrecognizedPVnameStatus.getType(), 
						unrecognizedPVnameStatus.getMessage());	
            }
			 
			return result;
			
		}

	}
	
	public static void main(String[] args) throws PVAException
	{
		RPCServer server = new RPCServer();

		System.out.println(SERVICE_NAME + " initializing...");
		server.registerService(SERVICE_NAME, new MiniArchiveServiceToDemoServiceInterfaceImpl());
		server.printInfo();
		System.out.println(SERVICE_NAME + " is operational.");

		server.run(0);
	}
	
	/**
	 * The data type of the fake data returned by testArchiveService
	 */
	private static class ArchiveData
	{
		Date[] date;               // Datetime stamp of archive data
		double[] value;                // The value at the time of the datetime stamp
		
		ArchiveData(int points)
		{
			date = new Date[points];
			value = new double[points];
		}
	}
	
    /**
     * Static test data initializer. 
     * 
     * This is fake date/time stamps and associated fake data
     * from which client queries can be drawn.
     */
	private static void initArchiveData()
	{
		archivedPVname = "quad45:bdes;history";
		
		dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss");
		try
		{
			archiveData.date[0] = dateFormater.parse("2011-09-16T00.03.42");
			archiveData.value[0] = 21.2;
			archiveData.date[1] = dateFormater.parse("2011-09-16T01.04.40");
			archiveData.value[1] = 31.2;
			archiveData.date[2] = dateFormater.parse("2011-09-16T02.12.56");
			archiveData.value[2] = 42.2;
			archiveData.date[3] = dateFormater.parse("2011-09-16T04.34.03");
			archiveData.value[3] = 2.76;
			archiveData.date[4] = dateFormater.parse("2011-09-16T06.08.41");
			archiveData.value[4] = 45.3;
			archiveData.date[5] = dateFormater.parse("2011-09-16T08.34.42");
			archiveData.value[5] = 85.3245;
			archiveData.date[6] = dateFormater.parse("2011-09-16T10.01.02");
			archiveData.value[6] = 35.234;
			archiveData.date[7] = dateFormater.parse("2011-09-16T12.03.42");
			archiveData.value[7] = 4.2345;
			archiveData.date[8] = dateFormater.parse("2011-09-16T15.23.18");
			archiveData.value[8] = 45.234;
			archiveData.date[9] = dateFormater.parse("2011-09-16T19.45.50");
			archiveData.value[9] = 56.234;
		
		} catch ( Exception ex )
		{
			System.err.println("Error Constructing fake data at initialization;" + 
					ex.toString());
			System.exit(1);
		}
	}
}
