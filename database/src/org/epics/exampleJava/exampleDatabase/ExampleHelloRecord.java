// Copyright information and license terms for this software can be
// found in the file LICENSE that is included with the distribution

/**
 * @author mrk
 * @date 2013.07.24
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

public class ExampleHelloRecord extends PVRecord {
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();
    
    private PVString arg;
    private PVString result;

    public static PVRecord create(String recordName)
    {
        FieldBuilder fb = fieldCreate.createFieldBuilder();
        Structure structure = 
            fb.addNestedStructure("argument").
                add("value",ScalarType.pvString).
                endNested().
            addNestedStructure("result").
                add("value",ScalarType.pvString).
                endNested().
            add("timeStamp",standardField.timeStamp()).
            createStructure();
       PVRecord pvRecord = new ExampleHelloRecord(recordName,pvDataCreate.createPVStructure(structure));
       PVDatabase master = PVDatabaseFactory.getMaster();
       master.addRecord(pvRecord);
       return pvRecord;
    }
    public ExampleHelloRecord(String recordName,PVStructure pvStructure) {
        super(recordName,pvStructure);
        arg = pvStructure.getSubField(PVString.class, "argument.value");
        if(arg==null) throw new IllegalArgumentException("arg not found");
        result = pvStructure.getSubField(PVString.class, "result.value");
        if(result==null) throw new IllegalArgumentException("result not found");
        
    }
    
    public void process()
    {
        int level = getTraceLevel();
        if(level>1) System.out.println("HelloRecord::process");
        result.put("Hello " +arg.get());
        super.process();
    }
}
