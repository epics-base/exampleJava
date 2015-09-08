/**
 * rdbService defines classes for the server side of an EPICS V4 service for accessing
 * a relational database, such as ORACLE.
 */
package services.rdbService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.*;

/**
 * RdbServiceConnection implements JDBC connection logic.
 *
 * @version 08-Sep-2015, Greg White, move introspection interface into getData method per new V4 API;
 *          and correct indentation (bsd style)
 */
public class RdbServiceConnection
{
	private static final Logger logger = Logger.getLogger(RdbServiceConnection.class.getName());
	
	// TODO: Externalize and protect access username/passwords and strings
	// TODO: Convert connection string to java Property.

	// Oracle JDBC connection URI and ID stuff.
	//
	private static volatile Connection m_Conn = null; // JDBC connection for queries
	private static final String CONNECTION_URI_DEFAULT = 
		"jdbc:oracle:thin:@yourdbs.host.name:1521:YOURDBNAME";
	private static final String CONNECTION_USERID_DEFAULT = "eida";
	private static final String CONNECTION_PWD_DEFAULT = "nicetry";
	private static final String NORESULTSETMETADATA = 
		"No ResultSet metadata available, so can not continue to get data";
	private static final int MAX_RETRIES = 2; // Try a SQL query at most 2 times
	                                          // before reinit and requery.

	// Index of the column of eida.eida_names that contains the query string.
	private static final int QRYCOLUMNNUM = 1; 

	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

	/**
	 * Initialize for an acquisition.
	 * 
	 * Note: we use a pattern where the initialization is done on
	 * server startup, and the important part (getConnection) can be redone at any time if the connection
	 * to the backend rdb goes bad. 
	 */
	RdbServiceConnection()
	{
		init();
	}

	/**
	 * Init loads JDBC and initializes connection to the db, Oracle in this case.
	 */
	private void init()
	{
		// Load JDBC.
		try
		{
			logger.info("Loading Oracle database runtime connection...");
			Class.forName("oracle.jdbc.OracleDriver");
		}
		catch (Throwable ex)
		{
			throw new RuntimeException("Failed to register driver for connection to Oracle.", ex);
		}
		
		// Establish connection to the Oracle instance defined in private members.
		getConnection();
	}

