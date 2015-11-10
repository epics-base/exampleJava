package org.epics.serviceAPI;

import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

/**
 * ServiceAPIClient is the client side of a notional EPICS archive client. More specifically, ServiceAPIClient exemplifies 
 * the client side of an EPICS V4 client server system that
 * illustrates the use of standardized EPICS V4 pvStructures for exchanging data in 
 * the classic case of a client making a parameterized request for data, and the 
 * servers response - that is, a so called "Remote Procedure Call" (or RPC). 
 * 
 * <p>The service uses the pattern of an archive client and server to make the illustration.</p>
 * 
 * <p>It also shows using NTURI, a specialized way exchanging URI data between EPICS V4 endpoints.</p>
 * 
 * <p> The client encodes its request in a pvStructure whose fields map to the components
 * of an internet Uniform Resource Identifier (URI, see <a href="http://www.ietf.org/rfc/rfc2396.txt">
 * http://www.ietf.org/rfc/rfc2396.txt</a>). The pvStructure which encodes the URI is 
 * actually defined as an EPICS V4 standard type, NTURI, in the EPICS V4 document
 * <a href="http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html">
 * EPICS V4 Normative Types</a>. So, by using an NTURI, we conform to the IETF standard
 * for making RPC style resource data requests. In the standard EPICS V4 case, the pvAccess
 * protocol is the protocol mapped to the "pva" URI scheme. The EPICS V4 Working Group
 * encourages the use of NTURI for client-server RPC exchanges in an EPICS V4 network,
 * but pvAccess' channelRPC method does not require it. </p> 
 * 
 * <p>EasyPVA (presently in alpha) is used to actually make the request call, which 
 * is a very simple calling interface API for pvAccess. The data returned is
 * in the form of another standard pvStructure type called 
 * <a href="http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html#nttable">
 * NTTable</a>. However, Also no code is spent showing
 * how the returned pvStructure NTTable may be examined and data extracted. For an example
 * of that, see the full rdbService example also in this repo. </p>
 * 
 * @author Greg White SLAC/PSI, 12-Nov-2012 
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">[1] http://www.ietf.org/rfc/rfc2396.txt</a>
 * @see <a href="http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html">
 * [2] EPICS V4 Normative Types</a>
 */
public class ServiceAPIClient 
{
	// Construct an NTURI for making a request to a service that understands 
	// URI auery part arguments named "entity", "starttime" and "endtime".
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private final static Structure queryStructure = fieldCreate.createStructure(
				new String[] {"entity", "starttime", "endtime"},
				new Field[] { fieldCreate.createScalar(ScalarType.pvString),
						      fieldCreate.createScalar(ScalarType.pvString),
						      fieldCreate.createScalar(ScalarType.pvString) });
	private final static Structure uriStructure =
		fieldCreate.createStructure("epics:nt/NTURI:1.0",
				new String[] { "scheme", "path", "query" },
				new Field[] { fieldCreate.createScalar(ScalarType.pvString),
				              fieldCreate.createScalar(ScalarType.pvString),
				              queryStructure });
	
	
	public static void main(String[] args) throws Throwable 
	{


		PVStructure request = PVDataFactory.getPVDataCreate().
			createPVStructure(uriStructure);
		request.getStringField("scheme").put("pva");     // Scheme must be populated for conformance to 
		request.getStringField("path").put("miniArchiveServiceToDemoServiceInterface");
		PVStructure query = request.getStructureField("query");
		query.getStringField("entity").put("quad45:bdes;history");
		query.getStringField("starttime").put("2011-09-16T02.12.55");
		query.getStringField("endtime").put("2011-09-16T10.01.03");
		
		// Now that the request URI is populated, use it to make the data request to the
		// service. Note that the path part of the URI is the same as the channel name to
		// which the service responds. In this example, the service responds is on the single
		// EV4 channel "miniArchiveServiceToDemoServiceInterface", but it could have
		// be configured to respond to any number of channels. For instance, rather than
		// making channel name miniArchiveServiceToDemoServiceInterface, many channels
		// of the form quad45:bdes;history could have been used. In that case, the path
		// part of the URI should encode the channel and the "entity" query parameter would not
		// be used. Also note that, the path part of the URI contains the channel name for
		// conformance to the definition of NTURI; but there is no programmatic link between
		// the channel argument to pvAccess (the argument to createChannel) and the value
		// of the path part of the NTURI encoded in the pvStructure in the request method argument.
		//
		// Using easyPVA in alphaJava this acqu would have been as commented out, but instead see 
		// actual code below.
		// EasyPVA easyPVA = EasyPVAFactory.get();
		// PVStructure result = easyPVA.createChannel("miniArchiveServiceToDemoServiceInterface").createRPC().request(request);
		
		RPCClientImpl client = new RPCClientImpl("miniArchiveServiceToDemoServiceInterface");
		try 
		{
			PVStructure result = client.request(request, 3.0);	
			System.out.println("The URI request structure:\n" + request +"\n\nResulted in:\n" + result);
		}
		catch (RPCRequestException ex)
		{
			// The client connected to the server, but the server request method issued its 
			// standard summary exception indicating it couldn't complete the requested task.
			System.err.println("Acquisition was not successful, " +
				"service responded with an error: " + ex.getMessage());
		}	
		catch (IllegalStateException ex)
		{
			// The client failed to connect to the server. The server isn't running or
			// some other network related error occurred.
			System.err.println("Acquisition was not successful, " +
				"failed to connect: "+ ex.getMessage());
		}
		client.destroy();
		
        System.exit(0);
	}
}
