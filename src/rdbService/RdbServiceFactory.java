/**
 * rdbService defines classes for the server side of an EPICS V4 service for accessing
 * a relational database, such as ORACLE.
 */
package rdbService;

import java.util.*;
import java.sql.*; // Oracle connections
import org.epics.ca.client.Channel;
import org.epics.ca.client.ChannelRPCRequester;
import org.epics.ioc.database.PVRecord;
import org.epics.ioc.pvAccess.RPCServer;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.pv.Status.StatusType;
import org.epics.pvData.pv.*;

/**
 * RdbServiceFactory implements an EPICS v4 service for retrieving data from a relational 
 * database (rdb) like Oracle.
 * 
 * In the EPICS v4 services framework, each service is implemented by creating a class with 
 * the signature defined by [TODO: where in fact?]. RdbServiceFactory is the required factory 
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
 * 
 */
public class RdbServiceFactory
{

	// Define factory to create an instance of this service
	public static RPCServer create()
	{
		return new RPCServerImpl();
	}

	// Create Status codes used by this service
	//
	private static final boolean DEBUG = false; // Whether to print debugging
	                                            // info.

	private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
	private static final Status okStatus = statusCreate.getStatusOK();
	private static final Status missingRequiredArgumentStatus = statusCreate.createStatus(StatusType.ERROR,
	        "Missing required argument", null);
	private static final Status noMatchingQueryStatus = statusCreate.createStatus(StatusType.ERROR,
	        "The query name given was not recognized", null);

	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

	private static class RPCServerImpl implements RPCServer
	{

		private ChannelRPCRequester channelRPCRequester; 
		private String entity; // String in pvEntity.

		
		/*
		 * Declare elements of the interface between the pvIOC server side and this server. 
		 * Why the SuppressWarnings? Because these elements are populated pvIOC and READ FROM in 
		 * this class, which Eclipse erroneously interprets as unused, so we override that warning.
		 */
		@SuppressWarnings("unused")           
		private PVStructure m_pvRequest;      // ??
		@SuppressWarnings("unused")           // m_pvParameters is not used in this example.
		private PVString m_pvParameters;      // Would be used to send qualifying information about the 
		                                      // entity, but in fact it's not used in this server.
		private PVString m_pvEntity;          // The name of the SQL query to run against Oracle

		// TODO: Externalize and protect access username/passwords and strings
		// TODO: Convert connection string to java Property.
		private static final String SERVICE_NAME = "rdbService";

		// Oracle JDBC connection URI and ID stuff.
		//
		private static Connection m_Conn = null; // JDBC connection for queries
		private static final String CONNECTION_URI_DEFAULT = 
			"jdbc:oracle:thin:@gfadb05s.psi.ch:1521:GFAPRD";
		private static final String CONNECTION_USERID_DEFAULT = "eida";
		private static final String CONNECTION_PWD_DEFAULT = "nicetry";
		private static final String NORESULTSETMETADATA = 
			"No ResultSet metadata available, so can not continue to get data";
		private static final int MAX_RETRIES = 2; // Try a SQL query at most 2 times
		                                          // before reinit and requery.
	
		// Index of the column of eida.eida_names that contains the query string.
		private static final int QRYCOLUMNNUM = 1; 
		

		// Send message of status.
		// TODO: Expand this to differentially handling the 3 endpoints: 1)
		// user's client, 2) global message log, 3) local stderr.
		private void msg(String message)
		{
			// TODO: Make these go back to the client. Following line causes Connection Error
			// channelRPCRequester.message(message, MessageType.error);
			System.err.println(SERVICE_NAME + ": " + message);
		}

		private void msgl(String message)
		{
			msg(message);
			System.err.println(SERVICE_NAME + ": " + message);
		}

		private void msgl(Throwable throwable, String message)
		{
			msg(throwable.toString() + " " + message);
			System.err.println(SERVICE_NAME + ": " + throwable.toString() + ": " + message);
		}

