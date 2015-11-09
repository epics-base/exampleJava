/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */


package org.epics.exampleLink;

import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.ContextLocal;


/**
 * @author Marty Kraimer
 *
 */
public class ExampleLink {
    private static final StandardPVField standardPVField  = StandardPVFieldFactory.getStandardPVField();


    public static void main(String[] args)
    {
        PVDatabase master = PVDatabaseFactory.getMaster();
        ContextLocal context = new ContextLocal();
        context.start(false);
        String  properties = "alarm,timeStamp";
        String recordName = "doubleArray";
        PVStructure pvStructure = standardPVField.scalarArray(ScalarType.pvDouble,properties);
        PVRecord pvRecord = new PVRecord(recordName,pvStructure);
        master.addRecord(pvRecord);
        pvRecord = ExampleLinkRecord.create("exampleLink",recordName);
        master.addRecord(pvRecord);
        while(true) {
            System.out.print("waiting for exit: ");
            String value = System.console().readLine();
            if(value.equals("exit")) break;
        }
        context.destroy();
        master.destroy();
        System.out.println("ExampleLink exiting");
        System.exit(0);
    }
}
