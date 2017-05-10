/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 * @since 2013.07.24
 */
package org.epics.exampleJava.exampleDatabase;

import org.epics.nt.NTEnum;
import org.epics.nt.NTEnumBuilder;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvdata.copy.PVCopy;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;

/**
 * A PVRecord that implements a busy record. It goes busy when a 1 is sent to it an becomes idle when a 0 is sent.
 *
 */
public class ExampleBusyRecord extends PVRecord {
    private static StatusCreate statusCreate = StatusFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();
    private PVStructure pvValue;
    private PVInt pvIndex;
    private volatile ChannelPut channelPut = null;
    private volatile ChannelPutRequester channelPutRequester = null;
    /**
     * Create an instance of ExampleBusyRecord.
     * @param recordName The name of the record.
     * @return The new instance.
     */
    public static PVRecord create(String recordName)
    {
        NTEnumBuilder ntEnumBuilder = NTEnum.createBuilder();
        PVStructure pvStructure = ntEnumBuilder.
                addAlarm().addTimeStamp().
                createPVStructure();
        String[] choices = new String[2];
        choices[0] = "Done";
        choices[1] = "Acquire";
        PVStringArray pvChoices = pvStructure.getSubField(PVStringArray.class,"value.choices");
        pvChoices.put(0, 2, choices, 0);
        PVRecord pvRecord = new ExampleBusyRecord(recordName,pvStructure);
        PVDatabase master = PVDatabaseFactory.getMaster();
        master.addRecord(pvRecord);
        return pvRecord;
    }
   
    private ExampleBusyRecord(String recordName,PVStructure pvStructure) {
        super(recordName,pvStructure);
        pvValue = pvStructure.getSubField(PVStructure.class, "value");
        if(pvValue==null) throw new IllegalArgumentException("value not found");
        pvIndex = pvValue.getSubField(PVInt.class, "index");
        if(pvIndex==null) throw new IllegalArgumentException("value,index not found");
    }
    
    public boolean isAsynRecord()
    {
        return true;
    }

    public void process(
            ChannelPut channelPut,ChannelPutRequester channelPutRequester,boolean block,
            PVCopy pvCopy,PVStructure pvData,BitSet bitSet)
    {
        PVStructure pvs = pvData.getSubField(PVStructure.class, "value");
        if(pvs==null) {
            Status status = statusCreate.createStatus(StatusType.ERROR, "value not found", null);
            channelPutRequester.putDone(status,channelPut);
            return;
        }
        PVInt pvint = pvs.getSubField(PVInt.class, "index");
        if(pvint==null) {
            Status status = statusCreate.createStatus(StatusType.ERROR, "value.index not found", null);
            channelPutRequester.putDone(status,channelPut);
            return;
        }
        boolean callWaitingRequester = false;
        boolean callRequester = true;
        lock();
        try {
            int current = pvIndex.get();
            int newval = pvint.get();
            if(current==newval) {
                if(current==0) {
                    Status status = statusCreate.createStatus(StatusType.WARNING, "record is already Done", null);
                    channelPutRequester.putDone(status,channelPut);
                } else {
                    Status status = statusCreate.createStatus(StatusType.WARNING, "record is already Busy", null);
                    channelPutRequester.putDone(status,channelPut);
                }
                return;
            }
            beginGroupPut();
            pvCopy.updateMaster(pvData,bitSet);
            process();
            endGroupPut();
            if(block&&newval==1) {
                this.channelPut = channelPut;
                this.channelPutRequester = channelPutRequester;
                callRequester = false;
                return;
            } else if(newval==0&&this.channelPut!=null) {
                callWaitingRequester = true;
            }
        } finally {
            unlock();
        }
        if(getTraceLevel()>1) {
            System.out.println("ChannelPutLocal::put recordName " + getRecordName());
        }        
        if(callRequester) channelPutRequester.putDone(okStatus,channelPut);
        if(callWaitingRequester) {
            this.channelPutRequester.putDone(okStatus,this.channelPut);
            this.channelPutRequester = null;
            this.channelPut = null;
        }
    }
}