		/**
		 * Initialize for an acquisition.
		 * 
		 * RPCServerImpl is called by the pvIOC framework at server start and on each service request, so if 
		 * you want to execute initializations only once, you have to check if it's already done.
		 * Note, in this service example, we use a pattern where the initialization is done on
		 * server startup, and the important part (getConnection) can be redone at any time if the connection
		 * to the backend rdb goes bad. 
		 */
		RPCServerImpl()
		{
			if (m_Conn == null)
				init();
		}

		/**
		 * init loads JDBC and initializes connection to the db, Oracle in this case.
		 */
		private void init()
		{
			// Load JDBC.
			try
			{
				msg("Loading Oracle database runtime connection");
				Class.forName("oracle.jdbc.OracleDriver");

			} catch (Exception ex)
			{
				msg(ex.getMessage() + " - while registering driver for connection to Oracle.");
			}
			
			// Establish connection to the Oracle instance defined in private members.
			getConnection();
			if (m_Conn != null)
				msg("Oracle database connection completed successfully");
			else
				msg("Unable to establish connection to RDB with URI " + CONNECTION_URI_DEFAULT);
		}

		/**
		 * Initializes a database connection to the Oracle Database.
		 * 
		 * If the connection is non-null, then the old connection is first closed. This part is include so that this
		 * routine can be used to renew a stale connection.
		 */
		private void getConnection()
		{
			// If we already have a connection dispose of it.
			closeConnection();

			// Having dealt with a possible stale connection, get a new one.
			try
			{
				// Get DB connection JDBC URL, using hard coded one
				// (CONNECTION_URI_DEFAULT) as the default if no CONNECTION_URI_OPTIONAL 
				// is given in Properties. Then get the connection. Use property 
				// CONNECTION_USERID_PWD if given, otherwise hard coded.
				//
				String connectionString = System.getProperty("CONNECTION_URI_PROPERTY", CONNECTION_URI_DEFAULT);
				String pwd = System.getProperty("CONNECTION_PWD", CONNECTION_PWD_DEFAULT);
				msgl("Initializing database connection: " + connectionString);
				m_Conn = DriverManager.getConnection(connectionString,
				        System.getProperty("CONNECTION_USERID_PROPERTY", CONNECTION_USERID_DEFAULT), pwd); 
		
			} catch (Exception ex)
			{
				msgl(ex, " while " + SERVICE_NAME + " initialising connection to database");
			}
		}

		/**
		 * Disposes of existing Db connection.
		 */
		private void closeConnection()
		{
			// If we have a connection, dispose of it.
			try
			{
				if (m_Conn != null)
				{
					if (!m_Conn.isClosed())
					{
						msgl("Closing Database db connection");
						m_Conn.close();
						m_Conn = null;
					}
				}
			} catch (Exception ex)
			{
				msgl(ex, "while closing database connection");
			}
		}

		/* We have to override destroy.
		 * (non-Javadoc)
		 * 
		 * @see org.epics.ioc.pvAccess.RPCServer#destroy()
		 */
		@Override
		public void destroy()
		{
		}

		/**
		 * 
		 * 
		 * @see org.epics.ioc.pvAccess.RPCServer#initialize(org.epics.ca.client.Channel ,
		 * org.epics.ioc.database.PVRecord, org.epics.ca.client.ChannelRPCRequester, org.epics.pvData.pv.PVStructure,
		 * org.epics.pvData.misc.BitSet, org.epics.pvData.pv.PVStructure)
		 * 
		 * @param channel The channel that is requesting the service. 
		 * @param pvRecord The record that is being serviced.
		 * @param channelRPCRequester The client that is requesting the service. 
		 * @param pvArgument The structure for the argument data
		 * that will be passed from the client 
		 * @param bitSet The bitSet that shows which fields in pvArgument have changed
		 * value
		 * @param pvRequest The client's request structure - the agreement 
		 * between client and server.
		 */
		@Override
		public Status initialize(Channel channel, PVRecord pvRecord, 
				ChannelRPCRequester channelRPCRequester, PVStructure pvRequest)
		{
			if (DEBUG) 
				msg("intialize() entered.");

			Status status = okStatus;
			this.channelRPCRequester = channelRPCRequester;
			m_pvRequest = pvRequest;

			if (DEBUG)
				msg("intialize() leaving: status=" + status.toString());
			return status;
		}

