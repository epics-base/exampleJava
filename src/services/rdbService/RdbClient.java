/** -*-java-*-
 * RdbClient is a simple client of the rdbService (an example EPICS V4 service).
 */
package services.rdbService;

import java.util.logging.Level;
import java.util.logging.Logger;   // Prerequisite of ConsoleLogHandler pvAccess utility.

import org.epics.pvaccess.util.logging.ConsoleLogHandler;    // Logging.
import org.epics.pvaccess.ClientFactory;                     // Client side constructor.
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;    // Standard EPICS service exception.
import org.epics.pvdata.factory.FieldFactory;                // 
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.util.namedValues.NamedValues;
import org.epics.pvdata.util.namedValues.NamedValuesFormatter;
import org.epics.pvdata.util.pvDataHelper.GetHelper;

/**
 * RdbClient is a simple client of the rdbService; the rdbService is
 * intended to help get data out of relational databases like Oracle, mySQL, 
 * via EPICS V4. 
 * 
 * @author Greg White (greg@slac.stanford.edu) 22-Sep-2011
 * @version Greg White (greg@slac.stanford.edu) 15-Jan-2013 Updated for 
 * conformance to NTTable. 
 *
 */
public class RdbClient 
{
	// Instantiate the Java logging API.  
	private static final Logger logger = Logger.getLogger(RdbClient.class.getName());
	
    // The rdbService responds to a single channel name. That's not a requirement of
	// EPICS V4 services, it just makes it easier when the server doesn't "know"
	// at the time of implementation which names for which it will be called on to get data.
	private static final String CHANNEL_NAME = "rdbService";  // Name of service to contact.

	// The rdbClient expects to get returned in a PVStructure which conforms to the 
	// definition of the NTTable Normative Type. See EPICS V4 Normative Types for definition. 
	private static String NTNAMESPACE="epics:nt";
	private static String NTTABLE_TYPE_NAME = NTNAMESPACE + "/" + "NTTable:1.0";
	
	// Error exit codes
	@SuppressWarnings("unused")
	private static final int NOTNORMATIVETYPE = 1;
	private static final int NOTNTTABLETYPE = 2;
	private static final int NODATARETURNED = 3;
	private static final int NOARGS = 4;

	private static final int _STYLE_ARGN = 1;                   // The index of the row/col arg if given.
	private static final String _STYLE_ROW = "row";             // Arg _STYLE_ARGN must be this for row printout.
	private static final String _STYLE_COL = "col";             // Arg _STYLE_ARGN, if given, = this for column printout.
	private static final int _LABELS_ARGN = 2;                  // The index of the labels wanted arg. 
	                                                            // If given STYLE must be given also.
	private static final String _LABELS_WANTED = "labels";      // Arg _LABELS_ARGN must be this for headings. 
	private static final String _LABELS_NOTWANTED = "nolabels"; // Arg _LABELS_ARGN, if given, 
	                                                            // must be this to suppress headings.
	                
	
	private static int _style = NamedValuesFormatter.STYLE_COLUMNS;
	private static boolean _labels_wanted = true;
	
	private final static double TIMEOUT_SEC = 5.0;
	
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

