/*ExamplePvaClientMonitor.java */
/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
/**
 * @author mrk
 */
package org.epics.exampleJava.powerSupply;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvaClient.PvaClientPutGet;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVStructure;

public class PowerSupplyClient
{
    public static void main( String[] args )
    {
        PvaClient pva= PvaClient.get();
        try {
        	PvaClientChannel pvaChannel = pva.channel("powerSupply");
        	PvaClientPutGet putGet =pvaChannel.createPutGet(
        	    "putField(power.value,voltage.value)getField()");
        	putGet.connect();
        	PvaClientPutData putData = putGet.getPutData();
        	PvaClientGetData getData = putGet.getGetData();
        	PVStructure pvStructure = putData.getPVStructure();
        	PVDouble putPower = pvStructure.getSubField(PVDouble.class,"power.value");
        	PVDouble putVoltage = pvStructure.getSubField(PVDouble.class,"voltage.value");
        	putPower.put(5.0);
            putVoltage.put(5.0);
            putGet.putGet();
            
            pvStructure = getData.getPVStructure();
            PVDouble getPower = pvStructure.getSubField(PVDouble.class,"power.value");
            PVDouble getVoltage = pvStructure.getSubField(PVDouble.class,"voltage.value");
            PVDouble getCurrent = pvStructure.getSubField(PVDouble.class,"current.value");
            if(getPower.get() == 5.0) System.out.println("returned correct power");
            if(getVoltage.get() == 5.0) System.out.println("returned correct voltage");
            if(getCurrent.get() == 1.0) System.out.println("returned correct current");

            putPower.put(10.0);
            putGet.putGet();
            
            pvStructure = getData.getPVStructure();
            getPower = pvStructure.getSubField(PVDouble.class,"power.value");
            getVoltage = pvStructure.getSubField(PVDouble.class,"voltage.value");
            getCurrent = pvStructure.getSubField(PVDouble.class,"current.value");
            if(getPower.get() == 10.0) System.out.println("returned correct power");
            if(getVoltage.get() == 5.0) System.out.println("returned correct voltage");
            if(getCurrent.get() == 2.0) System.out.println("returned correct current");

            putPower.put(5.0);
            putVoltage.put(0.0);
            System.out.println("NOTE!!! an exception will be thrown because voltage is 0\n");
            putGet.putGet();
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
            System.exit(1);;
        }
        System.out.println("_____powerSupplyClient done_______");
        pva.destroy();
    }

}
