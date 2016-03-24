/*HelloWorldRPC.java */
/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
/**
 * @author mrk
 */

package org.epics.exampleJava.pvDatabaseRPC;


import org.epics.pvaccess.client.rpc.RPCClient;
import org.epics.pvaccess.client.rpc.RPCClientFactory;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.*;
import org.epics.pvdata.pv.*;


public class Move
{
	static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	static final double REQUEST_TIMEOUT = 3.0;
	static final String DEVICE_NAME = "mydevice";
	static final String APP_NAME = "move";

	static Structure deviceStructure = fieldCreate.createFieldBuilder().
			add("x",ScalarType.pvDouble).
			add("y",ScalarType.pvDouble).
			createStructure();

	static Structure requestStructure = fieldCreate.createFieldBuilder().
			addArray("value",deviceStructure).
			createStructure();

	static void usage() {
		System.out.println("Usage:"
				+  " [x_1 y_1] ... [x_n y_n]\n"
				+ "Sequentially sets the values of the x and y fields of "
				+ DEVICE_NAME + " to (x_i,y_i).\n"
				+ "Returns on completion."
				);
	}

	public static void main( String[] args )
	{
		int argc = args.length;
		if(argc==1 && args[0].equals("-help")) {
			usage();
			return;
		}
		if ((argc % 2) != 0)
		{
			System.out.println(APP_NAME + " requires an even number of positions.");
			usage();
			System.exit(1);
		}
		if(argc==0) {
			System.out.println(APP_NAME + " must have at least two positions.");
			usage();
			System.exit(1);
		}
		org.epics.pvaccess.ClientFactory.start();
		try {

			int npoints = argc/2;
			PVStructure pvRequest = pvDataCreate.createPVStructure(requestStructure);
			PVStructureArray pvStructureArray = pvRequest.getSubField(PVStructureArray.class, "value");
			PVStructure[] values = new PVStructure[npoints];
			int indarg = 0;
			for(int i=0; i<npoints; ++i) {
				values[i] = pvDataCreate.createPVStructure(deviceStructure);
				PVDouble pvDouble = values[i].getSubField(PVDouble.class,"x");
				pvDouble.put(Double.valueOf(args[indarg++]));
				pvDouble = values[i].getSubField(PVDouble.class,"y");
				pvDouble.put(Double.valueOf(args[indarg++]));
		    }
			pvStructureArray.put(0, npoints, values, 0);
			pvStructureArray.setLength(npoints);
			RPCClient rpcClient = RPCClientFactory.create(DEVICE_NAME);
			PVStructure pvResult = rpcClient.request(pvRequest, 10.0);
			System.out.println(pvResult.toString());
		} catch (RPCRequestException e) {
			System.out.println("exception " + e.getMessage());
		}
		finally
		{
			// Stop pvAccess client, so that this application exits cleanly.
			org.epics.pvaccess.ClientFactory.stop();
		}
	}

}