	private final static Structure queryStructure = 
		fieldCreate.createStructure(
			new String[] {"rdbqueryname"},
			new Field[] { fieldCreate.createScalar(ScalarType.pvString)});
	private final static Structure uriStructure =
		fieldCreate.createStructure("epics:nt/NTURI:1.0",
			new String[] { "scheme", "query" },
			new Field[] { fieldCreate.createScalar(ScalarType.pvString), queryStructure } );

	
	/**
	 * Main of rdbService client takes 1 argument, being the name of a SQL query that the
	 * server side will understand.
	 *  
	 * @param args args[0] must be the name of a query that the server will understand; 
	 * effectively the "key" of a SQL SELECT statement, in a lookup table mapping keys to 
	 * SELECT statements maintained by the server. Eg "swissFEL:allQuads". 
	 */
	public static void main(String[] args) 
	{	
		// Initialize Console logging, and set it to log only messages at WARNING or above.
		// To turn on debugging messages, set this to INFO.
		ConsoleLogHandler.defaultConsoleLogging(Level.WARNING);
		
		// Update arguments to the service with what we got at the command line, 
		// like "swissfel:allmagnetnames"
		if ( args.length <= 0 )
		{
			logger.log(Level.SEVERE, "No name of a db query was given; exiting.");
			System.exit(NOARGS);
		}
		parseArguments( args );
		
		
		// Start pvAccess and instantiate RPC client of the rdb service's single V4 channel.
		// This form creates a synchronous EPICS V4 pvAccess RPC client; see pvAccess, in 
		// particular the the test folder org.epics.pvaccess.client.rpc.test package, for an
		// example of an asynchronous client.
		//
		ClientFactory.start();
		RPCClientImpl client = new RPCClientImpl(CHANNEL_NAME);
		
		// Retrieve introspection interface of the service's input argument structure, 
		// i.e. the API for setting the arguments of the service. It is a PVStructure 
		// conforming to the NTURI Normative Type. Retrieve the rdbqueryname string of the
		// query part of the uri, and set it's value to the first argument to this executable, ie
		// the name of the rdb query the rdbService should run against it's SQL database.
		//
	    PVStructure pvRequest = PVDataFactory.getPVDataCreate().createPVStructure(uriStructure);
	    pvRequest.getStringField("scheme").put("pva");
	    PVStructure pvQuery = pvRequest.getStructureField("query");
		pvQuery.getStringField("rdbqueryname").put(args[0]);
    
		logger.log(Level.INFO, "Following client connect the arguments to service will be = "+
				pvRequest.toString());

	
		// Execute the service request for data subject to the arguments constructed above. 
		//
		PVStructure pvResult = null;
		try
		{
			// Actual wire i/o to get the data from the server
			pvResult = client.request( pvRequest, TIMEOUT_SEC );
			
			// Verify result validity, unpack and display it.
			if (pvResult != null)
			{
				// Check the result PVStructure is of the type we expect. It should be an NTTABLE.
				String type = pvResult.getStructure().getID();
				if (!type.equals(NTTABLE_TYPE_NAME))	
				{
					logger.log(Level.SEVERE, "Unable to get data: unexpected data structure returned from "+
							CHANNEL_NAME + "; \nexpected returned data id member value "+NTTABLE_TYPE_NAME+
							" but found type = " + type);
					System.exit(NOTNTTABLETYPE);
				}

				/*
				 * OK, so we know we got back an NTTable. Now we have to decode it
				 * and display it as a table. The datum pvResult that came back from
				 * the rdbService should be of EPICS V4 normative type NTTable. That
				 * is, pvResult is a PVStructure whose specific shape conforms to
				 * the definition NTTable
				 * (http://epics-pvdata.sourceforge.net/normative_types_specification
				 * .html). NTTable is composed of two fields; a string array field of
				 * column titles, and a structure containing N array fields each being
				 * one column of data. Further, we know that
				 * rdbService only returns columns of type PVDoubleArray,
				 * PVLongArray, PVByteArray, or PVStringArray. We use the
				 * introspection interface of PVStructure to determine which.
				 */	
				
				PVStringArray pvColumnTitles = (PVStringArray) 
						pvResult.getScalarArrayField("labels", ScalarType.pvString);
				PVStructure pvTableData = pvResult.getStructureField("value");
				int Ncolumns = pvTableData.getNumberFields();
					
				PVField[] pvColumns = pvTableData.getPVFields();
				if ( Ncolumns <= 0 || pvColumns.length <= 0)
				{
					logger.log(Level.SEVERE, "No data fields returned from " + CHANNEL_NAME + ".");
					System.exit(NODATARETURNED);
				}

				// To print the contents of the NTTable conforming PVstructure, make a 
				// NamedValues system from its data. A NamedValues object allows us to 
				// personify the table as 2 arrays: a vector <String> of column headings, 
				// and a 2-d array <String> of values - the table of data. Then use the formatting
				// provisions of NamedValues to print that data in a familiar looking table
				// format.
				//
				StringArrayData data = new StringArrayData();
				int labelOffset=0;
				NamedValues namedValues = new NamedValues();
				for (PVField pvColumnIterator : pvColumns)
				{
					// Get the column name. This will be the
					// column name from the ResultSet of the SQL SELECT query.
					// The column name should be taken from the returned NTTable labels
					// field, as opposed to the names of the value structure fields, though the
					// field names of the value structure are, in the case of rdbService
					// exactly the same values as the labels field.
					pvColumnTitles.get(labelOffset,1,data);
					String fieldName = data.data[labelOffset++];
					/* pvColumnIterator.getFieldName(); - could have been used instead */
					
					if (pvColumnIterator.getField().getType() == Type.scalarArray)
					{
						ScalarArray scalarArray = (ScalarArray) pvColumnIterator
								.getField();
						if (scalarArray.getElementType() == ScalarType.pvDouble)
						{
							PVDoubleArray pvDoubleArray = (PVDoubleArray) pvColumnIterator;
							namedValues.add(fieldName,
									GetHelper.getDoubleVector(pvDoubleArray));
						} else if (scalarArray.getElementType() == ScalarType.pvLong)
						{
							PVLongArray pvLongArray = (PVLongArray) pvColumnIterator;
							namedValues.add(fieldName,
									GetHelper.getLongVector(pvLongArray));
						} else if (scalarArray.getElementType() == ScalarType.pvByte)
						{
							PVByteArray pvByteArray = (PVByteArray) pvColumnIterator;
							namedValues.add(fieldName,
									GetHelper.getByteVector(pvByteArray));
						} else if (scalarArray.getElementType() == ScalarType.pvString)
						{
							PVStringArray pvStringArray = (PVStringArray) pvColumnIterator;
							namedValues.add(fieldName,
									GetHelper.getStringVector(pvStringArray));
						} else
						{
							// Array types other than those above are not understood by this client. Note that
							// conformance to NTTable does NOT REQUIRE that all possible terminals
							// of a non-terminal N-type field are used by every client, only that every
							// client using a N-tpype SHOULD handle every possible implied design of that N-type.
							// Furthermore, handling specifically includes trapping the condition that the
							// implied design is not semantically meaningful to the client.
							//
							logger.log( Level.SEVERE, "Unexpected array type returned from "+ CHANNEL_NAME
									+ "; only pvData scalarArray types pvDouble, pvLong, pvByte or pvString expected");
						}
					} else
					{
						logger.log( Level.SEVERE, "Unexpected non-array field returned from "+ CHANNEL_NAME
								+ ".\n Field named \'"+ fieldName + "\' is not of scalarArray type, "
								+ "and so can not be interpretted as a data column.");
					}
				}	
			
				// Format the NTTable we got from the server as a familiar looking table.
				// To do so, set up a printout formatter for NamedValues give it our
				// constructed namedValues (the data that came back from the server. Then
				// ask the formatter to print it to System.out.
				//
				NamedValuesFormatter formatter = NamedValuesFormatter.create(_style);
				formatter.setWhetherDisplayLabels(_labels_wanted);
				formatter.assignNamedValues(namedValues);
				formatter.display(System.out);
			}
			logger.log(Level.INFO, pvResult.toString());
		
		}
		catch (RPCRequestException rre)	
		{
			logger.log(Level.SEVERE, "Acquisition of "+args[0]+" query was not successful.\n"+
					"Channel " +CHANNEL_NAME+ " connected but issued exception. Check validity and spelling:", rre);
		}
		catch (IllegalStateException rre)	
		{
			logger.log(Level.SEVERE, "Acquisition not successful, failed to connect to "+CHANNEL_NAME, rre);
		}
		finally
		{
			// Clean up
			client.destroy();
			ClientFactory.stop();
		}
		
		logger.log(Level.INFO, "Completed Successfully");
		System.exit(0);
	}

	
	/**
	 * Parses command line arguments to see what we're going to get and how to print it.
	 * @param args - the command line arguments
	 */
	private static void parseArguments( String[] args )
	{
		// Create a formatter for the namedValues system constructed above. This 
		// will be used to print the system as a familiar looking table, unless the style arg says 
		// print it as rows, in which case you'll get a list of row data. Column style
		// is better for >1 value per name. Row style is good for 1 value per name.
		//
		if ( args[_STYLE_ARGN] != null ) 
		{
			if ( _STYLE_ROW.equals(args[_STYLE_ARGN]) ) 
				_style = NamedValuesFormatter.STYLE_ROWS;
			else if ( _STYLE_COL.equals(args[_STYLE_ARGN]) ) 
				_style = NamedValuesFormatter.STYLE_COLUMNS;
			else
				System.err.println( "Unexpected value of style argument; it must be given as " + _STYLE_ROW + 
						" or "+ _STYLE_COL );
		}
		

		if ( args[_STYLE_ARGN] != null && args[_LABELS_ARGN] != null )
		{
			if ( _LABELS_WANTED.equals(args[_LABELS_ARGN]) )
				_labels_wanted = true;
			else if ( _LABELS_NOTWANTED.equals(args[_LABELS_ARGN]) )
				_labels_wanted = false;
			else
				System.err.println( "Unexpected value of labels argument; it must be given as " + _LABELS_WANTED + 
						" or "+ _LABELS_NOTWANTED );
		}
	}
	
} // end class RdbClient
