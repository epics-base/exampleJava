/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author Dave Hickin
 *
 */

package org.epics.exampleJava.pvDatabaseRPC;

import java.util.ArrayList;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCResponseCallback;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvaccess.server.rpc.RPCServiceAsync;
import org.epics.pvaccess.server.rpc.Service;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVTimeStampFactory;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArrayData;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;

public class ExampleRPCRecord extends PVRecord implements Device.Callback
{
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();

    private static final Structure resultStructure = fieldCreate.
        createFieldBuilder().createStructure();

    private static final Structure pointStructure = fieldCreate.
                createFieldBuilder().
                setId("point_t").
                add("x", ScalarType.pvDouble).
                add("y", ScalarType.pvDouble).
                createStructure();

    private static final Structure pointTopStructure = fieldCreate.
                createFieldBuilder().
                setId("Point").
                add("value", pointStructure).
                add("timeStamp", standardField.timeStamp()).
                createStructure();

    private static final Structure recordStructure = fieldCreate.
                createFieldBuilder().
                add("positionSP", pointTopStructure).
                add("positionRB", pointTopStructure).
                add("state", standardField.enumerated("timeStamp")).
                add("timeStamp", standardField.timeStamp()).
                createStructure();

    private Device device = new Device();

    private PVDouble       pvx;
    private PVDouble       pvy;
    private PVDouble       pvx_rb;
    private PVDouble       pvy_rb;
    private PVInt          pvStateIndex;
    private PVStringArray  pvStateChoices;

    private PVTimeStamp pvTimeStamp    = PVTimeStampFactory.create();
    private PVTimeStamp pvTimeStamp_sp = PVTimeStampFactory.create();
    private PVTimeStamp pvTimeStamp_rb = PVTimeStampFactory.create();
    private PVTimeStamp pvTimeStamp_st = PVTimeStampFactory.create();

    private boolean firstTime = true;

    public void readbackChanged(Point rb)
    {
        lock();
        try {
            TimeStamp timeStamp = TimeStampFactory.create();
            timeStamp.getCurrentTime();
            beginGroupPut();
            pvx_rb.put(rb.x);
            pvy_rb.put(rb.y);
            pvTimeStamp_rb.set(timeStamp);
            pvTimeStamp.set(timeStamp);
            endGroupPut();
        }
        catch (Throwable t)
        {
            unlock();
            throw t;
        }
        unlock();
    }

    public void setpointChanged(Point sp)
    {
        lock();
        try
        {
            TimeStamp timeStamp = TimeStampFactory.create();
            timeStamp.getCurrentTime();
            beginGroupPut();
            pvx.put(sp.x);
            pvy.put(sp.y);
            pvTimeStamp_sp.set(timeStamp);
            pvTimeStamp.set(timeStamp);
            endGroupPut();
        }
        catch (Throwable t)
        {
            unlock();
            throw t;
        }
        unlock();
    }

    public void stateChanged(Device.State state)
    {
        lock();
        try {
            TimeStamp timeStamp = TimeStampFactory.create();
            timeStamp.getCurrentTime();
            beginGroupPut();
            int index = device.getState().ordinal();
            if (index != pvStateIndex.get())
            {
                pvStateIndex.put(index);
                pvTimeStamp_st.set(timeStamp);
            }
            pvTimeStamp.set(timeStamp);
            endGroupPut();
        }
        catch (Throwable t)
        {
            unlock();
            throw t;
        }
        unlock();
    }

    public void scanComplete()
    {
    }

