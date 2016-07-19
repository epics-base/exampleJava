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

public class ExamplePutLinkRecord extends PVRecord
{
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static Convert convert = ConvertFactory.getConvert();


    private PVDoubleArray pvValue = null;
    private PvaClientPut pvaClientPut = null;

    public static PVRecord create(PvaClient pva,String recordName,String provider,String channelName)
    {
        PVStructure pvStructure = standardPVField.scalarArray(ScalarType.pvDouble, "timeStamp");
        ExamplePutLinkRecord pvRecord = new ExamplePutLinkRecord(recordName,pvStructure);
        if(!pvRecord.init(pva,channelName,provider)) return null;
        return pvRecord;
    }
    public ExamplePutLinkRecord(String recordName,PVStructure pvStructure)
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
        pvaClientPut = pva.channel(channelName,provider).createPut();
        return true;
    }


    public void process()
    {
        convert.copy(pvValue,pvaClientPut.getData().getValue());
        pvaClientPut.put();
        super.process();
    }

}
