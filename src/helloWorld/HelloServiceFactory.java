/**
 * helloWorld is meant to illustrate the simplest example of a remote procedure call (RPC)
 * style interaction between a client and a server using EPICS V4. 
 */
package helloWorld;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvioc.database.PVRecord;
import org.epics.pvioc.pvAccess.RPCServer;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.*;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.StatusCreate;

/**
 * HelloServiceFactory is an example of the factory class a user-developer of EPICS V4
 * would write to implement a trivial RPC style server.
 *   
 * @author Greg White 29.Sep.11 (gregory.white@psi.ch)
 * @version 7-May-2012, Greg White (greg@slac.stanford.edu) Changed calls to pvAccess api
 * following changes in introspection API.
 */
public class HelloServiceFactory {
	
	/**
	 * You are required to implement RPCServer create, and it must at minimum return 
	 * an instance of RPCServerImpl.
	 * 
	 * @return a new RPCServerImpl
	 */
	public static RPCServer create() 
	{
		return new RPCServerImpl();
	}
	
	// EPICS v4 Status is the mechanism for communicating success or failure of
	// operations between client and server. Make yourself an object that can
	// create a status object, and then use it to create the "ok" status at least.
	//
	private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
	private static final Status okStatus = statusCreate.getStatusOK();
	
	// All EPICS V4 services return PVData objects (by definition). Create the 
	// factory object that will allow you to create the returned PVData object later. 
	//
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

	
	private static class RPCServerImpl implements RPCServer
	{
		private PVString inputPersonName;               // Will hold data from the client side
		private ChannelRPCRequester channelRPCRequester; 
		
		/** 
		 * You must at declare a "destroy" method for your server, so pvAccess can
		 * clean up.
		 * 
		 * @see org.epics.ioc.pvAccess.RPCServer#destroy()
		 */
		@Override
		public void destroy() {}

		/**
		 * You must define an "initialize" method as defined; it is called on
		 * each invocation of a request on the client, not just at startup. It's
		 * job here is to retrieve the data that was sent in the client's call, the
		 * personsname that, in our helloWorld example, was supplied to client.
		 *  
		 * @see org.epics.ioc.pvAccess.RPCServer#initialize(org.epics.ca.client.Channel, 
		 * org.epics.ioc.database.PVRecord, org.epics.ca.client.ChannelRPCRequester, 
		 * org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet, 
		 * org.epics.pvData.pv.PVStructure)
		 */
		@Override
		public Status initialize(Channel channel, PVRecord pvRecord,
				ChannelRPCRequester channelRPCRequester, PVStructure pvRequest)
		{
			this.channelRPCRequester = channelRPCRequester;

			// Recover the input parameter from client side
			// this.inputPersonName = pvArgument.getStringField("personsname");

			return okStatus;
		}

		/**
		 * The overridden request method "returns" the data to the client, by
		 * calling given ChannelRPCRequester's requestDone method with the data to return.
		 * I.e. this is where you put the meat of the server.
		 * 
		 * @see org.epics.ioc.pvAccess.RPCServer#request()
		 */
		@Override
		public void request( PVStructure pvArgument ) 
		{	
		    FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		    // Create top level structure of the introspection interface, which is used 
		    // to send the response to the client.
		    Field[] fields = new Field[1];
		    String[] fieldNames = new String[1];
		    fields[0] = fieldCreate.createScalar(ScalarType.pvString); 
		    fieldNames[0] = "greeting";
		    Structure structure = fieldCreate.createStructure(fieldNames, fields);
		    
		    // Now create the top level structure of the data interface.
			PVStructure pvTop = pvDataCreate.createPVStructure(null,structure);
			
			// Now extract from the constructed data interface the value of "greeting" 
			// field. The value we'll return, is "Hello" concatenated 
			// to the value of the input parameter called "personsname". 
			//
			PVString greetingvalue = pvTop.getStringField("greeting"); 
			// Extract the arguments. Just one in this case.
            inputPersonName = pvArgument.getStringField("personsname");
			greetingvalue.put("Hello " + inputPersonName.get());
			
			// Return the constructed structure, with the message pay load, to client.
			//
			channelRPCRequester.requestDone(okStatus, pvTop);
		}
	}
}
