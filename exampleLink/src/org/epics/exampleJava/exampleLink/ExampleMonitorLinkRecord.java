/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.exampleJava.exampleLink;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdatabase.PVRecord;

public class ExampleMonitorLinkRecord extends PVRecord
implements  PvaClientMonitorRequester
{
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static Convert convert = ConvertFactory.getConvert();

    private PVDoubleArray pvValue = null;

    public static PVRecord create(PvaClient pva,String recordName,String provider,String channelName)
    {
        PVStructure pvStructure = standardPVField.scalarArray(ScalarType.pvDouble, "timeStamp");
        ExampleMonitorLinkRecord pvRecord = new ExampleMonitorLinkRecord(recordName,pvStructure);
        if(!pvRecord.init(pva,channelName,provider)) return null;
        return pvRecord;
    }
    public ExampleMonitorLinkRecord(String recordName,PVStructure pvStructure)
    {
        super(recordName,pvStructure);
    }

    private boolean init(PvaClient pva,String channelName,String provider)
    {
        PVStructure pvStructure = getPVRecordStructure().getPVStructure();
        pvValue = pvStructure.getSubField(PVDoubleArray.class,"value");
        if(pvValue == null) {
            return false;
        }
        PvaClientChannel pvaClientChannel = pva.channel(channelName,provider,0.0);
        pvaClientChannel.monitor("value",this);
        return true;
    }

    @Override
    public void event(PvaClientMonitor monitor) {
        while(monitor.poll()) {
            PVStructure pvStructure = monitor.getData().getPVStructure();
            PVDoubleArray pvDoubleArray = pvStructure.getSubField(PVDoubleArray.class,"value");
            if(pvDoubleArray==null) throw new RuntimeException("value is not a double array");

            lock();
            try {
                beginGroupPut();
                convert.copy(pvDoubleArray,pvValue);
                process();
                endGroupPut();
            } finally {
                unlock();
            }
            monitor.releaseEvent();
        } 
    }

    public void process()
    {
        super.process();
    }

}