		/**
		 * Construct and return the requested archive data.
		 */
		@Override
		public void request( PVStructure pvArguments )
		{
			// Retrieve the arguments (entity and parameters). Only the "entity"
			// argument is required for rdbService at this time. That's the
			// one that has the user's given "key" of the SQL query they want,
			// eg "swissfel:allmagnetnames". the "parameters" argument is not supplied
		    // by rdbClient. It is only in the rdbService xml record db because 
			// a this early stage in EPICS v4 it looks like a good idea to make all
			// services xml look identical, so all server code can be cloned.
			//
            m_pvEntity = pvArguments.getStringField("entity");
            if (m_pvEntity == null)
            	channelRPCRequester.requestDone(missingRequiredArgumentStatus, null);
            entity = m_pvEntity.get();

            // m_pvParameters is not used, so we don't check it.
			m_pvParameters = pvArguments.getStringField("parameters");

			// Construct the return data structure "pvTop."
			// The data structure we return here is a pre-release example
			// of the idea of normative types. This pvTop is self identifying that
			// it is a "normativeType" and that it is specifically a NTTable normative
			// type. In this way, the client side can check that it got a structure it
			// understands. The client knows to look for structures declaring themselves
			// to be normative type NTTable in their first element. The data payload
			// follows the declaration, in another PVStructure called ResultSet.
			//
			Field normativeType = fieldCreate.createScalar("normativeType", ScalarType.pvString);
			PVField[] t = new PVField[1];
			t[0] = pvDataCreate.createPVField(null, normativeType);
			PVString x = (PVString) t[0];
			x.put("NTTable");
			PVStructure pvTop = pvDataCreate.createPVStructure(null, "ResultSet", t);

			String query = entityToQuery(entity);
			if (query == null)
			{
				_dbg("No matching Entity was found for query: " + query);

				channelRPCRequester.requestDone(noMatchingQueryStatus, pvTop);
			} else
			{
				// All gone well, so, pass the pvTop introspection interface and the 
				// query string to getData, which will populate the pvTop for us with
				// the data in Oracle.
				//
				getData(query, pvTop);
				
				// Return the data from Oracle, in the pvTop, to the client.
				_dbg("pvTop = " + pvTop);
				channelRPCRequester.requestDone(okStatus, pvTop);
			}
		}

		/**
		 * Get the SQL query (probably a SELECT statement) identified by the given query name.
		 * 
		 * @param entity
		 * @return
		 */
		private String entityToQuery(String queryName)
		{
			String queryQuery = "SELECT QRY FROM EIDA_NAMES WHERE NAME = '" + queryName + "'";
			ResultSet sqlqueryResultSet = null; //
			String query = null;

			try
			{
				sqlqueryResultSet = executeQuery(queryQuery);
				ResultSetMetaData rsmd = sqlqueryResultSet.getMetaData();
				if (rsmd == null)
					throw new UnableToGetDataException(NORESULTSETMETADATA);

				// Make assumption that only 1 row is returned, so we don't waste time
				// error checking for a very rare occurrence.
				sqlqueryResultSet.beforeFirst();
				sqlqueryResultSet.next();
				query = sqlqueryResultSet.getString(QRYCOLUMNNUM);

			} catch (Exception e)
			{
				msgl(e.getMessage() + " when processing the SQL query to get the SQL query matching the entity '"
				        + queryName + "'");
			}

			finally
			{
				// Free JDBC resources.
				//
				try
				{
					if (sqlqueryResultSet != null)
					{
						// Statement stmt = sqlqueryResultSet.getStatement();
						// if ( stmt != null ) stmt.close();
						sqlqueryResultSet.close();
					}
				} catch (Exception e)
				{
					msgl("when attempting to free JDBC resources for query " + queryQuery);
				}
			}
			return query; // Return the SQL query that is identified by the
			              // given "entity".

		}

