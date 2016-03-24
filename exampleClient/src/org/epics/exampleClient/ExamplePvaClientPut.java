/*ExamplePvaClientPut.java */
/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
/**
 * @author mrk
 */

package org.epics.exampleClient;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.pv.Status;


public class ExamplePvaClientPut
{
	static void exampleDouble(PvaClient pva,String channelName,String provider)
	{
		System.out.println("__exampleDouble__");
		PvaClientChannel channel = pva.channel(channelName,provider,2.0);
		PvaClientPut put = channel.put();
		PvaClientPutData putData = put.getData();
		putData.putDouble(3.0); put.put();
		System.out.println(channel.get("field()").getData().showChanged());
		putData.putDouble(4.0); put.put();
		System.out.println(channel.get("field()").getData().showChanged());
	}
	
	static void exampleDoubleArray(PvaClient pva,String channelName,String provider)
	{
		System.out.println("__exampleDoubleArray__");
		PvaClientChannel channel = pva.channel(channelName,provider,2.0);
		PvaClientPut put = channel.put();
		PvaClientPutData putData = put.getData();
		double[] data = new double[5];
		for(int i=0; i< data.length; ++i) data[i] = .1*i;
		putData.putDoubleArray(data); put.put();
		System.out.println(channel.get("field()").getData().showChanged());
		for(int i=0; i< data.length; ++i) data[i] = .1*(i+1);
		putData.putDoubleArray(data); put.put();
		System.out.println(channel.get("field()").getData().showChanged());
	}

	public static void main( String[] args )
	{
		System.out.println("_____examplePvaClientPut starting_______");
		PvaClient pva= PvaClient.get();
		try {
			exampleDouble(pva,"PVRdouble","pva");
			exampleDoubleArray(pva,"PVRdoubleArray","pva");
			PvaClientChannel pvaChannel = pva.createChannel("DBRdouble00","ca");
			pvaChannel.issueConnect();
			Status status = pvaChannel.waitConnect(2.0);
			if(status.isOK()) {
				exampleDouble(pva,"DBRdouble00","pva");
				exampleDouble(pva,"DBRdouble00","ca");
				exampleDoubleArray(pva,"DBRdoubleArray","pva");
				exampleDoubleArray(pva,"DBRdoubleArray","ca");
			} else {
				System.out.println("DBRdouble00 not found");
			}
			System.out.println("_____examplePvaClientPut done_______");
		}
		catch (Exception e)
		{
			System.err.println("exception " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
		pva.destroy();
	}

}
