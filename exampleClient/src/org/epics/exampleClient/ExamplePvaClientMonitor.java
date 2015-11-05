/*ExamplePvaClientMonitor.java */
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
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.pv.Status;


public class ExamplePvaClientMonitor
{


    static void exampleMonitor(PvaClient pva,String recordName,String provider)
    {
        System.out.println("__exampleDouble recordName " + " provider " + provider);
        PvaClientMonitor monitor = pva.channel(recordName,provider,2.0).monitor("");
        PvaClientMonitorData pvaData = monitor.getData();
        PvaClientPut put = pva.channel(recordName,provider,2.0).put("");
        PvaClientPutData putData = put.getData();
        for(int ntimes=0; ntimes<5; ++ntimes)
        {
            try {
                double value = ntimes;
                putData.putDouble(value); put.put();
                if(!monitor.waitEvent(0.0)) {
                    System.out.println("waitEvent returned false. Why???");
                }
                System.out.println("changed");
                System.out.println(pvaData.showChanged());
                System.out.println("overrun");
                System.out.println(pvaData.showOverrun());
                monitor.releaseEvent();
            } catch (RuntimeException e) {
                System.out.println("exception " + e.getMessage());
            }
        }
    }

    public static void main( String[] args )
    {
        System.out.println("_____examplePvaClientMonitor starting_______");
        PvaClient pva= PvaClient.get();
        exampleMonitor(pva,"PVRdouble","pva");
        PvaClientChannel pvaChannel = pva.createChannel("DBRdouble00","ca");
        pvaChannel.issueConnect();
        Status status = pvaChannel.waitConnect(2.0);
        if(status.isOK()) {
             exampleMonitor(pva,"DBRdouble00","pva");
             exampleMonitor(pva,"DBRdouble00","ca");
        } else {
            System.out.println("DBRdouble00 not found");
        }
        System.out.println("_____examplePvaClientMonitor done_______");
    }

}
