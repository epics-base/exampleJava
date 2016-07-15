/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.exampleJava.exampleLink;

import org.epics.pvaClient.*;
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

public class ExampleGetLinkRecord extends PVRecord
{
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();

    private PVDoubleArray pvValue = null;
    private PvaClientGet pvaClientGet = null;

    public static PVRecord create(PvaClient pva,String recordName,String provider,String channelName)
    {
        PVStructure pvStructure = standardPVField.scalarArray(ScalarType.pvDouble, "timeStamp");
        ExampleGetLinkRecord pvRecord = new ExampleGetLinkRecord(recordName,pvStructure);
        if(!pvRecord.init(pva,channelName,provider)) return null;
        return pvRecord;
    }
    public ExampleGetLinkRecord(String recordName,PVStructure pvStructure)
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
        pvaClientGet = pva.channel(channelName,provider).createGet();
        return true;
    }


    public void process()
    {
        pvaClientGet.get();
        double[] value = pvaClientGet.getData().getDoubleArray();
        int length = value.length;
        pvValue.put(0, length, value, 0);
        super.process();
    }

}
