/** -*-java-*-
 * RdbClient is a simple client of the rdbService (an example EPICS V4 service).
 */
package rdbService;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import java.lang.RuntimeException;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.*;
import org.epics.rpc.ServiceClient;
import org.epics.rpc.ServiceClientFactory;
import org.epics.rpc.ServiceClientRequester;

import rdbService.namedValues.*;                         // Table manipulation and printing 
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
	private static final boolean DEBUG = false;
	
	// Set an identifying name for your client, used by the service. Does not have to
	// be unique in an installation, just a free format string.
	private static final String CLIENT_NAME = "RDB client";
	
	// These literals must agree with the XML Database of the service in question. In this
	// case perfTestService. See perfTestService.xml.
	private static final String OBJECTIVE_SERVICE_NAME = "rdbService";  // Name of service to contact.
	private static final String SERVICE_ARGUMENTS_FIELDNAME = "arguments"; 
	                                                               // Name of field holding
	                                                               // arguments in the service's interface xml db.
	private static final String ENTITY_ARGNAME = "entity";         // Name of argument holding what the user asked for.   
	private static final String PARAMS_ARGNAME = "parameters";     // Name of an argument supplied, but not used by
	                                                               // the rdb service. 
	
	// rdbClient expects to get returned a PVStructure which conforms to the 
	// definition of a NTTable. As such, the PVStructure's first field should 
	// be called "normativeType" and have value "NTTable".
	private static String TYPE_FIELD_NAME = "normativeType";
	private static String NTTABLE_TYPE_NAME = "NTTable";
	
	// Error exit codes
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
		PVStructure pvResult=null;  // The data that will come back from the server.

		parseArguments( args );
		
		// Start PVAccess. Instantiate private class that handles callbacks and look for arguments
		org.epics.ca.ClientFactory.start();
		Client client = new Client();
		
		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	    FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	    Field[] fields = new Field[2];
	    fields[0] = fieldCreate.createScalar(ENTITY_ARGNAME,ScalarType.pvString);
	    fields[1] = fieldCreate.createScalar(PARAMS_ARGNAME,ScalarType.pvString);
	    PVStructure pvArguments = pvDataCreate.createPVStructure(null, SERVICE_ARGUMENTS_FIELDNAME, fields);
	    
		// Make connection to service
		client.connect(OBJECTIVE_SERVICE_NAME);

		_dbg("DEBUG: main(): following client connect, pvarguments = "+
				pvArguments.toString());

		// Retrieve interface (i.e., an API for setting the arguments of the service)
		//
		PVString pvQuery = pvArguments.getStringField(ENTITY_ARGNAME);
		@SuppressWarnings("unused") // The parameters field of the input is not yet used by rdbService.
        PVString pvParams = pvArguments.getStringField(PARAMS_ARGNAME);

		// Update arguments to the service with what we got at the command line, like "swissfel:allmagnetnames"
		//
		if ( args.length <= 0 )
		{
			System.err.println("No name of a db query was given; exiting.");
			System.exit(NOARGS);
		}
		pvQuery.put(args[0]);
		
		// TODO: There are presently no record parameters supported (ie, parameters 
		// member of the rdbService xml record is not used). This would be used if for instance 
		// supporting SQL "define" parameter replacement (eg &1,&2 etc), or any other
		// runtime modification you wanted to make to the way the RDB query is formed or processed.	

		// Execute the service request for data subject to the arguments constructed above. 
		//
		try
		{
			pvResult = client.request( pvArguments );
		}
		catch ( Exception ex )
		{
			System.err.println(ex.getMessage());
			pvResult = null;
		}
		
		// Print the results if we have any.
		//
		if (pvResult != null) 
		{
			PVString normativetypeField = pvResult.getStringField(TYPE_FIELD_NAME);

			if ( normativetypeField == null ) 
			{
				System.err.println("Unable to get data: unexpected data structure returned from "+
						OBJECTIVE_SERVICE_NAME + ". Expected normativetype member, "+
						"but normativetype not found in returned datum."); 
				System.err.println(pvResult);
				System.exit(NOTNORMATIVETYPE);
			}
			String type = normativetypeField.get();
			if (type.compareTo(NTTABLE_TYPE_NAME ) != 0)	
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
			
			// skip past the first field, that was the normative type specifier.
			int N_dataFields = pvResult.getNumberFields()-1;
			
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
				String fieldName = pvFielde.getField().getFieldName();
				
				// Skip past the meta-data field named "normativeType"
				if (fieldName.compareTo(TYPE_FIELD_NAME) == 0)
					continue;
				
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
		org.epics.ca.ClientFactory.stop();
		
		_dbg("DEBUG: main(): Completed Successfully");
		System.exit(0);
	}

	
	
	
	/** 
	 * Client is an example implementor of ServiceClientRequester. A client side
	 * of EPICS V4 rpc support must implement a static class of ServiceClientImplemetor.
	 * This class forms the interface between your functional client side code, and the
	 * callbacks required by the client side of pvData and the RPC support.
	 */
	private static class Client implements ServiceClientRequester 
	{	
		private ServiceClient serviceClient = null;
		private PVStructure pvResult = null;

		/**
		 * Connect and wait until connected
		 * 
		 * @param objectiveServiceName The recordName of the service to which to connect.
		 * @return The pvStructure of the arguments expected by the service; the client
		 * "sends arguments" by filling in the elements of this returned structure.
		 */
		void connect( String objectiveServiceName) 
		{
			_dbg("connect() entered");

			// Service name argument must match recordName in XML database exactly, 
			// including case.
			serviceClient = ServiceClientFactory.create(objectiveServiceName, this);			
			// Connect with 5.0s timeout. Increase for slow services. 
			serviceClient.waitConnect(5.0);

			_dbg("connect() exits");
		}
		
		/**
		 * Cleanup client side resource. At least call super serviceClient destroy.
		 */
		void destroy()
		{
		    serviceClient.destroy();
		}
		
		/**
		 * Send a request and wait until done.
		 * 
		 * The service will be issued a "sendRequest" by this method invocation, which 
		 * is its queue to "process the record." Processing the record amounts to
		 * examining the bitset on the server side, processing, and returning a 
		 * PVStructure holding the returned data. 
		 * 
		 * It's important that this request method sets the bits of the bitset 
		 * to indicate which arguments (inside the PVArguments PVStructure) should 
		 * be examined by the server for possible changes in value. Since it's a 
		 * cheap operation, and this is a demo, we just set those bits every time. 
		 * But an optimized client would only actively reset bits in the bitset if
		 * the had changed value since the last sendRequest. 
		 * 
		 * @return PVStructure the data returned by the service for this call.  
		 */
		PVStructure request( PVStructure pvArguments ) 
		{
			_dbg("request() entered");
				
			// Actually execute the request for data on the server.
			serviceClient.sendRequest( pvArguments );
			serviceClient.waitRequest();
			
			_dbg("Request() exits with pvResult="+pvResult.toString());
			return pvResult;
		}
		
		/**
		 * connectResult verifies connection and gets the interface of the specific server
		 * you specified in ServiceClientFactory.create. 
		 * 
		 * You, the client side programmer must supply (aka Override) this method 
		 * definition in your implementation of ServiceClientRequester; but client
		 * side of pvData calls it, you don't call it directly.
		 * 
		 * @see org.epics.pvService.client.ServiceClientRequester#connectResult(org.epics.pvData.pv.Status, 
		 * org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet)
		 */
		@Override
		public void connectResult( Status status ) 
		{
		    if ( !status.isOK() ) 
		    {
		        throw new RuntimeException("Connection error: " + status.getMessage());
		    }

		    return;
		}
		
		/**
		 * requestResult receives the data from the server.
		 * 
		 * You, the client side programmer must supply (aka Override) this method 
		 * definition in your implementation of ServiceClientRequester; but client
		 * side of pvData calls it, you don't call it directly.
		 * 
		 * @see org.epics.pvService.client.ServiceClientRequester#requestResult(org.epics.pvData.pv.Status, 
		 * org.epics.pvData.pv.PVStructure)
		 */
		@Override
		public void requestResult(Status status, PVStructure pvResult) 
		{
		    if ( !status.isOK() ) 
		    {
		        // throw new RuntimeException("Request error: " + status.getMessage());
		    	System.err.println(status.getMessage());
		    	this.pvResult = null;
		    }
		    else
		    	this.pvResult = pvResult;
		    return;
		}
		
		/**
		 * The message method is called back by the service to acquire the
		 * name of client. Right! It's that clever: you
		 * can get and print diagnostic messages from a server while it's
		 * processing your request, not just an exit status. Neatto eh.
		 * 
		 * You, the client side programmer must supply (aka Override) this method 
		 * definition in your implementation of ServiceClientRequester; but the
		 * client side of pvData calls it, not your client side code directly.
		 * 
		 * @see org.epics.pvData.pv.Requester#getRequesterName()
		 */
		@Override
		public String getRequesterName() 
		{
			return CLIENT_NAME;  
 		}
		
		/**
		 * The message method is called back by the service to issue messages
		 * while it is processing requests for you. Right! It's that clever: you
		 * can get and print diagnostic messages from a server while it's
		 * processing your request, not just an exit status. Neatto eh.
		 * 
		 * You, the client side programmer must supply this method; but the
		 * server calls it, not your client side code.
		 * 
		 * @see org.epics.pvData.pv.Requester#message(java.lang.String,
		 *      org.epics.pvData.pv.MessageType)
		 */
		@Override
		public void message(String message, MessageType messageType) 
		{
			System.out.printf("%n%s %s%n",messageType.toString(),message);
		}
		
	} // end class Client

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
	private static void _dbg(String debug_message)
	{
		if (DEBUG)
			System.err.println("DEBUG: "+debug_message);
	}

} // end class RdbClient
