/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */


package org.epics.exampleJava.exampleLink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.epics.nt.NTScalarArray;
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
        String exampleLinkRecordName = "exampleLink";
        String linkedRecordName = "doubleArray";
        boolean generateLinkedRecord = true;
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("provider exampleLinkRecordName linkedRecordName generateLinkedRecord");
            System.out.println("default");
            System.out.println(provider + " " + exampleLinkRecordName + " " + linkedRecordName + " " + generateLinkedRecord);
            System.exit(0);
        }
        if(argc>0) provider = args[0];
        if(argc>1) exampleLinkRecordName = args[1];
        if(argc>2) linkedRecordName = args[2];
        if(argc>3) {
            String val = args[3];
            if(val.equals("false")) generateLinkedRecord = false;
        }
        try {
            PVDatabase master = PVDatabaseFactory.getMaster();
            ChannelProvider channelProvider = ChannelProviderLocalFactory.getChannelServer();
            if(generateLinkedRecord) {
                NTScalarArrayBuilder builder = NTScalarArray.createBuilder();
                PVStructure pvStructure = builder.
                        value(ScalarType.pvDouble).
                        addAlarm().
                        addTimeStamp().
                        createPVStructure();
                master.addRecord(new PVRecord(linkedRecordName,pvStructure));
            }
            ServerContextImpl context = ServerContextImpl.startPVAServer("local",0,true,System.out);
            PvaClient pva= PvaClient.get(provider);
            PVRecord pvRecord = ExampleLinkRecord.create(pva,exampleLinkRecordName,provider,linkedRecordName);
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
