/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 *
 */

package org.epics.exampleJava.arrayPerformance;

import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
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

public class LongArrayMonitor implements RunnableReady {


    private static ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    private AtomicBoolean runStop = new AtomicBoolean(false);
    private AtomicBoolean runReturn = new AtomicBoolean(false);
    private String providerName;
    private String channelName;

    public LongArrayMonitor(String providerName,String channelName)
    {
        this.providerName = providerName;
        this.channelName = channelName;
        threadCreate.create("longArrayMonitor",ThreadPriority.getJavaPriority(ThreadPriority.middle), this);
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
        PvaClientMonitor monitor = pva.channel(channelName,providerName,5.0).monitor("value,timeStamp,alarm");
        TimeStamp timeStamp = TimeStampFactory.create();
        TimeStamp timeStampLast = TimeStampFactory.create();
        timeStampLast.getCurrentTime();
        LongArrayData longData = new LongArrayData();
        int nElements = 0;
        int nSinceLastReport = 0;
        long first = 0;
        long last = 0;
        threadReady.ready();
        while(true) {
            if(runStop.get()) {
                runReturn.compareAndSet(false, true);
                return;
            }
            if(!monitor.waitEvent(0.0)) {
                System.out.println("waitEvent returned false. Why???");
                continue;
            }
            PvaClientMonitorData pvaData = monitor.getData();
            PVStructure pvStructure = pvaData.getPVStructure();
            PVLongArray pvValue = pvStructure.getSubField(PVLongArray.class,"value");
            int len = pvValue.getLength();
            if(len>0) {
                pvValue.get(0, len,longData);
                long[] value = longData.data;
                first = value[0];
                last = value[len-1];
                if(first!=last) System.out.println("error first=" + first +" last=" + last);
            } else {
                System.out.println("len is 0");
            }
            nElements += len;
            timeStamp.getCurrentTime();
            double diff = timeStamp.diff(timeStamp,timeStampLast);
            if(diff>=1.0) {
                String out = " monitors/sec " + nSinceLastReport;
                if(len>0) out += " first " + first + " last " + last ;
                out += " changed " + pvaData.getChangedBitSet().toString();
                out += " overrun " + pvaData.getOverrunBitSet().toString();

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
                System.out.println(out);
                timeStampLast.getCurrentTime();
                nSinceLastReport = 0;
                nElements = 0;
            }
            ++nSinceLastReport;
            monitor.releaseEvent();
        }
    }
}
