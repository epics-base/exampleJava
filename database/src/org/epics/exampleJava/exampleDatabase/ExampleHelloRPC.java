// Copyright information and license terms for this software can be
// found in the file LICENSE that is included with the distribution

/**
 * @author mrk
 * @date 2013.07.24
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

public class ExampleHelloRPC extends PVRecord {
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();


    private final static Structure resultStructure = 
            fieldCreate.createFieldBuilder().
            add("value",ScalarType.pvString).createStructure();

    private final static PVStructure pvResult = pvDataCreate
            .createPVStructure(resultStructure);


    static class RPCServiceImpl implements RPCService {

        private ExampleHelloRPC pvRecord;

        RPCServiceImpl(ExampleHelloRPC record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            PVString pvFrom = args.getSubField(PVString.class,"value");
            if (pvFrom == null)
                throw new RPCRequestException(StatusType.ERROR,
                        "PVString field with name 'value' expected.");
            pvRecord.put(pvFrom);
            return pvResult;

        }
    }

    public static ExampleHelloRPC create(String recordName)
    {

        ExampleHelloRPC pvRecord = new ExampleHelloRPC(recordName,pvResult);
        PVDatabase master = PVDatabaseFactory.getMaster();
        master.addRecord(pvRecord);
        return pvRecord;
    }

    public ExampleHelloRPC(String recordName, PVStructure pvStructure) {
        super(recordName, pvStructure);
        process();
    }
    
    public void put(PVString pvFrom)
    {
        lock();
        beginGroupPut();
        PVString pvTo = pvResult.getSubField(PVString.class,"value");
        pvTo.put("Hello " + pvFrom.get());
        process();
        endGroupPut();
        unlock();
    }


    public Service getService(PVStructure pvRequest)
    {
        return new RPCServiceImpl(this);
    }

}