    public void process()
    {
        TimeStamp timeStamp = TimeStampFactory.create();
        timeStamp.getCurrentTime();

        boolean updateSPTimeStamp = firstTime;

        Point newSP = new Point(pvx.get(), pvy.get()); 
        try
        {
            Point sp_initial = device.getPositionSetpoint();
      
            if (!sp_initial.equals(newSP))
            {
                device.setSetpoint(newSP);
            }
        }
        catch (IllegalOperationException o)
        {
            // If write to device fails restore values
            Point sp = device.getPositionSetpoint();
            if (!sp.equals(newSP))
            {
                pvx.put(sp.x);
                pvy.put(sp.y);
            }
        }

        // If readback is written to, restore value
        Point device_rb = device.getPositionReadback();
        Point record_rb = new Point(pvx_rb.get(), pvx_rb.get());
        if (!record_rb.equals(device_rb))
        {
            pvx_rb.put(device_rb.x);
            pvy_rb.put(device_rb.y);        
        }

        // If state is written to, restore value
        int index = device.getState().ordinal();
        if (index != pvStateIndex.get())
        {
            pvStateIndex.put(index);
        }

        if (firstTime) {
            pvTimeStamp_sp.set(timeStamp);
            pvTimeStamp_rb.set(timeStamp);
            pvTimeStamp_st.set(timeStamp);
        }

        firstTime = false;
        pvTimeStamp.set(timeStamp);
    }

    static class AbortService implements RPCService {

        private ExampleRPCRecord pvRecord;

