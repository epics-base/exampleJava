/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleClient;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.Status;


public class ExamplePvaClientPut
{
    static void exampleDouble(PvaClient pva,String channelName,String provider)
    {
        System.out.print("__exampleDouble__");
        System.out.println(" channelName " + channelName + " provider " + provider);
        PvaClientChannel channel = pva.channel(channelName,provider,2.0);
        PvaClientPut put = channel.put();
        PvaClientPutData putData = put.getData();
        PvaClientMonitor monitor = pva.channel(channelName,provider,2.0).monitor("value");
        PvaClientMonitorData monitorData = monitor.getData();
        putData.putDouble(3.0); put.put();
        System.out.println(channel.get("field(value)").getData().showChanged());
        putData.putDouble(4.0); put.put();
        System.out.println(channel.get("field(value)").getData().showChanged());
        if(!monitor.waitEvent(5.0)) {
            System.out.println("waitEvent returned false. Why???");
        } else while(true) {
            System.out.println("monitor changed\n" + monitorData.showChanged());;
            monitor.releaseEvent();
            if(!monitor.poll()) break;
        }
    }

    static void exampleDoubleArray(PvaClient pva,String channelName,String provider)
    {
        System.out.print("__exampleDoubleArray__");
        System.out.println(" channelName " + channelName + " provider " + provider);
        PvaClientChannel channel = pva.channel(channelName,provider,2.0);
        PvaClientPut put = channel.put();
        PvaClientPutData putData = put.getData();
        PvaClientMonitor monitor = pva.channel(channelName,provider,2.0).monitor("value");
        PvaClientMonitorData monitorData = monitor.getData();
        double[] data = new double[5];
        for(int i=0; i< data.length; ++i) data[i] = .1*i;
        putData.putDoubleArray(data); put.put();
        System.out.println(channel.get("field(value)").getData().showChanged());
        for(int i=0; i< data.length; ++i) data[i] = .1*(i+1);
        putData.putDoubleArray(data); put.put();
        System.out.println(channel.get("field(value)").getData().showChanged());
        if(!monitor.waitEvent(5.0)) {
            System.out.println("waitEvent returned false. Why???");
        } else while(true) {
            System.out.println("monitor changed\n" + monitorData.showChanged());;
            monitor.releaseEvent();
            if(!monitor.poll()) break;
        }
    }

    static void examplePVFieldPut(PvaClient pva,String channelName,String provider)
    {
        System.out.print("__exampleDouble__");
        System.out.println(" channelName " + channelName + " provider " + provider);
        PvaClientChannel channel = pva.channel(channelName,provider,2.0);
        PvaClientPut put = channel.put();
        PvaClientPutData putData = put.getData();
        PVScalar pvScalar = (PVScalar)putData.getValue();
        Convert  convert = ConvertFactory.getConvert();
        convert.fromDouble(pvScalar, 1.0); put.put();
        System.out.println(channel.get("field(value)").getData().showChanged());
        convert.fromDouble(pvScalar, 2.0); put.put();
        System.out.println(channel.get("field(value)").getData().showChanged());
    }

    public static void main( String[] args )
    {
        System.out.println("_____examplePvaClientPut starting_______");
        PvaClient pva= PvaClient.get("pva ca");
        try {
            exampleDouble(pva,"PVRdouble","pva");
            exampleDoubleArray(pva,"PVRdoubleArray","pva");
            examplePVFieldPut(pva,"PVRint","pva");
            PvaClientChannel pvaChannel = pva.createChannel("DBRdouble00","ca");
            pvaChannel.issueConnect();
            Status status = pvaChannel.waitConnect(2.0);
            pvaChannel.destroy();
            if(status.isOK()) {
                exampleDouble(pva,"DBRdouble00","pva");
                exampleDouble(pva,"DBRdouble00","ca");
                exampleDoubleArray(pva,"DBRdoubleArray","pva");
                exampleDoubleArray(pva,"DBRdoubleArray","ca");
                examplePVFieldPut(pva,"DBRint00","pva");
                examplePVFieldPut(pva,"DBRint00","ca");
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