		private void getData(String query, PVStructure pvTop)
		{
			ArrayList<Field> myArr = new ArrayList<Field>();
			ResultSet rs = null;

			try
			{
				// Replace values of any passed arguments for matched arg names
				// in the query
				// String query = substituteArgs( args, query );
				rs = executeQuery(query);
				ResultSetMetaData rsmd = rs.getMetaData();
				if (rsmd == null)
					throw new UnableToGetDataException(NORESULTSETMETADATA);

				// Get number of rows in ResultSet
				rs.last();
				int rowsM = rs.getRow();

				// Get number of columns in ResultSet
				int columnsN = rsmd.getColumnCount();
				String columnName = null;
				_dbg("Num Columns = " + columnsN);

				// For each column, extract all the rows of the column from the
				// ResultSet and add the whole column to what we return. So we're
				// transposing the ResultSet where the slow moving index is row, 
				// to a PVStructure.
				//
				for (int colj = 1; colj <= columnsN; colj++)
				{
					rs.beforeFirst(); // Reset cursor to first row.
					int i = 0; // Reset row indexer.
					ScalarArray colField = null;
					columnName = rsmd.getColumnName(colj);
					_dbg("\nColumn Name = " + columnName);

					switch (rsmd.getColumnType(colj)) {
					case java.sql.Types.DECIMAL:
					case java.sql.Types.DOUBLE:
					case java.sql.Types.REAL:
					case java.sql.Types.NUMERIC:
					case java.sql.Types.FLOAT:
					{
						colField = fieldCreate.createScalarArray(columnName, ScalarType.pvDouble);
						// myArr.add(colField);
						PVDoubleArray valuesArray = (PVDoubleArray) pvDataCreate.createPVScalarArray(pvTop, colField);

						double[] coldata = new double[rowsM];
						while (rs.next())
						{
							coldata[i++] = rs.getDouble(colj);
						}
						valuesArray.put(0, rowsM, coldata, 0);
						pvTop.appendPVField(valuesArray);
						break;
					}
					case java.sql.Types.INTEGER:
					case java.sql.Types.SMALLINT:
					case java.sql.Types.BIGINT:
					{
						colField = fieldCreate.createScalarArray(columnName, ScalarType.pvInt);
						myArr.add(colField);
						PVLongArray valuesArray = (PVLongArray) pvDataCreate.createPVScalarArray(pvTop, colField);

						long[] coldata = new long[rowsM];
						while (rs.next())
						{
							coldata[i++] = rs.getLong(colj);
						}
						valuesArray.put(0, rowsM, coldata, 0);
						pvTop.appendPVField(valuesArray);
						break;
					}

					case java.sql.Types.TINYINT:
					case java.sql.Types.BIT:
					{
						colField = fieldCreate.createScalarArray(columnName, ScalarType.pvByte);
						myArr.add(colField);
						PVByteArray valuesArray = (PVByteArray) pvDataCreate.createPVScalarArray(pvTop, colField);

						byte[] coldata = new byte[rowsM];
						while (rs.next())
						{
							coldata[i++] = rs.getByte(colj);
						}
						valuesArray.put(0, rowsM, coldata, 0);
						pvTop.appendPVField(valuesArray);
						break;
					}
					case java.sql.Types.VARCHAR:
					case java.sql.Types.CHAR:
					case java.sql.Types.LONGVARCHAR:
					{
						colField = fieldCreate.createScalarArray(columnName, ScalarType.pvString);
						myArr.add(colField);
						PVStringArray valuesArray = (PVStringArray) pvDataCreate.createPVScalarArray(pvTop, colField);

						String[] coldata = new String[rowsM];
						while (rs.next())
						{
							String d = rs.getString(colj);
							coldata[i++] = (d == null || d.length() == 0) ? " " : d;
							_dbg("coldata = '" + coldata[i - 1] + "'");
						}
						valuesArray.put(0, rowsM, coldata, 0);
						pvTop.appendPVField(valuesArray);
						break;
					}
					default:
					{
						colField = fieldCreate.createScalarArray(columnName, ScalarType.pvString);
						myArr.add(colField);
						PVStringArray valuesArray = (PVStringArray) pvDataCreate.createPVScalarArray(pvTop, colField);

						String[] coldata = new String[rowsM];
						while (rs.next())
						{
							String d = rs.getString(colj);
							coldata[i++] = (d == null || d.length() == 0) ? " " : d;
							_dbg("coldata = '" + coldata[i - 1] + "'");
						}
						valuesArray.put(0, rowsM, coldata, 0);
						pvTop.appendPVField(valuesArray);
						break;
					}
					} // column type

				} // For each column
			} // try block processing ResultSet

			catch (Exception e)
			{
				msgl(e.getMessage() + "when processing SQL query ");
			} 
			finally
			{
				// Free JDBC resources. 
				//
				try
				{
					if (rs != null)
					{
						// Close and free resources of ResultSet.
						rs.close();
					}
				} catch (Exception e)
				{
					msgl("when attempting to free JDBC resources for query " + query);
				}
			}

		}

