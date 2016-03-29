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
        process.process();
        System.out.println(channel.get("field()").getData().showChanged());
        process.process();
        System.out.println(channel.get("field()").getData().showChanged());
    }

    public static void main( String[] args )
    {
        PvaClient pva= PvaClient.get();
        try {
            exampleProcess(pva);
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
