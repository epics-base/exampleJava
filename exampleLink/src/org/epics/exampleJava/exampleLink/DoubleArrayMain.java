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
import org.epics.pvaccess.PVAConstants;
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
public class DoubleArrayMain {

    public static void main(String[] args)
    {
        int argc = args.length;
        String doubleArrayRecordName = "doubleArray";
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("doubleArrayRecordName");
            System.out.println("default");
            System.out.println(doubleArrayRecordName);
            System.exit(0);
        }
        if(argc>0) doubleArrayRecordName = args[0];
        try {
            PVDatabase master = PVDatabaseFactory.getMaster();
            ChannelProvider channelProvider = ChannelProviderLocalFactory.getChannelServer();
            NTScalarArrayBuilder builder = NTScalarArray.createBuilder();
            PVStructure pvStructure = builder.
                    value(ScalarType.pvDouble).
                    addAlarm().
                    addTimeStamp().
                    createPVStructure();
            master.addRecord(new PVRecord(doubleArrayRecordName,pvStructure));

            ServerContextImpl context = ServerContextImpl.startPVAServer(PVAConstants.PVA_ALL_PROVIDERS,0,true,null);
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
            System.out.println("ExampleLink exiting");
        } catch (PVAException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
