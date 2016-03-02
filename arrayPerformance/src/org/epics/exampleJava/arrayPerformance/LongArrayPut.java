package org.epics.exampleJava.arrayPerformance;

import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.misc.RunnableReady;
import org.epics.pvdata.misc.ThreadCreate;
import org.epics.pvdata.misc.ThreadCreateFactory;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.*;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStructure;

public class LongArrayPut implements RunnableReady {

    
    private static ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    private String providerName;
    private String channelName;
    private int arraySize;
    private int iterBetweenCreateChannel;
    private int iterBetweenCreateChannelPut;
    private double delayTime;
    private AtomicBoolean runStop = new AtomicBoolean(false);
    private AtomicBoolean runReturn = new AtomicBoolean(false);
    

    public LongArrayPut(
            String providerName, String channelName,
         int arraySize,
         int iterBetweenCreateChannel,int iterBetweenCreateChannelPut,double delayTime)
    {
        this.providerName = providerName;
        this.channelName = channelName;
        this.arraySize = arraySize;
        this.iterBetweenCreateChannel = iterBetweenCreateChannel;
        this.iterBetweenCreateChannelPut = iterBetweenCreateChannelPut;
        this.delayTime = delayTime;
        threadCreate.create(
            "longArrayPut",ThreadPriority.getJavaPriority(ThreadPriority.middle), this);
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
        PvaClient pva= PvaClient.get();
        PvaClientChannel pvaChannel = pva.channel(channelName,providerName,5.0);
        PvaClientPut pvaPut = pvaChannel.createPut("value");
        TimeStamp timeStamp = TimeStampFactory.create();
        TimeStamp timeStampLast = TimeStampFactory.create();
        timeStampLast.getCurrentTime();
        int numChannelPut = 0;
        int numChannelCreate = 0;
        int nElements = 0;
        long delayMilli = (long)(delayTime*1000);
        PvaClientPutData putData = pvaPut.getData();
        PVStructure pvStructure = putData.getPVStructure();
        PVLongArray pvLongArray = pvStructure.getSubField(PVLongArray.class,"value");
        pvLongArray.setCapacity(arraySize);
        BitSet bitSet = putData.getChangedBitSet();
        threadReady.ready();
        while(true) {
            if(runStop.get()) {
                runReturn.compareAndSet(false, true);
                return;
            }
            nElements += arraySize;
            long[] data = new long[arraySize];
            for(int i=0; i< arraySize; ++i) data[i] = numChannelPut;
            pvLongArray.put(0,arraySize, data,0);
            pvaPut.put();
            bitSet.set(pvLongArray.getNextFieldOffset());
            timeStamp.getCurrentTime();
            double diff = timeStamp.diff(timeStamp,timeStampLast);
            if(diff>=1.0) {
                timeStampLast.getCurrentTime();
                String out = "put " + numChannelPut;
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
                if(iterBetweenCreateChannelPut!=0) out += " numChannelPut " + numChannelPut;
                if(iterBetweenCreateChannel!=0) out+= " numChannelCreate " + numChannelCreate;
                System.out.println(out);
                nElements = 0;
            }
            if(delayMilli>0) {
                try {
                    Thread.sleep(delayMilli);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ++numChannelPut;
            boolean createPut = false;
            if(iterBetweenCreateChannelPut!=0) {
                if(numChannelPut>=iterBetweenCreateChannelPut) createPut = true;
            }
            if(createPut) {
                 pvaPut.destroy();
                 pvaPut = pvaChannel.createPut("value,timeStamp,alarm");
                 numChannelPut = 0;
            }
            ++numChannelCreate;
            if(iterBetweenCreateChannel!=0) {
                if(numChannelCreate>=iterBetweenCreateChannel) {
                    pvaChannel.destroy();
                    pvaChannel = pva.createChannel(channelName,providerName);
                    pvaPut = pvaChannel.createPut("value,timeStamp,alarm");
                    numChannelCreate = 0;
                }
            } 
        }
    }
}
