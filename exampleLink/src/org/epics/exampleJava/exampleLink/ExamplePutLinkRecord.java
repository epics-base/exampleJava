/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleLink;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientChannelStateChangeRequester;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.property.Alarm;
import org.epics.pvdata.property.AlarmSeverity;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVAlarmFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdata.pv.*;
import org.epics.pvdatabase.PVRecord;

public class ExamplePutLinkRecord extends PVRecord
    implements PvaClientChannelStateChangeRequester,PvaClientPutRequester
{
   


    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static Convert convert = ConvertFactory.getConvert();
    private boolean channelConnected = false;
    private boolean isPutConnected = false;
    private boolean isPutDone = true;
    private boolean setAlarmGood = false;
    private PVField pvValue = null;
    private PVStructure pvAlarmField = null;
    private PVAlarm pvAlarm = PVAlarmFactory.create();
    private Alarm alarm = new Alarm();
    private PvaClientChannel pvaClientChannel = null;
    private PvaClientPut pvaClientPut = null;

    public static PVRecord create(PvaClient pva,String recordName,String provider,String channelName,boolean valueIsArray)
    {
        PVStructure pvStructure = null;
        if(valueIsArray) {
            pvStructure = standardPVField.scalarArray(ScalarType.pvDouble, "timeStamp,alarm");
        } else {
            pvStructure = standardPVField.scalar(ScalarType.pvDouble, "timeStamp,alarm");
        }
        ExamplePutLinkRecord pvRecord = new ExamplePutLinkRecord(recordName,pvStructure);
        if(!pvRecord.init(pva,channelName,provider)) return null;
        return pvRecord;
    }
    public ExamplePutLinkRecord(String recordName,PVStructure pvStructure)
    {
        super(recordName,pvStructure);
    }

    private boolean init(PvaClient pvaClient,String channelName,String providerName)
    {
        PVStructure pvStructure = getPVRecordStructure().getPVStructure();
        pvValue = pvStructure.getSubField("value");
        if(pvValue == null) {
            throw new RuntimeException("no value field");
        }
        pvAlarmField = pvStructure.getSubField(PVStructure.class,"alarm");
        if(pvAlarmField==null) {
            throw new RuntimeException("no alarm field");
        }
        if(!pvAlarm.attach(pvAlarmField)) {
            throw new RuntimeException("bad alarm field");
        }
        pvaClientChannel = pvaClient.createChannel(channelName,providerName);
        pvaClientChannel.setStateChangeRequester(this);
        pvaClientChannel.issueConnect();
        return true;
        
    }
    
    /* (non-Javadoc)
     * @see org.epics.pvaClient.PvaClientChannelStateChangeRequester#channelStateChange(org.epics.pvaClient.PvaClientChannel, boolean)
     */
    public void channelStateChange(PvaClientChannel channel, boolean isConnected) {
        channelConnected = isConnected;
        if(isConnected) {
            setAlarmGood = true;
            if(pvaClientPut==null) {
                pvaClientPut = pvaClientChannel.createPut("value");
                pvaClientPut.setRequester(this);
                pvaClientPut.issueConnect();
            }
            return;
        }
        lock();
        try {
            beginGroupPut();
            process();
            endGroupPut();
        } finally {
           unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvaClient.PvaClientPutRequester#channelPutConnect(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientPut)
     */
    public void channelPutConnect(Status status, PvaClientPut pvaClientPut) {
        if(status.isOK()) {
            isPutConnected = true;
            return;
        }
        lock();
        isPutConnected = false;
        try {
            beginGroupPut();
            process();
            endGroupPut();
        } finally {
            unlock();
        }

    }
    /* (non-Javadoc)
     * @see org.epics.pvaClient.PvaClientPutRequester#getDone(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientPut)
     */
    public void getDone(Status status, PvaClientPut pvaClientPut) {
        // Nothing to do
    }
    /* (non-Javadoc)
     * @see org.epics.pvaClient.PvaClientPutRequester#putDone(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientPut)
     */
    public void putDone(Status status, PvaClientPut pvaClientPut) {
        if(status.isOK()) {
            isPutDone = true;
            return;
        }
        lock();
        isPutDone = false;
        try {
            beginGroupPut();
            process();
            endGroupPut();
        } finally {
            unlock();
        }
        isPutDone = true;
    }
    

    public void process()
    {
        if(!channelConnected)
        {
            alarm.setMessage("disconnected");
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            setAlarmGood = true;
        } else if(!isPutConnected) 
        {
            alarm.setMessage("channelPut not connected");
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            setAlarmGood = true;
        } else if(!isPutDone) 
        {
            alarm.setMessage("previous put not done");
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            setAlarmGood = true;
        } else {
            try {
                convert.copy(pvValue,pvaClientPut.getData().getValue());
                pvaClientPut.put();
                if(setAlarmGood) {
                    setAlarmGood = false;
                    alarm.setMessage("connected");
                    alarm.setSeverity(AlarmSeverity.NONE);
                    pvAlarm.set(alarm);
                }
            } catch (Exception e) {
                alarm.setMessage(e.getMessage());
                alarm.setSeverity(AlarmSeverity.INVALID);
                pvAlarm.set(alarm);
                setAlarmGood = true;
            }
        }
        super.process();
    }
}
