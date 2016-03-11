/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */


package org.epics.exampleJava.powerSupply;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;


/**
 * @author Marty Kraimer
 *
 */
public class PowerSupplyMonitor {

    public static void main(String[] args)
    {   
        PvaClient pva= PvaClient.get();
        try {
            PvaClientMonitor monitor = pva.channel("powerSupply").monitor("");
            while(true) {
                if(!monitor.waitEvent(0.0)) {
                    System.out.println("waitEvent returned false. Why???");
                    continue;
                }
                PvaClientMonitorData monitorData = monitor.getData();
                System.out.println(" changed " + monitorData.getChangedBitSet());
                System.out.println(monitorData.showChanged());
                System.out.println(" overrun " + monitorData.getOverrunBitSet());
                System.out.println(monitorData.showOverrun());
                monitor.releaseEvent();
            }
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
            System.exit(1);;
        }
        System.out.println("_____powerSupplyClient done_______");
        pva.destroy();
    }
}
