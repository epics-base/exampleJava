/*ExamplePvaClientGet.java */
/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
/**
 * @author mrk
 */

/* Author: Marty Kraimer */

package org.epics.exampleClient;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvdata.pv.Status;


public class ExamplePvaClientGet
{

    static void exampleDouble(PvaClient pva,String channelName,String provider)
    {
        System.out.print("__exampleDouble__");
        System.out.println(" channelName " + channelName + " provider " + provider);
        double value;
        System.out.println("short way");
        value =  pva.channel(channelName,provider,2.0).get().getData().getDouble();
        System.out.println("as double " + value);
        System.out.println("repeat short way");
        value =  pva.channel(channelName,provider,2.0).get().getData().getDouble();
        System.out.println("as double " + value);

        System.out.println("long way");
        PvaClientChannel pvaChannel = pva.createChannel(channelName,provider);
        pvaChannel.issueConnect();
        Status status = pvaChannel.waitConnect(2.0);
        if(!status.isOK()) {System.out.println(" connect failed"); return;}
        PvaClientGet pvaGet = pvaChannel.createGet();
        pvaGet.issueConnect();
        status = pvaGet.waitConnect();
        if(!status.isOK()) {System.out.println(" createGet failed"); return;}
        PvaClientGetData pvaData = pvaGet.getData();
        value = pvaData.getDouble();
        System.out.println("as double " + value);
        pvaChannel.destroy();
    }

    static void exampleDoubleArray(PvaClient pva,String channelName,String provider)
    {
        System.out.print("__exampleDoubleArray__");
        System.out.println(" channelName " + channelName + " provider " + provider);
        double[] value;
        System.out.println("short way");
        value =  pva.channel(channelName,provider,2.0).get().getData().getDoubleArray();
        System.out.print("as doubleArray");
        for(int i=0; i<value.length; ++i) {
            System.out.print(" ");
            System.out.print(value[i]);
        }
        System.out.println();
        System.out.println("repeat short way");
        value =  pva.channel(channelName,provider,2.0).get().getData().getDoubleArray();
        System.out.print("as doubleArray");
        for(int i=0; i<value.length; ++i) {
            System.out.print(" ");
            System.out.print(value[i]);
        }
        System.out.println();

        System.out.println("long way");
        PvaClientChannel pvaChannel = pva.createChannel(channelName,provider);
        pvaChannel.connect(2.0);
        PvaClientGet pvaGet = pvaChannel.createGet();
        PvaClientGetData pvaData = pvaGet.getData();
        value = pvaData.getDoubleArray();
        System.out.print("as doubleArray");
        for(int i=0; i<value.length; ++i) {
            System.out.print(" ");
            System.out.print(value[i]);
        }
        System.out.println();
        pvaChannel.destroy();
    }


    public static void main( String[] args )
    {
        System.out.println("_____examplePvaClientGet starting_______");
        PvaClient pva= PvaClient.get("pva ca");
        try {
            exampleDouble(pva,"PVRdouble","pva");
            exampleDoubleArray(pva,"PVRdoubleArray","pva");
            PvaClientChannel pvaChannel = pva.createChannel("DBRdouble00","ca");
            pvaChannel.issueConnect();
            Status status = pvaChannel.waitConnect(2.0);
            pvaChannel.destroy();
            if(status.isOK()) {
                exampleDouble(pva,"DBRdouble00","pva");
                exampleDouble(pva,"DBRdouble00","ca");
                exampleDoubleArray(pva,"DBRdoubleArray","pva");
                exampleDoubleArray(pva,"DBRdoubleArray","ca");

            } else {
                System.out.println("DBRdouble00 not found");
            }
            System.out.println("_____examplePvaClientGet done_______");
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
