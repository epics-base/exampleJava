/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 * @since 2013.07.24
 */

package org.epics.exampleJava.exampleDatabase;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvaccess.server.rpc.Service;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;

/**
 * A PVRecord that implements a hello service accessed via a channelRPC request.
 *
 */
public class ExampleHelloRPC extends PVRecord implements RPCService{
    /**
     * Create an instance of ExampleHelloRecord.
     * @param recordName The name of the record.
     * @return The new instance.
     */
    public static ExampleHelloRPC create(String recordName)
    {
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        Structure resultStructure = 
                fieldCreate.createFieldBuilder().
                add("value",ScalarType.pvString).createStructure();
        PVStructure pvTop = pvDataCreate
                .createPVStructure(resultStructure);
        ExampleHelloRPC pvRecord = new ExampleHelloRPC(recordName,pvTop);
        PVDatabase master = PVDatabaseFactory.getMaster();
        master.addRecord(pvRecord);
        return pvRecord;
    }
    
    /**
     * Get the ExampleRPC service.
     */
    public Service getService(PVStructure pvRequest)
    {
        return this;
    }
    
    /**
     * Process a request from the client
     *
     * @param args The request from the client
     * @return The result.
     */
    public PVStructure request(PVStructure args) throws RPCRequestException
    {
        PVString pvFrom = args.getSubField(PVString.class,"value");
        if (pvFrom == null)
            throw new RPCRequestException(StatusType.ERROR,
                    "PVString field with name 'value' expected.");
        return put(pvFrom);

    }
    private PVStructure pvTop;

    private  ExampleHelloRPC(String recordName, PVStructure pvTop) {
        super(recordName, pvTop);
        this.pvTop = pvTop;
    }
    
    private PVStructure put(PVString pvFrom)
    {
        lock();
        beginGroupPut();
        PVString pvTo = pvTop.getSubField(PVString.class,"value");
        pvTo.put("Hello " + pvFrom.get());
        process();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Unexpected exception " + e.getMessage());
        }
        endGroupPut();
        PVStructure pvResult =  PVDataFactory.getPVDataCreate().createPVStructure(pvTop);
        unlock();
        return pvResult;
    }
    

}
