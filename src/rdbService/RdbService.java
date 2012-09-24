/**
 * rdbService defines classes for the server side of an EPICS V4 service for accessing
 * a relational database, such as ORACLE.
 */
package rdbService;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.CAException;
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
import org.epics.pvdata.pv.Status.StatusType;

/**
 * RdbService implements an EPICS v4 service for retrieving data from a relational 
 * database (rdb) like Oracle.
 * 
 * In the EPICS v4 services framework, each service is implemented by creating a class with 
 * the signature defined by [TODO: where in fact?]. RdbService is the required factory 
 * Class for the Rdb service. This is the guy a service developer writes.
 * 
 * As written, RdbService expects arguments of the following form:
 * <pre>
 *     string entity      - The entity for which to get data from the relational database,
 *                          eg "SwissFEL:alldevices"
 *     string parameters  - No parameters are supported by rdbService at present. When 
 *                          functionality like text replacement is added, this argument 
 *                          will be used.
 * </pre>
 * This form is not required by the EPICS v4 RPC framework, but is shared by the 3 
 * services I've written so far because it's a pattern that seems to fit many use 
 * cases; you ask a service for a named thing, subject to parameters. It's also the pattern 
 * at the heart of URLs, so it'll be easy to expose EPICS V4 services as Web Services.
 * 
 * These must be defined in the EPICS V4 XML database definition file of the service 
 * (rdbService.xml) and this class must expect and process these in accordance with the 
 * XML file. Note the XML db is part of EPICS V4, and has nothing to do with the 
 * relational database that will be accessed. The XML db is part of the required EPICS 
 * V4 infrastructure of any EPICS V4 RPC type service, this particular service accesses 
 * a relational database like Oracle.  
 * 
 * The service returns results as a PVStructure of normative type NTTable (as NTTable 
 * was defined at the time of writing, it was in flux, as the idea was being driven 
 * by this project).
 * 
 * @author Greg White, 13-Oct-2011 (greg@slac.stanford.edu)
 * @version 7-May-2012, Greg White (greg@slac.stanford.edu) Changed calls to pvAccess api
 * following changes in introspection API.
 * 
 */
public class RdbService
{

	// Logging level.
	private static final Level LOG_LEVEL = Level.INFO;

	private static final String SERVICE_NAME = "rdbService";

	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

	private static class RPCServiceImpl implements RPCService
	{
		private static final Logger logger = Logger.getLogger(RPCServiceImpl.class.getName());

		private final RdbServiceConnection connection;
		
		RPCServiceImpl(RdbServiceConnection connection)
		{
			this.connection = connection;
		}

		/**
		 * Construct and return the requested archive data.
		 * @see org.epics.pvaccess.server.rpc.RPCService#request(org.epics.pvdata.pv.PVStructure)
		 */
		public PVStructure request(PVStructure pvArguments) throws RPCRequestException {
			
			// Retrieve the arguments (entity and parameters). Only the "entity"
			// argument is required for rdbService at this time. That's the
			// one that has the user's given "key" of the SQL query they want,
			// eg "swissfel:allmagnetnames". the "parameters" argument is not supplied
		    // by rdbClient. It is only in the rdbService xml record db because 
			// a this early stage in EPICS v4 it looks like a good idea to make all
			// services xml look identical, so all server code can be cloned.
			//
            PVString pvEntity = pvArguments.getStringField("entity");
            if (pvEntity == null)
            	throw new RPCRequestException(StatusType.ERROR, "Missing required argument");
            String entity = pvEntity.get();

            // pvParameters is not used, so we don't check it.
            //PVString pvParameters = pvArguments.getStringField("parameters");

			// Construct the return data structure "pvTop."
			// The data structure we return here is a pre-release example
			// of the idea of normative types. This pvTop is self identifying that
			// it is a "NTTable". The data payload
			// follows the declaration, in another PVStructure called ResultSet.
            PVStructure pvTop = pvDataCreate.createPVStructure(
            		fieldCreate.createStructure("NTTable", new String[0], new Field[0])
            		);
			
            try
            {
    			String query = connection.entityToQuery(entity);
    			if (query == null)
    			{
    				throw new RPCRequestException(
    						StatusType.ERROR, 
    						"The query name '" + entity + "' was not recognized.");
    			}
            
				// All gone well, so, pass the pvTop introspection interface and the 
				// query string to getData, which will populate the pvTop for us with
				// the data in Oracle.
				//
				connection.getData(query, pvTop);
				
				// Return the data from Oracle, in the pvTop, to the client.
				logger.finer("pvTop = " + pvTop);
				
				return pvTop;
            }
            catch (UnableToGetDataException ex)
            {
            	throw new RPCRequestException(StatusType.ERROR, "Failed to get data from the RDB.", ex);
            }
		}
	}
	
	
	
	public static void main(String[] args) throws CAException
	{
		// initialize nice console logging
		ConsoleLogHandler.defaultConsoleLogging(LOG_LEVEL);
		
		RdbServiceConnection connection = new RdbServiceConnection();

		RPCServer server = new RPCServer();

		// register our service 
		server.registerService(SERVICE_NAME, new RPCServiceImpl(connection));
		
		server.printInfo();
		server.run(0);
	}
	
}
