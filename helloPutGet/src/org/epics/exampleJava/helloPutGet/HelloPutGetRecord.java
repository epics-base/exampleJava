/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.exampleJava.helloPutGet;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVRecord;

public class HelloPutGetRecord extends PVRecord
{
	private PVString pvArgumentValue = null;
	private PVString pvResultValue = null;
    
   

    public static HelloPutGetRecord create(String recordName)
    {
    	 StandardField standardField = StandardFieldFactory.getStandardField();
    	    FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    	    PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    	    Structure topStructure = fieldCreate.createFieldBuilder().
    	        addNestedStructure("argument").
    	            add("value",ScalarType.pvString).
    	            endNested().
    	        addNestedStructure("result") .
    	            add("value",ScalarType.pvString) .
    	            add("timeStamp",standardField.timeStamp()) .
    	            endNested().
    	        createStructure();
    	    PVStructure pvStructure = pvDataCreate.createPVStructure(topStructure);

    	    HelloPutGetRecord pvRecord =
    	        new HelloPutGetRecord(recordName,pvStructure);
    	    if(!pvRecord.init()) return null;
    	    return pvRecord;
    }
    public HelloPutGetRecord(String recordName,PVStructure pvStructure)
    {
        super(recordName,pvStructure);
    }
    
    private boolean init()
    {
    	PVStructure pvStructure = getPVRecordStructure().getPVStructure();
        pvArgumentValue = pvStructure.getSubField(PVString.class,"argument.value");
        if(pvArgumentValue==null) return false;
        pvResultValue = pvStructure.getSubField(PVString.class,"result.value");
        if(pvResultValue==null) return false;
        return true;
    }
    
    public void process()
    {
    	pvResultValue.put("Hello " + pvArgumentValue.get());
        super.process();
    }
}
