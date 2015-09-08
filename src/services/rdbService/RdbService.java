/**
 * RdbService defines the server side of an EPICS V4 service for accessing
 * a relational database, such as ORACLE.
 */
package services.rdbService;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Status.StatusType;

/**
 * RdbService implements an EPICS v4 service for retrieving data from a
 * relational database (rdb) like Oracle.
 * 
 * In the EPICS v4 services framework, each service is implemented by creating a
 * class with the signature defined by org.epics.pvaccess.server.rpc.RPCService. This 
 * is the required factory Class for the Rdb service. This is the guy a service
 * developer writes.
 * 
 * As written, RdbService expects its input, the argument to the request method, to be
 * a pvStructure conforming to Normative Type NTURI (see EPICS V4 Normative Types).  
 * Specifically the NTURI must encode a single query argument, named rdbqueryname, whose
 * value is the name of an rdb database query it knows how to process.
 * <pre>
 *     string rdbqueryname - The entity for which to get data from the relational database,
 *                          eg "SwissFEL:alldevices"
 * </pre>
 * 
 * The service returns results as a PVStructure of normative type NTTable (as
 * NTTable was defined at the time of writing, it was in flux, as the idea was
 * being driven by this project).
 * 
 * @author Greg White, 13-Oct-2011 (greg@slac.stanford.edu)
 * @version 08-Sep-2015, Greg White (greg@slac.stanford.edu) 
 *          Move construction of introspection interface for returned data down into 
 *          getData method, in keeping with new v4 API.
 * @version 15-Jan-2013, Greg White (greg@slac.stanford.edu) 
 *          Updated for conformance to NTTable.
 * @version 2-Nov-2012, Greg White (greg@slac.stanford.edu) 
 *          Added use of NTURI normative type. Hence rdbService is Normative Types
 *          compliant, since input is by NNTRI and output by NTTAable, all I/O is 
 *          by normative type. 
 * @version 2-Oct-2012, Greg White (greg@slac.stanford.edu) Converted to using
 *          NTTable normative type.
 * @version 7-May-2012, Greg White (greg@slac.stanford.edu) Changed calls to
 *          pvAccess api following changes in introspection API.
 * 
 */
public class RdbService
{
	// The advertised name of the service - that is, the EPICS V4 PV name of
	// this RPC service.
	private static final String CHANNEL_NAME = "rdbService";

	// Factories for creating the data and introspection interfaces of data
	// exchanged by RdbService.
	private static final FieldCreate fieldCreate = FieldFactory
			.getFieldCreate();
	private static final PVDataCreate pvDataCreate = PVDataFactory
			.getPVDataCreate();

	// Default console logging level.
	private static final Level LOG_LEVEL = Level.INFO;

	/**
	 * The implementation class of the Relational Database service - 
	 * an EPICS V4 RPC channel example.
	 *  
	 * @author Greg White, 9-Nov-2012.
	 *
	 */
	private static class RPCServiceImpl implements RPCService
	{
		// Acquire the logging interface
		private static final Logger logger = Logger
				.getLogger(RPCServiceImpl.class.getName());

		// The pvAccess connection delegate for the RDB service.
		private final RdbServiceConnection connection;
		RPCServiceImpl(RdbServiceConnection connection)
		{
			this.connection = connection;
		}

		/**
		 * Construct and return the requested database data, given an NTURI that
		 * encodes the name of a relational database query, as understood by this service.
		 * 
		 * @see org.epics.pvaccess.server.rpc.RPCService#request(org.epics.pvdata.pv.PVStructure)
		 */
		public PVStructure request(PVStructure pvUri)
				throws RPCRequestException
		{

			// Retrieve the argument, the name of a db query, as encoded by NTURI. 
			// This is the user's given "key" of the SQL query they want,
			// eg "swissfel:allmagnetnames". 
			//
			PVString pvRbbQueryName = pvUri.getStructureField("query").getStringField("rdbqueryname");
			if (pvRbbQueryName == null)
				throw new RPCRequestException(StatusType.ERROR,
					"Missing required argument, \n"+
					"query rdbqueryname - the name of query as understood by the RDB service");
			String queryname = pvRbbQueryName.get();


			// Declare top level PVStructure that will be returned to client.
			PVStructure pvTop = null;
			
			// Look up the actual SQL to run on the DB given the name of the SQL query
			// "key" given by the user. Then execute it on the DB, and return the result.
			try
			{
				String query = connection.entityToQuery(queryname);
				if (query == null)
				{
					throw new RPCRequestException(StatusType.ERROR,
							"The query name '" + queryname + "' was not recognized.");
				}

				// All gone well getting the SQL query, so get the data of the query,
				// populate the return structure with it. 
				//
				pvTop = connection.getData(query);

				logger.finer("pvTop = " + pvTop);

				// Return the data so acquired and populated, in the pvTop, to the client.
				return pvTop;
				
			} catch (UnableToGetDataException ex)
			{
				throw new RPCRequestException(StatusType.ERROR,
						"Failed to get data from the RDB.", ex);
			}
		}
	}

	public static void main(String[] args) throws PVAException
	{
		// Initialize nice console logging.
		ConsoleLogHandler.defaultConsoleLogging(LOG_LEVEL);

		// Initialize database connection.
		RdbServiceConnection connection = new RdbServiceConnection();

		// Instantiate a service instance.
		RPCServer server = new RPCServer();

		// Register channels to which this service should respond over pvAccess.
		server.registerService(CHANNEL_NAME, new RPCServiceImpl(connection));

		server.printInfo();
		
		// Start the service.
		server.run(0);
	}

}
