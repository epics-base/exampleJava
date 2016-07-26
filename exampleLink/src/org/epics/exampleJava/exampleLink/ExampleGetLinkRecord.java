/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleLink;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvdata.factory.StandardPVFieldFactory;
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
