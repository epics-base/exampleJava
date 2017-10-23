/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 * @since 2013.07.24
 */
package org.epics.exampleJava.exampleDatabase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.nt.*;

/**
 * A PVRecord that has fields that change value only because a client put.
 *
 */
public class ExampleSoftRecord extends PVRecord {
    /**
     * Create an instance of ExampleHelloRecord.
     * @param recordName The name of the record.
     * @return The new instance.
     */
    public static PVRecord create(String recordName)
    {
        NTScalarBuilder ntScalarBuilder = NTScalar.createBuilder();
        PVStructure pvStructure =  ntScalarBuilder.
            value(ScalarType.pvDouble).
            addAlarm().
            addTimeStamp().
            addDisplay().
            addControl().
            createPVStructure();
        
        
        PVRecord pvRecord = new ExampleSoftRecord(recordName,pvStructure);
        PVDatabase master = PVDatabaseFactory.getMaster();
        master.addRecord(pvRecord);
        return pvRecord;
    }
   
    private ExampleSoftRecord(String recordName,PVStructure pvStructure) {
        super(recordName,pvStructure);
        
    }

    /**
     * Implement hello semantics.
     */
    public void process()
    {
        int level = getTraceLevel();
        if(level>1) System.out.println("HelloRecord::process");
    }
}
