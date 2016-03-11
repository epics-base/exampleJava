/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */


package org.epics.exampleJava.helloPutGet;

import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.ContextLocal;


/**
 * @author Marty Kraimer
 *
 */
public class HelloPutGetMain {

    public static void main(String[] args)
    {
        PVDatabase master = PVDatabaseFactory.getMaster();
        ContextLocal context = new ContextLocal();
        context.start(false);
        String recordName = "helloPutGet";
        PVRecord pvRecord = HelloPutGetRecord.create(recordName);
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
