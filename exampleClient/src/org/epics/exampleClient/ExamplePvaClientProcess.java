/*ExamplePvaClientProcess.java */
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
import org.epics.pvaClient.PvaClientProcess;


public class ExamplePvaClientProcess
{


    static void exampleProcess(PvaClient pva)
    {
        System.out.println("example process");
        PvaClientChannel channel = pva.channel("PVRdouble");
        PvaClientProcess process = channel.createProcess();
        try {
            process.process();
            System.out.println(channel.get("field()").getData().showChanged());
            process.process();
            System.out.println(channel.get("field()").getData().showChanged());
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
        }
    }

    public static void main( String[] args )
    {
        PvaClient pva= PvaClient.get();
        exampleProcess(pva);
    }

}
