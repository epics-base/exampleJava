package org.epics.exampleDatabase;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;

public class ExampleHelloRPC implements RPCService {
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    
    
    private final static Structure resultStructure = fieldCreate.createFieldBuilder().
            add("value",ScalarType.pvString).createStructure();
    
    private final static PVStructure pvResult = pvDataCreate
            .createPVStructure(resultStructure);
            
    
    public PVStructure request(PVStructure args) throws RPCRequestException
    {
       
        PVString pvFrom = args.getSubField(PVString.class,"value");
        if (pvFrom == null)
            throw new RPCRequestException(StatusType.ERROR,
                    "PVString field with name 'value' expected.");
        
        PVString pvTo = pvResult.getSubField(PVString.class,"value");
        pvTo.put("Hallo " + pvFrom.get());
        return pvResult;
    }

}
