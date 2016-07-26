/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */
package org.epics.exampleJava.helloPutGet;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvaClient.PvaClientPutGet;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;


public class HelloPutGetClient
{
    public static void main( String[] args )
    {
        PvaClient pva= PvaClient.get("pva");
        try {
            PvaClientChannel channel = pva.channel("helloPutGet");
            PvaClientPutGet putGet = channel.createPutGet();
            putGet.connect();
            PvaClientPutData putData = putGet.getPutData();
            PVStructure arg = putData.getPVStructure();
            PVString pvValue = arg.getSubField(PVString.class,"argument.value");
            pvValue.put("World");
            putGet.putGet();
            PvaClientGetData getData = putGet.getGetData();
            System.out.println(getData.getPVStructure().toString());
        } catch (RuntimeException e) {
            System.err.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);;
        }
        System.out.println("_____exampleLinkClient done_______");
        pva.destroy();
    }

}
