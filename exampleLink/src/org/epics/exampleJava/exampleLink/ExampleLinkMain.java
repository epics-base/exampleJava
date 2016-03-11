/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */


package org.epics.exampleJava.exampleLink;

import org.epics.nt.NTScalarArray;
import org.epics.nt.NTScalarArrayBuilder;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.ContextLocal;


/**
 * @author Marty Kraimer
 *
 */
public class ExampleLinkMain {

    public static void main(String[] args)
    {
        int argc = args.length;
        String provider = "pva";
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("provider");
            System.out.println("default");
            System.out.println(provider);
            System.exit(0);
        }
        if(argc>0) provider = args[0];
        PVDatabase master = PVDatabaseFactory.getMaster();
        ContextLocal context = new ContextLocal();
        context.start(false);
        NTScalarArrayBuilder builder = NTScalarArray.createBuilder();
        PVStructure pvStructure = builder.
            value(ScalarType.pvDouble).
            addAlarm().
            addTimeStamp().
            createPVStructure();
        String recordName = "doubleArray";
        PVRecord pvRecord = new PVRecord(recordName,pvStructure);
        master.addRecord(pvRecord);
        pvRecord = ExampleLinkRecord.create("exampleLink",provider,recordName);
        master.addRecord(pvRecord);
        while(true) {
            System.out.print("waiting for exit: ");
            String value = System.console().readLine();
            if(value.equals("exit")) break;
        }
        context.destroy();
        master.destroy();
        System.out.println("ExampleLink exiting");
    }
}
