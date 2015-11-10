/*ExamplePvaClientMultiDouble.java */
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
import org.epics.pvaClient.PvaClientMultiChannel;
import org.epics.pvaClient.PvaClientMultiGetDouble;
import org.epics.pvaClient.PvaClientMultiMonitorDouble;
import org.epics.pvaClient.PvaClientMultiPutDouble;
import org.epics.pvdata.pv.Status;

public class ExamplePvaClientMultiDouble
{
    
    static String printData(double[] data)
    {
        String ret = "";
        for(int i=0; i<data.length; ++i) {
            if(i>0)ret += ",";
            ret += data[i];
        }
        return ret;
    }
    
    static void example(
            PvaClient pva,
            String provider,
            String[] channelNames)
    {
        System.out.println("_example provider " + provider + " channels " + channelNames + "_");
        int num = channelNames.length;
        PvaClientMultiChannel multiChannel =
                PvaClientMultiChannel.create(pva,channelNames,provider,0);
        PvaClientMultiGetDouble multiGet = multiChannel.createGet();
        PvaClientMultiPutDouble multiPut =multiChannel.createPut();
        PvaClientMultiMonitorDouble multiMonitor = multiChannel.createMonitor();
        double[] data = new double[num];
        for(int i=0; i<num; ++i) data[i] = 0.0;
        for(double value = 0.0; value< 1.0; value+= .2) {
            try {
                for(int i=0; i<num; ++i) data[i] = value + i;
                System.out.println("put " + printData(data));
                multiPut.put(data);
                data =  multiGet.get();
                System.out.println("get " + printData(data));
                boolean result = multiMonitor.waitEvent(.1);
                while(result) {
                    System.out.println("monitor " + printData(data));
                    result = multiMonitor.poll();
                }
            } catch (RuntimeException e) {
                System.out.println("exception " + e.getMessage());
            }

        }
    }

    public static void main( String[] args )
    {
        System.out.println("_____examplePvaClientMultiDouble starting_______");
        PvaClient pva = PvaClient.get();
        int num = 5;
        String[] names = new String[num];
        names[0] = "PVRdouble01";
        names[1] = "PVRdouble02";
        names[2] = "PVRdouble03";
        names[3] = "PVRdouble04";
        names[4] = "PVRdouble05";
        example(pva,"pva",names);
        PvaClientChannel pvaChannel = pva.createChannel("DBRdouble00","ca");
        pvaChannel.issueConnect();
        Status status = pvaChannel.waitConnect(2.0);
        if(status.isOK()) {
            names[0] = "DBRdouble01";
            names[1] = "DBRdouble02";
            names[2] = "DBRdouble03";
            names[3] = "DBRdouble04";
            example(pva,"pva",names);
            example(pva,"ca",names);
        } else {
            System.out.println("DBRdouble00 not found");
        }
        System.out.println("_____examplePvaClientMultiDouble done_______");
        pva.destroy();
    }
}
