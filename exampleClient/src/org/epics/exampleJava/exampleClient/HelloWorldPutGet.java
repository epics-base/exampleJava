/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleClient;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvaClient.PvaClientPutGet;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;


public class HelloWorldPutGet
{
    static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    static void example(PvaClient pva)
    {
        System.out.println("helloWorldPutGet");
        PvaClientChannel channel = pva.channel("PVRhelloPutGet");
        PvaClientPutGet putGet = channel.createPutGet();
        putGet.connect();
        PvaClientPutData putData = putGet.getPutData();
        PVStructure arg = putData.getPVStructure();
        PVString pvValue = arg.getSubField(PVString.class,"argument.value");
        pvValue.put("World");
        putGet.putGet();
        PvaClientGetData getData = putGet.getGetData();
        System.out.println(getData.getPVStructure());
    }

    public static void main( String[] args )
    {
        PvaClient pva= PvaClient.get("pva");
        try {
            example(pva);
        }
        catch (Exception e)
        {
            System.out.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        pva.destroy();
    }

}
