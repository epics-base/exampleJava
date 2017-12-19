/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleLink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.epics.nt.*;
import org.epics.nt.NTScalarArrayBuilder;
import org.epics.pvaClient.PvaClient;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.ChannelProviderLocalFactory;


/**
 * @author Marty Kraimer
 *
 */
public class ExampleLinkMain {

    public static void main(String[] args)
    {
        int argc = args.length;
        String provider = "pva";
        String linkedRecordName = "doubleArray";
        boolean valueIsArray = true;
        boolean generateLinkedRecord = true;
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("provider linkedRecordName valueisArray generateLinkedRecord");
            System.out.println("default");
            System.out.println(provider  + " " + linkedRecordName + " " + valueIsArray + " " + generateLinkedRecord);
            System.exit(0);
        }
        if(argc>0) provider = args[0];
        if(argc>1) linkedRecordName = args[1];
        if(argc>2) {
            String val = args[2];
            if(val.equals("false")) valueIsArray = false;
        }
        if(argc>3) {
            String val = args[3];
            if(val.equals("false")) generateLinkedRecord = false;
        }
        System.out.println("provider " + provider
                + " linkedRecordName " + linkedRecordName
                + " valueIsArray "+ valueIsArray
                + " generateLinkedRecord " + generateLinkedRecord
                );
        try {
            PVDatabase master = PVDatabaseFactory.getMaster();
            ChannelProvider channelProvider = ChannelProviderLocalFactory.getChannelProviderLocal();
            if(generateLinkedRecord) {
                if(valueIsArray) {
                    NTScalarArrayBuilder builder = NTScalarArray.createBuilder();
                    PVStructure pvStructure = builder.
                            value(ScalarType.pvDouble).
                            addAlarm().
                            addTimeStamp().
                            createPVStructure();
                    PVRecord pvRecord = new PVRecord(linkedRecordName,pvStructure);
                    master.addRecord(pvRecord);
                } else {
                    NTScalarBuilder builder = NTScalar.createBuilder();
                    PVStructure pvStructure = builder.
                            value(ScalarType.pvDouble).
                            addAlarm().
                            addTimeStamp().
                            createPVStructure();
                    PVRecord pvRecord = new PVRecord(linkedRecordName,pvStructure);
                    master.addRecord(pvRecord);
                }
            }
            ServerContextImpl context = ServerContextImpl.startPVAServer("local",0,true,System.out);
            PvaClient pva= PvaClient.get(provider);
            PVRecord pvRecord = ExampleMonitorLinkRecord.create(
                 pva,"exampleMonitorLink",provider,linkedRecordName,valueIsArray);           
            master.addRecord(pvRecord);
            pvRecord = ExampleGetLinkRecord.create(
                    pva,"exampleGetLink",provider,linkedRecordName,valueIsArray);           
            master.addRecord(pvRecord);
            pvRecord = ExamplePutLinkRecord.create(
                    pva,"examplePutLink",provider,linkedRecordName,valueIsArray);           
            master.addRecord(pvRecord);
            while(true) {
                System.out.print("waiting for exit: ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String value = null;
                try {
                    value = br.readLine();
                } catch (IOException ioe) {
                    System.out.println("IO error trying to read input!");
                }
                if(value.equals("exit")) break;
            }
            context.destroy();
            master.destroy();
            channelProvider.destroy();
            pva.destroy();
            System.out.println("ExampleLink exiting");
        } catch (PVAException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
