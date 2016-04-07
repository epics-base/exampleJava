/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.exampleJava.powerSupply;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.property.Alarm;
import org.epics.pvdata.property.AlarmSeverity;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVAlarmFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVRecord;

public class PowerSupplyRecord extends PVRecord
{
    private PVDouble pvCurrent = null;
    private PVDouble pvPower = null;
    private PVDouble pvVoltage = null;
    private PVAlarm pvAlarm = PVAlarmFactory.create();
    private Alarm alarm = new Alarm();
    
    public static PowerSupplyRecord create(String recordName)
    {
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        StandardField standardField = StandardFieldFactory.getStandardField();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        Structure topStructure = fieldCreate.createFieldBuilder().
            add("alarm",standardField.alarm()).
            add("timeStamp",standardField.timeStamp()).
            addNestedStructure("power").
               add("value",ScalarType.pvDouble).
               endNested().
            addNestedStructure("voltage").
               add("value",ScalarType.pvDouble).
               endNested().
            addNestedStructure("current").
               add("value",ScalarType.pvDouble).
               endNested().
            createStructure();
        PVStructure pvStructure = pvDataCreate.createPVStructure(topStructure);
        PowerSupplyRecord pvRecord = new PowerSupplyRecord(recordName,pvStructure);
        if(!pvRecord.init()) return null;
        return pvRecord;
    }
    public PowerSupplyRecord(String recordName,PVStructure pvStructure)
    {
        super(recordName,pvStructure);
    }
    
    private boolean init()
    {
        PVStructure pvStructure = getPVRecordStructure().getPVStructure();
        PVField pvField;
        boolean result;
        pvField = pvStructure.getSubField(PVField.class,"alarm");
        if(pvField==null) {
                System.out.println("no alarm");
                return false;
        }
        result = pvAlarm.attach(pvField);
        if(!result) {
                System.out.println("no alarm");
                return false;
        }
        pvCurrent = pvStructure.getSubField(PVDouble.class,"current.value");
        if(pvCurrent==null) {
                System.out.println("no current");
                return false;
        }
        pvVoltage = pvStructure.getSubField(PVDouble.class,"voltage.value");
        if(pvVoltage==null) {
                System.out.println("no current");
                return false;
        }
        pvPower = pvStructure.getSubField(PVDouble.class,"power.value");
        if(pvPower==null) {
                System.out.println("no power");
                return false;
        }
        alarm.setMessage("bad voltage");
        alarm.setSeverity(AlarmSeverity.MAJOR);
        pvAlarm.set(alarm);
        return true;
    }

    
    public void process()
    {
        double voltage = pvVoltage.get();
        double power = pvPower.get();
        if(voltage<1e-3 && voltage>-1e-3) {
            alarm.setMessage("bad voltage");
            alarm.setSeverity(AlarmSeverity.MAJOR);
            pvAlarm.set(alarm);
            throw new RuntimeException("bad voltage exception");
        }
        double current = power/voltage;
        pvCurrent.put(current);
        pvAlarm.get(alarm);
        if(alarm.getSeverity()!=AlarmSeverity.NONE) {
            alarm.setMessage("");
            alarm.setSeverity(AlarmSeverity.NONE);
            pvAlarm.set(alarm);
        }
        super.process();
    }
}

