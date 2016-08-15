/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleLink;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;


public class ExampleLinkClient
{
    public static void main( String[] args )
    {
        int argc = args.length;
        String provider = "pva";
        String doubleArray = "doubleArray";
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("provider");
            System.out.println("default");
            System.out.println(provider + " " + doubleArray);
            System.exit(0);
        }
        System.out.println("_____exampleLinkClient starting_______");
        if(argc>0) provider = args[0];
        if(argc>1) doubleArray = args[1];
        String providers = "pva";
        if(provider.equals("ca")) providers = "pva ca";
        PvaClient pva= PvaClient.get(providers);
        try {
            PvaClientPut put = pva.channel(doubleArray,provider,5.0).put();
            PvaClientPutData putData = put.getData();
            PvaClientMonitor monitor = pva.channel("exampleMonitorLink").monitor("");
            PvaClientMonitorData pvaData = monitor.getData();
            if(!monitor.waitEvent(0.0)) {
                System.out.println("waitEvent returned false. Why???");
            } else {
                System.out.println("exampleLink\n" +pvaData.getPVStructure());
                monitor.releaseEvent();
            }
            double[] data = new double[5];
            for(int i=0; i< data.length; ++i) data[i] = 0.0;
            putData.putDoubleArray(data); put.put();
            if(!monitor.waitEvent(0.0)) {
                System.out.println("waitEvent returned false. Why???");
            } else {
                System.out.println("exampleLink\n" +pvaData.getPVStructure());
                monitor.releaseEvent();
            }
            for(int i=0; i< data.length; ++i) data[i] = .1*(i+1);
            putData.putDoubleArray(data); put.put();
            if(!monitor.waitEvent(0.0)) {
                System.out.println("waitEvent returned false. Why???");
            } else {
                System.out.println("exampleLink\n" +pvaData.getPVStructure());
                monitor.releaseEvent();
            }
        } catch (Exception e) {
            System.err.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);;
        }
        System.out.println("_____exampleLinkClient done_______");
        pva.destroy();
    }

}
