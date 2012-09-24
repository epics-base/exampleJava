/**
 * rdbService defines classes for the server side of an EPICS V4 service for accessing
 * a relational database, such as ORACLE.
 */
package rdbService;

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
import org.epics.pvdata.pv.ScalarType;

/**
 * RdbServiceConnection implements JDBC connection logic.
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
		"jdbc:oracle:thin:@gfadb05s.psi.ch:1521:GFAPRD";
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
	 * Get the SQL query (probably a SELECT statement) identified by the given query name.
	 * 
	 * @param entity
	 * @return
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

	public void getData(String query, PVStructure pvTop) throws UnableToGetDataException
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
			String[] columnNames = new String[columnsN];
			PVField[] pvFields = new PVField[columnsN];
			logger.finer("Num Columns = " + columnsN);

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
				columnNames[colj-1] = rsmd.getColumnName(colj);
				logger.finer("Column Name = " + columnNames[colj-1]);

				switch (rsmd.getColumnType(colj)) {
				case java.sql.Types.DECIMAL:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.REAL:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.FLOAT:
				{
					colField = fieldCreate.createScalarArray(ScalarType.pvDouble);
					PVDoubleArray valuesArray = (PVDoubleArray) pvDataCreate.createPVScalarArray(colField);
					pvFields[colj-1] = valuesArray;
					
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
					colField = fieldCreate.createScalarArray(ScalarType.pvInt);
					myArr.add(colField);
					PVLongArray valuesArray = (PVLongArray) pvDataCreate.createPVScalarArray(colField);
                    pvFields[colj-1] = valuesArray;
                    
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
					colField = fieldCreate.createScalarArray(ScalarType.pvByte);
					myArr.add(colField);
					PVByteArray valuesArray = (PVByteArray) pvDataCreate.createPVScalarArray(colField);
                    pvFields[colj-1] = valuesArray;
                    
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
					colField = fieldCreate.createScalarArray(ScalarType.pvString);
					myArr.add(colField);
					PVStringArray valuesArray = (PVStringArray) pvDataCreate.createPVScalarArray(colField);
                    pvFields[colj-1] = valuesArray;
                    
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
					colField = fieldCreate.createScalarArray(ScalarType.pvString);
					myArr.add(colField);
					PVStringArray valuesArray = (PVStringArray) pvDataCreate.createPVScalarArray(colField);
                    pvFields[colj-1] = valuesArray;
                    
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
			
			// Append all the fields we created for each column, to the top level structure to be returned.
			pvTop.appendPVFields(columnNames, pvFields);
			
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
			}
			catch (Throwable ex)
			{
				// We encountered an error in the execution of the sql
				// query,
				// so try to fix this by getting a new Oracle connection
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
