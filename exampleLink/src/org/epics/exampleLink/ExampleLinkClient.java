/*ExamplePvaClientMonitor.java */
/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
/**
 * @author mrk
 */
package org.epics.exampleLink;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;


public class ExampleLinkClient
{
    public static void main( String[] args )
    {
        System.out.println("_____exampleLinkClient starting_______");
        PvaClient pva= PvaClient.get();
        try {
            PvaClientPut put = pva.channel("doubleArray").put();
            PvaClientPutData putData = put.getData();
            PvaClientMonitor monitor = pva.channel("exampleLink").monitor("");
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
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
            System.exit(1);;
        }
        System.out.println("_____exampleLinkClient done_______");
        System.exit(0);
    }

}
