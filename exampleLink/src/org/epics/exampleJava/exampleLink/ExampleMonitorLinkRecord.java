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
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.property.Alarm;
import org.epics.pvdata.property.AlarmSeverity;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVAlarmFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVRecord;

public class ExampleMonitorLinkRecord extends PVRecord
implements   PvaClientChannelStateChangeRequester,PvaClientMonitorRequester
{
    
    

    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static Convert convert = ConvertFactory.getConvert();
    private boolean channelConnected = false;
    private boolean monitorConnected = false;
    private boolean setAlarmGood = false;
    private PVField pvValue = null;
    private PVStructure pvAlarmField = null;
    private PVAlarm pvAlarm = PVAlarmFactory.create();
    private Alarm alarm = new Alarm();
    private PVAlarm linkPVAlarm = PVAlarmFactory.create();
    private Alarm linkAlarm = new Alarm();
    private PvaClientChannel pvaClientChannel = null;
    private PvaClientMonitor pvaClientMonitor = null;


    public static PVRecord create(PvaClient pva,String recordName,String provider,String channelName,boolean valueIsArray)
    {
        PVStructure pvStructure = null;
        if(valueIsArray) {
            pvStructure = standardPVField.scalarArray(ScalarType.pvDouble, "timeStamp,alarm");
        } else {
            pvStructure = standardPVField.scalar(ScalarType.pvDouble, "timeStamp,alarm");
        }
        ExampleMonitorLinkRecord pvRecord = new ExampleMonitorLinkRecord(recordName,pvStructure);
        if(!pvRecord.init(pva,channelName,provider)) return null;
        return pvRecord;
    }
    public ExampleMonitorLinkRecord(String recordName,PVStructure pvStructure)
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
    public void channelStateChange(PvaClientChannel channel, boolean isConnected)
    {
        channelConnected = isConnected;
        if(isConnected) {
            setAlarmGood = true;
            if(pvaClientMonitor==null) {
                pvaClientMonitor = pvaClientChannel.createMonitor("value,alarm");
                pvaClientMonitor.setRequester(this);
                pvaClientMonitor.issueConnect();
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
     * @see org.epics.pvaClient.PvaClientMonitorRequester#monitorConnect(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientMonitor, org.epics.pvdata.pv.Structure)
     */
    public void monitorConnect(Status status, PvaClientMonitor pvaClientMonitor, Structure structure) 
    {
        if(status.isOK()) {
            monitorConnected = true;
            return;
        }
        lock();
        monitorConnected = false;
        try {
            beginGroupPut();
            alarm.setMessage(status.getMessage());
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            process();
            endGroupPut();
        } finally {
            unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvaClient.PvaClientMonitorRequester#unlisten(org.epics.pvaClient.PvaClientMonitor)
     */
    public void unlisten(PvaClientMonitor pvaClientMonitor) {
        lock();
        monitorConnected = false;
        try {
            beginGroupPut();
            alarm.setMessage("unlisten was called");
            alarm.setSeverity(AlarmSeverity.INVALID);
            pvAlarm.set(alarm);
            process();
            endGroupPut();
        } finally {
            unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvaClient.PvaClientMonitorRequester#event(org.epics.pvaClient.PvaClientMonitor)
     */
    public void event(PvaClientMonitor pvaClientMonitor) {
        while(pvaClientMonitor.poll()) {
            PVStructure pvStructure = pvaClientMonitor.getData().getPVStructure();
            PVField linkValue = pvStructure.getSubField("value");
            lock();
            try {
                beginGroupPut();
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
                process();
                endGroupPut();
            } finally {
                unlock();
            }
            pvaClientMonitor.releaseEvent();
        } 
    }

    public void process()
    {
        super.process();
    }

}
