package org.epics.exampleJava.arrayPerformance;

import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.misc.RunnableReady;
import org.epics.pvdata.misc.ThreadCreate;
import org.epics.pvdata.misc.ThreadCreateFactory;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.ThreadReady;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdatabase.PVRecord;

public class ArrayPerformance extends PVRecord implements RunnableReady {
    private static ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    private AtomicBoolean runStop = new AtomicBoolean(false);
    private AtomicBoolean runReturn = new AtomicBoolean(false);

    private int size;
    private long delayMilli;
    

    public static ArrayPerformance  create(String recordName,int size,double delay)
    {
        PVStructure pvs = StandardPVFieldFactory.getStandardPVField().scalarArray(
            ScalarType.pvLong,"value,timeStamp.alarm");
        return  new ArrayPerformance(recordName,pvs,size,delay);
    }

    private ArrayPerformance(String recordName,PVStructure pvStructure,int size,double delay) {
        super(recordName,pvStructure);
        this.size = size;
        delayMilli = (long)(delay*1000);

    }

    public void startThread()
    {
        
        threadCreate.create("arrayPerformance",ThreadPriority.getJavaPriority(ThreadPriority.middle), this);
    }

    public void process()
    {
        super.process();
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

    @Override
    public void run(ThreadReady threadReady) {
        PVStructure pvStructure = getPVRecordStructure().getPVStructure();
        PVLongArray pvValue = pvStructure.getSubField(PVLongArray.class, "value");
        TimeStamp timeStamp = TimeStampFactory.create();
        TimeStamp timeStampLast = TimeStampFactory.create();
        timeStampLast.getCurrentTime();
        int nSinceLastReport = 0;
        long value = 0;
        threadReady.ready();
        while(true) {
            if(runStop.get()) {
                runReturn.compareAndSet(false, true);
                return;
            }
            if(delayMilli>0) {
                try {
                    Thread.sleep(delayMilli);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timeStamp.getCurrentTime();
            double diff = timeStamp.diff(timeStamp,timeStampLast);
            if(diff>=1.0) {
                String out ="arrayPerformance value " + value + " time " + diff;
                double iterations = nSinceLastReport;
                iterations /= diff;
                if(iterations>10.0e9) {
                    iterations /= 1e9;
                    out += " gigaIterations/sec " +iterations;
                } else if(iterations>10.0e6) {
                    iterations /= 1e6;
                    out += "iterations/sec " +iterations;
                } else if(iterations>10.0e3) {
                    iterations /= 1e3;
                    out +=" kiloIterations/sec " + iterations;
                } else  {
                    out +=" Iterations/sec " + iterations;
                }
                double elementsPerSecond = size*nSinceLastReport;
                elementsPerSecond /= diff;
                if(elementsPerSecond>10.0e9) {
                    elementsPerSecond /= 1e9;
                    out += " gigaElements/sec " +elementsPerSecond;
                } else if(elementsPerSecond>10.0e6) {
                    elementsPerSecond /= 1e6;
                    out += " megaElements/sec " +elementsPerSecond;
                } else if(elementsPerSecond>10.0e3) {
                    elementsPerSecond /= 1e3;
                    out += " kiloElements/sec " +elementsPerSecond;
                } else  {
                    out += " Elements/sec " +elementsPerSecond;
                }
                System.out.println(out);
                timeStampLast.getCurrentTime();
                nSinceLastReport = 0;
            }
            ++nSinceLastReport;
            lock();
            try {
                if(getTraceLevel()>1) {
                    System.out.print("arrayPerformance size " + size);
                    System.out.println(" value " +value +1);
                }
                long[] data = new long[size];
                for(int i=0; i<data.length; ++i) data[i] = value;
                value += 1.0;
                beginGroupPut();
                pvValue.put(0,size, data, 0);
                pvValue.setLength(size);
                process();
                endGroupPut();
            }catch(Exception ex){
                throw ex;
            } finally {
                unlock();
            }
        }
    }
}
