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
import org.epics.pvaClient.*;
import org.epics.pvaClient.PvaClientGetRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.property.Alarm;
import org.epics.pvdata.property.AlarmSeverity;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVAlarmFactory;
import org.epics.pvdata.pv.*;
import org.epics.pvdatabase.PVRecord;

public class ExampleGetLinkRecord extends PVRecord
    implements PvaClientChannelStateChangeRequester,PvaClientGetRequester
{
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static Convert convert = ConvertFactory.getConvert();
    private boolean channelConnected = false;
    private boolean isGetConnected = false;
    private boolean isGetDone = true;
    private boolean setAlarmGood = false;
    private PVField pvValue = null;
    private PVStructure pvAlarmField = null;
    private PVAlarm pvAlarm = PVAlarmFactory.create();
    private Alarm alarm = new Alarm();
    private PVAlarm linkPVAlarm = PVAlarmFactory.create();
    private Alarm linkAlarm = new Alarm();
    private PvaClientChannel pvaClientChannel = null;
    private PvaClientGet pvaClientGet = null;

    public static PVRecord create(PvaClient pva,String recordName,String provider,String channelName,boolean valueIsArray)
    {
        PVStructure pvStructure = null;
        if(valueIsArray) {
            pvStructure = standardPVField.scalarArray(ScalarType.pvDouble, "timeStamp,alarm");
        } else {
            pvStructure = standardPVField.scalar(ScalarType.pvDouble, "timeStamp,alarm");
        }
        ExampleGetLinkRecord pvRecord = new ExampleGetLinkRecord(recordName,pvStructure);
        if(!pvRecord.init(pva,channelName,provider)) return null;
        return pvRecord;
    }
    public ExampleGetLinkRecord(String recordName,PVStructure pvStructure)
    {
        super(recordName,pvStructure);
    }

    private boolean init(PvaClient pvaClient,String channelName,String providerName)
    {
        PVStructure pvStructure = getPVRecordStructure().getPVStructure();
        pvValue = pvStructure.getSubField("value");
        if(pvValue==null) {
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
            if(pvaClientGet==null) {
                pvaClientGet = pvaClientChannel.createGet("value,alarm");
                pvaClientGet.setRequester(this);
                pvaClientGet.issueConnect();
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
     * @see org.epics.pvaClient.PvaClientGetRequester#channelGetConnect(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientGet)
     */
    public void channelGetConnect(Status status, PvaClientGet pvaClientGet) {
        if(status.isOK()) {
            isGetConnected = true;
            return;
        }
        lock();
            isGetConnected = false;
            try {
                beginGroupPut();
                process();
                endGroupPut();
            } finally {
               unlock();
            }
    }
    /* (non-Javadoc)
     * @see org.epics.pvaClient.PvaClientGetRequester#getDone(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientGet)
     */
    public void getDone(Status status, PvaClientGet pvaClientGet) {
        if (status.isOK()) {
            isGetDone = true;
            return;
        }
        lock();
        isGetDone = false;
        try {
            beginGroupPut();
            process();
            endGroupPut();
        } finally {
            unlock();
        }
    }

    public void process()
    {
        if(!channelConnected)
        {
            alarm.setMessage("disconnected");
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            setAlarmGood = true;
        } else if(!isGetConnected) 
        {
            alarm.setMessage("channelGet not connected");
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            setAlarmGood = true;
        } else if(!isGetDone) 
        {
            alarm.setMessage("previous get not done");
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            setAlarmGood = true;
        } else {
            try {
                if(setAlarmGood) {
                    setAlarmGood = false;
                    alarm.setMessage("connected");
                    alarm.setSeverity(AlarmSeverity.NONE);
                    pvAlarm.set(alarm);
                }
                isGetDone = false;
                pvaClientGet.get();
                PVStructure pvStructure = pvaClientGet.getData().getPVStructure();
                PVField linkValue = pvStructure.getSubField("value");
                convert.copy(linkValue,pvValue);
                PVStructure linkAlarmField = pvStructure.getSubField(PVStructure.class,"alarm");
                if(linkAlarmField!=null) {
                    boolean setAlarm = false;
                    if(linkPVAlarm.attach(linkAlarmField)) {
                        linkPVAlarm.get(linkAlarm);
                        if(!alarm.getMessage().equals(linkAlarm.getMessage())) {
                            alarm.setMessage(linkAlarm.getMessage());
                            setAlarm = true;
                        }
                        if(alarm.getSeverity()!=(linkAlarm.getSeverity())) {
                            alarm.setSeverity(linkAlarm.getSeverity());
                            setAlarm = true;
                        }
                        if(alarm.getStatus()!=(linkAlarm.getStatus())) {
                            alarm.setStatus(linkAlarm.getStatus());
                            setAlarm = true;
                        }
                        if(setAlarm) pvAlarm.set(alarm);
                    }
                }
                super.process();
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
