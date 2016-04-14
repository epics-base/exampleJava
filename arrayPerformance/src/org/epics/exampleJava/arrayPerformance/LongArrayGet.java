package org.epics.exampleJava.arrayPerformance;

import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvdata.misc.RunnableReady;
import org.epics.pvdata.misc.ThreadCreate;
import org.epics.pvdata.misc.ThreadCreateFactory;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.ThreadReady;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStructure;

public class LongArrayGet implements RunnableReady {


    private static ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    private String providerName;
    private String channelName;
    private int iterBetweenCreateChannel;
    private int iterBetweenCreateChannelGet;
    private double delayTime;
    private AtomicBoolean runStop = new AtomicBoolean(false);
    private AtomicBoolean runReturn = new AtomicBoolean(false);


    public LongArrayGet(
            String providerName,
            String channelName,
            int iterBetweenCreateChannel,
            int iterBetweenCreateChannelGet,
            double delayTime)
    {
        this.providerName = providerName;
        this.channelName = channelName;
        this.iterBetweenCreateChannel = iterBetweenCreateChannel;
        this.iterBetweenCreateChannelGet = iterBetweenCreateChannelGet;
        this.delayTime = delayTime;
        threadCreate.create(
                "longArrayGet",ThreadPriority.getJavaPriority(ThreadPriority.middle), this);
    }

    public void stop()
    {
        if(!runStop.compareAndSet(false, true)) return;
        while(true) {
            if(runReturn.get()) return;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run(ThreadReady threadReady)
    {
        PvaClient pva= PvaClient.get(providerName);
        PvaClientChannel pvaChannel = pva.channel(channelName,providerName,5.0);
        PvaClientGet pvaGet = pvaChannel.createGet("value,alarm,timeStamp");
        TimeStamp timeStamp = TimeStampFactory.create();
        TimeStamp timeStampLast = TimeStampFactory.create();
        timeStampLast.getCurrentTime();
        int numChannelGet = 0;
        int numChannelCreate = 0;
        int nElements = 0;
        LongArrayData longData = new LongArrayData();
        long delayMilli = (long)(delayTime*1000);
        threadReady.ready();
        while(true) {
            if(runStop.get()) {
                runReturn.compareAndSet(false, true);
                return;
            }
            pvaGet.get();
            PvaClientGetData pvaData = pvaGet.getData();
            PVStructure pvStructure = pvaData.getPVStructure();
            PVLongArray pvValue = pvStructure.getSubField(PVLongArray.class,"value");
            int len = pvValue.getLength();
            if(len>0) {
                pvValue.get(0, len,longData);
                long[] value = longData.data;
                long first = value[0];
                long last = value[len-1];
                if(first!=last) System.out.println("error first=" + first +" last=" + last);
            }
            nElements += len;
            timeStamp.getCurrentTime();
            double diff = timeStamp.diff(timeStamp,timeStampLast);
            if(diff>=1.0) {
                String out = "get ";
                double elementsPerSec = nElements;
                elementsPerSec /= diff;
                if(elementsPerSec>10.0e9) {
                    elementsPerSec /= 1e9;
                    out += " gigaElements/sec " + elementsPerSec;
                } else if(elementsPerSec>10.0e6) {
                    elementsPerSec /= 1e6;
                    out += " megaElements/sec " + elementsPerSec;
                } else if(elementsPerSec>10.0e3) {
                    elementsPerSec /= 1e3;
                    out += " kiloElements/sec " + elementsPerSec;
                } else  {
                    out += " Elements/sec " + elementsPerSec;
                }
                if(iterBetweenCreateChannelGet!=0) out += " numChannelGet " + numChannelGet;
                if(iterBetweenCreateChannel!=0) out += " numChannelCreate " + numChannelCreate;
                System.out.println(out);
                timeStampLast.getCurrentTime();
                nElements = 0;
            }
            if(delayMilli>0) {
                try {
                    Thread.sleep(delayMilli);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ++numChannelGet;
            boolean createGet = false;
            if(iterBetweenCreateChannelGet!=0) {
                if(numChannelGet>=iterBetweenCreateChannelGet) createGet = true;
            }
            if(createGet) {
                numChannelGet = 0;
                pvaGet.destroy();
                pvaGet = pvaChannel.createGet("value,alarm,timeStamp");
            }
            ++numChannelCreate;
            if(iterBetweenCreateChannel!=0) {
                if(numChannelCreate>=iterBetweenCreateChannel) {
                    pvaChannel.destroy();
                    pvaGet = pvaChannel.createGet("value,alarm,timeStamp");
                    pvaChannel = pva.createChannel(channelName,providerName);
                    numChannelCreate = 0;
                }
            } 
        }
    }
}