		@SuppressWarnings("unused")
		private Status parseParameters()
		{
			// There are no parameters recognized by the rdbService, so
			// always return okStatus.
			//
			return okStatus;
		}

		/**
		 * Queries the AIDA Name Server database with the query in sqlString. This is a wrapper to give appropriate
		 * error handling and retry logic.
		 * 
		 * @param sqlString
		 *            the SQL query, in "ascii" (actually UTF-16 or whatever java String is).
		 * @return The ResultSet given by stmt.executeQuery(sqlString)
		 * @version 1.0 19-Jun-2005, Greg White
		 */
		private ResultSet executeQuery(String sqlString) throws SQLException
		{
			Statement stmt = null; // The Statement on which the ResultSet is
			                       // acquired.
			ResultSet rs = null; // ResultSet receiving SQL results. NOTE:
			                     // should be closed by callers.
			int nRetries = 0;
			boolean bRetry = false;

			// Create a jdbc Statement and execute the given query. If the
			// query fails to execute for whatever reason, try to get a
			// new connection and loop, re-creating the statement and
			// re-executing the query. Try up to 3 times.
			do
			{
				try
				{
					// Create a statement with "Scrollable" ResultSet, as
					// necessary
					// for processing each column as a unit in the get method.
					stmt = m_Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					rs = stmt.executeQuery(sqlString);
					bRetry = false;
				} catch (Exception ex)
				{
					// We encountered an error in the execution of the sql
					// query,
					// so try to fix this by getting a new Oracle connection
					// and set logic so we'll go through the do loop again.
					if (nRetries < MAX_RETRIES)
					{
						msgl(ex.getMessage() + " when executing SQL query - " + "retrying with new java.sql.Connection");
						getConnection();
						bRetry = true;
						nRetries++;
					} else
					{
						bRetry = false;
						String suppl = "when executing SQL query " + sqlString;
						if (ex.getClass().getName() == "java.sql.SQLException")
							suppl.concat(": " + ((SQLException) ex).getSQLState());
						msgl(ex.getMessage() + suppl);
					}
				}
			} while (bRetry);

			if (rs != null && nRetries < MAX_RETRIES)
				return rs;
			else
			{
				msgl("Unable to execute query");
				throw (SQLException) new SQLException("Unable to execute query");
			}
		}

		void _dbg(String debug_message)
		{
			if (DEBUG)
				System.err.println(debug_message);
		}

	} // PerfTestServiceFactory
}