        AbortService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            pvRecord.device.abort();
            return pvDataCreate.createPVStructure(pvRecord.resultStructure);
        }
    }

    static class ConfigureService implements RPCService {

        private ExampleRPCRecord pvRecord;

        ConfigureService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            pvRecord.device.configure(getRequestedPoints(args));
            return pvDataCreate.createPVStructure(pvRecord.resultStructure);
        }

        private ArrayList<Point> getRequestedPoints(PVStructure args) throws RPCRequestException
        {
            PVStructureArray valueField = args.getSubField(PVStructureArray.class,
                    "value");
            if (valueField == null)
                throw new RPCRequestException(StatusType.ERROR,
                        "No structure array value field");

            Structure valueFieldStructure = valueField.
                    getStructureArray().getStructure();

            Scalar xField = valueFieldStructure.getField(Scalar.class, "x");
            if (xField == null || xField.getScalarType() != ScalarType.pvDouble)
                throw new RPCRequestException(StatusType.ERROR,
                        "value field's structure has no double field x");

            Scalar yField = valueFieldStructure.getField(Scalar.class, "y");
            if (yField == null || yField.getScalarType() != ScalarType.pvDouble)
                throw new RPCRequestException(StatusType.ERROR,
                        "value field's structure has no double field y");

            int length = valueField.getLength();
            StructureArrayData sad = new StructureArrayData();
            valueField.get(0, length, sad);

            ArrayList<Point> points = new ArrayList<Point>();
            for (int i = 0; i < length; i++)
            {
                double x = sad.data[i].getSubField(PVDouble.class, "x").get();
                double y = sad.data[i].getSubField(PVDouble.class, "y").get();
                points.add(new Point(x,y));
            }

            return points;
        }
    }

    static class RunService implements RPCService {

        private ExampleRPCRecord pvRecord;

        RunService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            pvRecord.device.run();
            return pvDataCreate.createPVStructure(pvRecord.resultStructure);
        }
    }

    static class PauseService implements RPCService {

        private ExampleRPCRecord pvRecord;

        PauseService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            pvRecord.device.pause();
            return pvDataCreate.createPVStructure(pvRecord.resultStructure);
        }
    }

    static class ResumeService implements RPCService {

        private ExampleRPCRecord pvRecord;

        ResumeService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            pvRecord.device.resume();
            return pvDataCreate.createPVStructure(pvRecord.resultStructure);
        }
    }

    static class StopService implements RPCService {

        private ExampleRPCRecord pvRecord;

        StopService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            pvRecord.device.stop();
            return pvDataCreate.createPVStructure(pvRecord.resultStructure);
        }
    }

    static class RewindService implements RPCService {

        private ExampleRPCRecord pvRecord;

        RewindService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        public PVStructure request(PVStructure args) throws RPCRequestException
        {
            pvRecord.device.rewind(getRequestedSteps(args));
            return pvDataCreate.createPVStructure(pvRecord.resultStructure);
        }

        int getRequestedSteps(PVStructure args) throws RPCRequestException
        {
            PVInt valueField = args.getSubField(PVInt.class, "value");
            if (valueField == null)
                throw new RPCRequestException(StatusType.ERROR,
                        "No int value field");

            return valueField.get();
        }
    }

    static class ScanService implements RPCServiceAsync, Device.Callback
    {
        private ExampleRPCRecord pvRecord;
        private final static Status statusOk = StatusFactory.
                getStatusCreate().getStatusOK();

        ScanService(ExampleRPCRecord record) {
            pvRecord = record;
        }

        private RPCResponseCallback callback;

        public void request(PVStructure args, RPCResponseCallback callback)
        {
            pvRecord.device.run();
            pvRecord.device.registerCallback(this);
            this.callback = callback;
        }

        private void handleError(String message, RPCResponseCallback callback)
        {
            Status status = StatusFactory.getStatusCreate().
                    createStatus(StatusType.ERROR, message, null);
            callback.requestDone(status, null);
            pvRecord.device.unregisterCallback(this);
        }

        public void setpointChanged(Point sp)
        {
        }

        public void readbackChanged(Point rb)
        {
        }

        public void stateChanged(Device.State state)
        {
            if (state == Device.State.READY)
            {
                handleError("Scan was stopped", callback);
                return;
            }
            else if (state == Device.State.IDLE)
            {
                 handleError("Scan was aborted", callback);
                 return;
            } 
        }

        public void scanComplete()
        {
            callback.requestDone(statusOk, pvDataCreate.createPVStructure(resultStructure));
            pvRecord.device.unregisterCallback(this);
        }
    }

    public static ExampleRPCRecord create(String recordName)
    {
        ExampleRPCRecord pvRecord = new ExampleRPCRecord(recordName, pvDataCreate.createPVStructure(recordStructure));
        PVDatabase master = PVDatabaseFactory.getMaster();
        master.addRecord(pvRecord);

        return pvRecord;
    }

    public ExampleRPCRecord(String recordName, PVStructure pvStructure) {
        super(recordName, pvStructure);
        pvx = pvStructure.getSubField(PVDouble.class, "positionSP.value.x");
        pvy = pvStructure.getSubField(PVDouble.class, "positionSP.value.y");
        pvx_rb = pvStructure.getSubField(PVDouble.class, "positionRB.value.x");
        pvy_rb = pvStructure.getSubField(PVDouble.class, "positionRB.value.y");

        pvTimeStamp.attach(pvStructure.getSubField(PVStructure.class, "timeStamp"));
        pvTimeStamp_sp.attach(pvStructure.getSubField(PVStructure.class, "positionSP.timeStamp"));
        pvTimeStamp_rb.attach(pvStructure.getSubField(PVStructure.class, "positionRB.timeStamp"));
        pvTimeStamp_st.attach(pvStructure.getSubField(PVStructure.class, "state.timeStamp"));

        pvStateIndex = pvStructure.getSubField(PVInt.class, "state.value.index");
        pvStateChoices = pvStructure.getSubField(PVStringArray.class, "state.value.choices");

        Device.State[] states = Device.State.values();
        String[] choices = new String[states.length];
        for (int i = 0; i < states.length; i++) {
            choices[i] = states[i].name();
        }
        pvStateChoices.put(0, choices.length, choices, 0);

        device.registerCallback(this);
        process();
    }

    public Service getService(PVStructure pvRequest)
    {
        PVString methodField = pvRequest.getSubField(PVString.class, "method");

        if (methodField != null)
        {
            String method = methodField.get();
            if (method.equals("abort"))
            {
                 return new AbortService(this);
            }
            else if (method.equals("configure"))
            {
                 return new ConfigureService(this);
            }
            else if (method.equals("run"))
            {
                return new RunService(this);
            }
            else if (method.equals("resume"))
            {
                 return new ResumeService(this);
            }
            else if (method.equals("pause"))
            {
                 return new PauseService(this);
            }
            else if (method.equals("stop"))
            {
                 return new StopService(this);
            }
            else if (method.equals("rewind"))
            {
                 return new RewindService(this);
            }
            else if (method.equals("scan"))
            {
                return new ScanService(this);
            }
        }
        return null;
    }
}
