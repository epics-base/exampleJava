/** -*-java-*-
 * RdbClient is a simple client of the rdbService (an example EPICS V4 service).
 */
package rdbService;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.rpc.RPCClient;
import org.epics.pvaccess.client.rpc.RPCClientFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;

import rdbService.namedValues.NamedValues;
import rdbService.namedValues.NamedValuesFormatter;
import rdbService.pvDataHelper.GetHelper;

/**
 * RdbClient is a simple client of the rdbService; the rdbService is
 * intended to help get data out of relational databases like Oracle, mySQL, 
 * via EPICS V4. 
 * 
 * @author Greg White (greg@slac.stanford.edu) 22-Sep-2011
 *
 */
public class RdbClient 
{
	// Issue application level debugging messages if true.
	// TODO remove this C concept, use Java Logging API instead
	private static final boolean DEBUG = false;
	
	private static final String OBJECTIVE_SERVICE_NAME = "rdbService";  // Name of service to contact.
	
	                                                               // Name of field holding
	                                                               // arguments in the service's interface xml db.
	private static final String ENTITY_ARGNAME = "entity";         // Name of argument holding what the user asked for.   
	private static final String PARAMS_ARGNAME = "parameters";     // Name of an argument supplied, but not used by
	                                                               // the rdb service. 
	
	// rdbClient expects to get returned a PVStructure which conforms to the 
	// definition of a NTTable. 
	private static String NTTABLE_TYPE_NAME = "NTTable";
	
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

	private final static Structure requestStructure =
    	fieldCreate.createStructure(
				new String[] { 
						ENTITY_ARGNAME, 
						PARAMS_ARGNAME 
						},
				new Field[] { 
						fieldCreate.createScalar(ScalarType.pvString),
						fieldCreate.createScalar(ScalarType.pvString)
						}
				);
	
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
		// Update arguments to the service with what we got at the command line, like "swissfel:allmagnetnames"
		if ( args.length <= 0 )
		{
			System.err.println("No name of a db query was given; exiting.");
			System.exit(NOARGS);
		}

		parseArguments( args );
		
		
		// Start pvAccess and instantiate RPC client.
		ClientFactory.start();
		RPCClient client = RPCClientFactory.create(OBJECTIVE_SERVICE_NAME);
		
	    PVStructure pvArguments = PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
	    
		_dbg("DEBUG: main(): following client connect, pvarguments = "+
				pvArguments.toString());

		// Retrieve interface (i.e., an API for setting the arguments of the service)
		PVString pvQuery = pvArguments.getStringField(ENTITY_ARGNAME);
		@SuppressWarnings("unused") // The parameters field of the input is not yet used by rdbService.
        PVString pvParams = pvArguments.getStringField(PARAMS_ARGNAME);

		pvQuery.put(args[0]);
		
		// TODO: There are presently no record parameters supported (ie, parameters 
		// member of the rdbService xml record is not used). This would be used if for instance 
		// supporting SQL "define" parameter replacement (eg &1,&2 etc), or any other
		// runtime modification you wanted to make to the way the RDB query is formed or processed.	

		// Execute the service request for data subject to the arguments constructed above. 
		//
		PVStructure pvResult = null;
		try
		{
			pvResult = client.request( pvArguments, TIMEOUT_SEC );
		}
		catch ( Exception ex )
		{
			// TODO see HelloClient for better error handling
			if ( ex.getMessage() != null ) System.err.println(ex.getMessage());
		    System.exit(NODATARETURNED);	
		}
		
