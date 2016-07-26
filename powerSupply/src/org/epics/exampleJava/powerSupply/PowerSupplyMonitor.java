/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
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
        PvaClient pva= PvaClient.get("pva");
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
            System.err.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);;
        }
        System.out.println("_____powerSupplyClient done_______");
        pva.destroy();
    }
}
