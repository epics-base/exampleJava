package org.epics.exampleLink;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaccess.client.Channel;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVRecord;

public class ExampleLinkRecord extends PVRecord
    implements  MonitorRequester, Requester
{
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static final CreateRequest createRequest = CreateRequest.create();
    private static Convert convert = ConvertFactory.getConvert();
    
    private String channelName = null;
    private PVDoubleArray pvValue = null;
    private PvaClientChannel pvaClientChannel = null;
    private Monitor monitor = null;

    public static PVRecord create(String recordName,String channelName)
    {
        PVStructure pvStructure = standardPVField.scalarArray(ScalarType.pvDouble, "timeStamp");
        ExampleLinkRecord pvRecord = new ExampleLinkRecord(recordName,channelName,pvStructure);
        if(!pvRecord.init()) return null;
        return pvRecord;
    }
    public ExampleLinkRecord(String recordName,String channelName,PVStructure pvStructure)
    {
        super(recordName,pvStructure);
        this.channelName = channelName;
    }
    
    private boolean init()
    {
        PVStructure pvStructure = getPVRecordStructure().getPVStructure();
        pvValue = pvStructure.getSubField(PVDoubleArray.class,"value");
        if(pvValue == null) {
            return false;
        }
        PvaClient pva = PvaClient.get();
        pvaClientChannel = pva.channel(channelName);
        Channel channel = pvaClientChannel.getChannel();
        PVStructure pvRequest = createRequest.createRequest("value");
        monitor = channel.createMonitor(this, pvRequest);
        return true;
    }
    
    public void process()
    {
        super.process();
    }
 
    public String getRequesterName() {
        return channelName;
    }
   
    public void message(String message, MessageType messageType) {
        System.out.println("Why is ExampleLink::message called");
    }
    
    public void monitorConnect(
            Status status,
            Monitor monitor,
            Structure structure)
    {
        monitor.start();
    }
   
    public void monitorEvent(Monitor monitor)
    {
        while(true) {
            MonitorElement monitorElement = monitor.poll();
            if(monitorElement==null) break;
            PVStructure pvStructure = monitorElement.getPVStructure();
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
            
            monitor.release(monitorElement);
        } 
    }
    
    public void unlisten(Monitor monitor) {
        destroy();
    }

    
    public void destroy()
    {
        monitor.destroy();
        pvaClientChannel.destroy();
         super.destroy();   
    }
}