		// Print the results if we have any.
		//
		if (pvResult == null)
		{
			System.err.println("Internal Error in "+OBJECTIVE_SERVICE_NAME+
					". Server returned null top level result but no Exception");
		    System.exit(NODATARETURNED);	
		}
		else
		{
			String type = pvResult.getStructure().getID();
			if (!type.equals(NTTABLE_TYPE_NAME))	
			{
				System.err.println("Unable to get data: unexpected data structure returned from "+
						OBJECTIVE_SERVICE_NAME + ".");
				System.err.println("Expected normativetype member value "+NTTABLE_TYPE_NAME+
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
			 * .html). Therefore it can be unpacked assuming such structure (a
			 * PVStructure containing a PVStructure containing a number of
			 * arrays of potentially different type). Further, we know that
			 * rdbService only returns columns of type PVDoubleArray,
			 * PVLongArray, PVByteArray, or PVStringArray. We use the
			 * introspection interface of PVStructure to determine which of
			 * these types is each <array> member of the NTTable.
			 */
			
			int N_dataFields = pvResult.getNumberFields();
			
			PVField[] pvFields = pvResult.getPVFields();
			if ( N_dataFields <= 0 || pvFields.length <= 0)
			{
				System.err.println("No data fields returned from " + OBJECTIVE_SERVICE_NAME + ".");
				System.exit(NODATARETURNED);
			}
			
			// To print the contents of the NTTable PVstructure, make a NamedValues system
			// from its data. A NamedValues object allows us to personify the table
			// as 2 vectors: a 1-D vector <String> of column headings, and a 2-d vector <String>
			// of values - the table of data. Then use the formatting provisions of 
			// NamedValues to print that data in a familiar looking table format. 
			//
			NamedValues namedValues = new NamedValues();
			for ( PVField pvFielde : pvFields)
			{
				// Get the label attached to the field. This will be the column name from the ResultSet
				// of the SQL SELECT query.
				String fieldName = pvFielde.getFieldName();
				
				if ( pvFielde.getField().getType() == Type.scalarArray ) 
				{
					ScalarArray scalarArray = (ScalarArray)pvFielde.getField();
					if (scalarArray.getElementType() == ScalarType.pvDouble) 
					{
						PVDoubleArray pvDoubleArray = (PVDoubleArray)pvFielde;
						namedValues.add(fieldName, GetHelper.getDoubleVector(pvDoubleArray));
					}   
					else if (scalarArray.getElementType() == ScalarType.pvLong) 
					{
						PVLongArray pvLongArray = (PVLongArray)pvFielde;
						namedValues.add(fieldName, GetHelper.getLongVector(pvLongArray));
					}   
					else if (scalarArray.getElementType() == ScalarType.pvByte) 
					{
						PVByteArray pvByteArray = (PVByteArray)pvFielde;
						namedValues.add(fieldName, GetHelper.getByteVector(pvByteArray) );
					}   
					else if (scalarArray.getElementType() == ScalarType.pvString) 
					{
						PVStringArray pvStringArray = (PVStringArray)pvFielde;
						namedValues.add(fieldName, GetHelper.getStringVector(pvStringArray));
					}   
					else
					{
						System.err.println("Unexpected array type returned from "+OBJECTIVE_SERVICE_NAME + ".");
						System.err.println("Only pvData scalarArray types pvDouble, pvLong, pvByte or pvString expected");
					}
				}
				else
				{
					System.err.println("Unexpected non-array field returned from "+OBJECTIVE_SERVICE_NAME + ".");
					System.err.println("Field named "+fieldName+" is not of scalarArray type, " +
							"and so can not be interpretted as a data column.");
				}
			}
			
	
			// Set up a printout formatter for our NamedValues system, and give it our 
			// constructed namedValues (the data that came 
			// back from the server, but recast from a PVStructure to a NamedValues). Then
			// ask the formatter to print it - result is a table printed to System.out.
			//
			NamedValuesFormatter formatter =
				NamedValuesFormatter.create (_style);
			formatter.setWhetherDisplayLabels(_labels_wanted);
			formatter.assignNamedValues (namedValues);
			formatter.display (System.out);
	    }
		_dbg(pvResult.toString());

        // Clean up
        client.destroy();
		ClientFactory.stop();
		
		_dbg("DEBUG: main(): Completed Successfully");
		System.exit(0);
	}

	
	
	
	/**
	 * parse command line arguments to see what we're going to get and how to print it.
	 * @param args the command line arguments
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
	
	/**
	 * Just a programming utility for debugging; if static DEBUG is true, then the
	 * argument will be printed to stderr, and not otherwise.
	 * 
	 * Helps you put debug messages in code in a simple way.
	 * 
	 * @param debug_message
	 */
	// TODO remove this C concept, use Java Logging API instead
	private static void _dbg(String debug_message)
	{
		if (DEBUG)
			System.err.println("DEBUG: "+debug_message);
	}

} // end class RdbClient