	/**
	 * Initializes a database connection to the Oracle Database.
	 * 
	 * If the connection is non-null, then the old connection is first closed. This part is include so that this
	 * routine can be used to renew a stale connection.
	 */
	private synchronized void getConnection()
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
			logger.info("Initializing database connection: " + connectionString);
			m_Conn = DriverManager.getConnection(connectionString,
			        System.getProperty("CONNECTION_USERID_PROPERTY", CONNECTION_USERID_DEFAULT), pwd); 
	
		}
		catch (Throwable ex)
		{
			logger.log(Level.SEVERE, "Failed to initialize connection to database.", ex);
		}
		
		if (m_Conn != null)
			logger.info("Oracle database connection completed successfully.");
		else
			logger.info("Unable to establish connection to RDB with URI " + CONNECTION_URI_DEFAULT);
	
	}

	/**
	 * Disposes of existing Db connection.
	 */
	private synchronized void closeConnection()
	{
		// If we have a connection, dispose of it.
		try
		{
			if (m_Conn != null)
			{
				if (!m_Conn.isClosed())
				{
					logger.config("Closing connection to database...");
					m_Conn.close();
					m_Conn = null;
				}
			}
		}
		catch (Throwable ex)
		{
			logger.log(Level.SEVERE, "Failed to close connection to database.", ex);
		}
	}

	/**
	 * Get the SQL query (probably a SELECT statement) identified by the given query name, as
	 * it is given in the data source to which teh server connects (probably a releational database).
	 * 
	 * @param queryName identifier of the SQL query; what the end user entered
	 * @return the SQL select statement that corresponds to the queryName input parameter  
	 */
	public String entityToQuery(String queryName) throws UnableToGetDataException
	{
		String queryQuery = "SELECT QRY FROM EIDA_NAMES WHERE NAME = '" + queryName + "'";
		ResultSet sqlqueryResultSet = null;

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
			return sqlqueryResultSet.getString(QRYCOLUMNNUM);

		}
		catch (Throwable e)
		{
			throw new UnableToGetDataException(
					"Failed to process the SQL query to get the SQL query matching the entity '"
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
			}
			catch (Throwable e)
			{
				logger.log(Level.SEVERE, "Failed to free JDBC resources for query '" + queryQuery + "'.", e);
			}
		}
	}

	public PVStructure getData(String query) throws UnableToGetDataException
	{
		ResultSet rs = null;
		PVStructure pvTop = null;
		try
		{
			// Replace values of any passed arguments for matched arg names
			// in the query
			rs = executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rsmd == null)
				throw new UnableToGetDataException(NORESULTSETMETADATA);

			// Get number of rows in ResultSet
			rs.last();
			int rowsM = rs.getRow();

			// Get number of columns in ResultSet
			int columnsN = rsmd.getColumnCount();
			String[] columnNames = new String[columnsN];
			Field[] fields = new Field[columnsN];
			logger.finer("Num Columns = " + columnsN);

			// Construct the pvData introspection interface of a Structure that can
			// represent the table returned by the database query. The introspection
			// interface is like a dynamically constructed structured variable.
			// After we've created the introspection interface (the structure),
			// we'll populate it (see below).
			//
			for (int colj = 1; colj <= columnsN; colj++)
			{
				rs.beforeFirst(); // Reset cursor to first row.
				columnNames[colj-1] = rsmd.getColumnName(colj);
				logger.finer("Column Name = " + columnNames[colj-1]);

				switch (rsmd.getColumnType(colj)) {
				case java.sql.Types.DECIMAL:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.REAL:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.FLOAT:
				{
					fields[colj] = fieldCreate.createScalarArray(ScalarType.pvDouble);
					break;
				}
				case java.sql.Types.INTEGER:
				case java.sql.Types.SMALLINT:
				case java.sql.Types.BIGINT:
				{
					fields[colj] = fieldCreate.createScalarArray(ScalarType.pvInt);
					break;
				}

				case java.sql.Types.TINYINT:
				case java.sql.Types.BIT:
				{
					fields[colj] = fieldCreate.createScalarArray(ScalarType.pvByte);
					break;
				}
				case java.sql.Types.VARCHAR:
				case java.sql.Types.CHAR:
				case java.sql.Types.LONGVARCHAR:
				{
					fields[colj] = fieldCreate.createScalarArray(ScalarType.pvString);
					break;
				}
				default:
				{
					fields[colj] = fieldCreate.createScalarArray(ScalarType.pvString);
					break;
				}
				} // column type

			} // For each column
			// Now put the individual column introspection interfaces together into a structure
			// that conforms to an EPICS V4 NTTable.
			String[] topNames = new String[2];
			Field[] topFields = new Field[2];
			topNames[0] = "labels";
			topNames[1] = "value";
			topFields[0] = fieldCreate.createScalarArray(ScalarType.pvString);
			topFields[1] = fieldCreate.createStructure(columnNames,fields);
			Structure top = fieldCreate.createStructure("epics:nt/NTTable:1.0", topNames, topFields);

			// Populate the structure created above from the data found in the database.
			//
			pvTop = pvDataCreate.createPVStructure(top);
			PVStructure pvValue = pvTop.getStructureField("value");
			PVStringArray labelsArray = (PVStringArray) 
				pvTop.getScalarArrayField("labels",ScalarType.pvString);
			labelsArray.put(0, columnNames.length, columnNames, 0);
			PVField[] pvFields = pvValue.getPVFields();
			for (int colj = 1; colj <= columnsN; colj++)
			{
				rs.beforeFirst(); // Reset cursor to first row.
				int i = 0; // Reset row indexer
				columnNames[colj-1] = rsmd.getColumnName(colj);
				logger.finer("Column Name = " + columnNames[colj-1]);

				switch (rsmd.getColumnType(colj)) {
				case java.sql.Types.DECIMAL:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.REAL:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.FLOAT:
				{
					PVDoubleArray valuesArray = (PVDoubleArray)pvFields[colj];
					double[] coldata = new double[rowsM];
					while (rs.next())
					{
						coldata[i++] = rs.getDouble(colj);
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				case java.sql.Types.INTEGER:
				case java.sql.Types.SMALLINT:
				case java.sql.Types.BIGINT:
				{
					PVLongArray valuesArray = (PVLongArray)pvFields[colj];
                    
					long[] coldata = new long[rowsM];
					while (rs.next())
					{
						coldata[i++] = rs.getLong(colj);
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}

				case java.sql.Types.TINYINT:
				case java.sql.Types.BIT:
				{
					PVByteArray valuesArray = (PVByteArray)pvFields[colj];
             
					byte[] coldata = new byte[rowsM];
					while (rs.next())
					{
						coldata[i++] = rs.getByte(colj);
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				case java.sql.Types.VARCHAR:
				case java.sql.Types.CHAR:
				case java.sql.Types.LONGVARCHAR:
				{
					
					PVStringArray valuesArray = (PVStringArray)pvFields[colj];
                    
					String[] coldata = new String[rowsM];
					while (rs.next())
					{
						String d = rs.getString(colj);
						coldata[i++] = (d == null || d.length() == 0) ? " " : d;
						logger.finer("coldata = '" + coldata[i - 1] + "'");
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				default:
				{
					PVStringArray valuesArray = (PVStringArray)pvFields[colj];
					
					String[] coldata = new String[rowsM];
					while (rs.next())
					{
						String d = rs.getString(colj);
						coldata[i++] = (d == null || d.length() == 0) ? " " : d;
						logger.finer("coldata = '" + coldata[i - 1] + "'");
					}
					valuesArray.put(0, rowsM, coldata, 0);
					break;
				}
				} // column type

			} // For each column
						
		} // try block processing ResultSet

		catch (SQLException e)
		{
			throw new UnableToGetDataException("Failed to process SQL query: " + query, e);
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
			} catch (Throwable e)
			{
				logger.log(Level.SEVERE, "Failed to free JDBC resources for query: " + query, e);
			}
		}
		return pvTop;
	}

	/**
	 * Queries the database with the query in sqlString. This is a wrapper to give appropriate
	 * error handling and retry logic.
	 * 
	 * @param sqlString
	 *            the SQL query, in "ascii" (actually UTF-16 or whatever java String is).
	 * @return The ResultSet given by stmt.executeQuery(sqlString)
	 * @version 1.0 19-Oct-2011, Greg White
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
			}
			catch (Throwable ex)
			{
				// We encountered an error in the execution of the sql
				// query, so try to fix this by getting a new Oracle connection
				// and set logic so we'll go through the do loop again.
				if (nRetries < MAX_RETRIES)
				{
					logger.log(Level.WARNING, "Failed to execute SQL query, retrying with new java.sql.Connection.", ex);
					getConnection();
					bRetry = true;
					nRetries++;
				}
				else
				{
					bRetry = false;
					String suppl = "Failed to execute SQL query " + sqlString;
					if (ex instanceof SQLException)
						suppl.concat(": " + ((SQLException) ex).getSQLState());
					logger.log(Level.SEVERE, suppl, ex);
				}
			}
		} while (bRetry);

		if (rs != null && nRetries < MAX_RETRIES)
			return rs;
		else
			throw new SQLException("Unable to execute query.");
	}
	
}
