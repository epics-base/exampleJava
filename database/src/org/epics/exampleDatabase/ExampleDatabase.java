/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */


package org.epics.exampleDatabase;

import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.ContextLocal;


/**
 * @author Marty Kraimer
 *
 */
public class ExampleDatabase {
 
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();

static void createStructureArrayRecord(
    PVDatabase master,
    String recordName)
{
    Structure top = fieldCreate.createFieldBuilder().
         addNestedStructureArray("value").
             add("name",ScalarType.pvString).
             add("value",ScalarType.pvString).
             endNested().
         createStructure();
    PVStructure pvStructure = pvDataCreate.createPVStructure(top);
    PVRecord pvRecord = new PVRecord(recordName,pvStructure);
    boolean result = master.addRecord(pvRecord); 
    if(!result) throw new RuntimeException(recordName + " not added");
}

static void createRegularUnionRecord(
    PVDatabase master,
    String recordName)
{
    Structure top = fieldCreate.createFieldBuilder().
         addNestedUnion("value").
             add("string",ScalarType.pvString).
             addArray("stringArray",ScalarType.pvString).
             endNested().
         createStructure();
    PVStructure pvStructure = pvDataCreate.createPVStructure(top);
    PVRecord pvRecord = new PVRecord(recordName,pvStructure);
    boolean result = master.addRecord(pvRecord);
    if(!result) throw new RuntimeException(recordName + " not added");
}

static void createVariantUnionRecord(
    PVDatabase master,
    String recordName)
{
    Structure top = fieldCreate.createFieldBuilder().
         add("value",fieldCreate.createVariantUnion()).
         createStructure();
    PVStructure pvStructure = pvDataCreate.createPVStructure(top);
    PVRecord pvRecord = new PVRecord(recordName,pvStructure);
    boolean result = master.addRecord(pvRecord);
    if(!result) throw new RuntimeException(recordName + " not added");
}

static void createRegularUnionArrayRecord(
    PVDatabase master,
    String recordName)
{
    Structure top = fieldCreate.createFieldBuilder().
         addNestedUnionArray("value").
             add("string",ScalarType.pvString).
             addArray("stringArray",ScalarType.pvString).
             endNested().
         createStructure();
    PVStructure pvStructure = pvDataCreate.createPVStructure(top);
    PVRecord pvRecord = new PVRecord(recordName,pvStructure);
    boolean result = master.addRecord(pvRecord);
    if(!result) throw new RuntimeException(recordName + " not added");
}

static void createVariantUnionArrayRecord(
    PVDatabase master,
    String recordName)
{
    Structure top = fieldCreate.createFieldBuilder().
         addArray("value",fieldCreate.createVariantUnion()).
         createStructure();
    PVStructure pvStructure = pvDataCreate.createPVStructure(top);
    PVRecord pvRecord = new PVRecord(recordName,pvStructure);
    boolean result = master.addRecord(pvRecord);
    if(!result) throw new RuntimeException(recordName + " not added");
}

static void createDumbPowerSupplyRecord(
    PVDatabase master,
    String recordName)
{
     Structure top = fieldCreate.createFieldBuilder().
         add("alarm",standardField.alarm()) .
            add("timeStamp",standardField.timeStamp()) .
            addNestedStructure("power") .
               add("value",ScalarType.pvDouble) .
               add("alarm",standardField.alarm()) .
               endNested().
            addNestedStructure("voltage") .
               add("value",ScalarType.pvDouble) .
               add("alarm",standardField.alarm()) .
               endNested().
            addNestedStructure("current") .
               add("value",ScalarType.pvDouble) .
               add("alarm",standardField.alarm()) .
               endNested().
            createStructure();
    PVStructure pvStructure = pvDataCreate.createPVStructure(top);
    PVRecord pvRecord = new PVRecord(recordName,pvStructure);
    boolean result = master.addRecord(pvRecord);
    if(!result) throw new RuntimeException(recordName + " not added");
}

    private static void createRecords(
            PVDatabase master,
            ScalarType scalarType,
            String recordNamePrefix,
            String properties)
    {
        String recordName = recordNamePrefix;
        PVStructure pvStructure = standardPVField.scalar(scalarType,properties);
        PVRecord pvRecord = new PVRecord(recordName,pvStructure);
        master.addRecord(pvRecord);
        recordName += "Array";
        pvStructure = standardPVField.scalarArray(scalarType,properties);
        pvRecord = new PVRecord(recordName,pvStructure);
        master.addRecord(pvRecord);
    }

    public static void main(String[] args)
    {
        PVDatabase master = PVDatabaseFactory.getMaster();
        ContextLocal context = new ContextLocal();
        context.start(false);
        
        String  properties = "alarm,timeStamp";
        createRecords(master,ScalarType.pvByte,"PVRbyte01",properties);

        createRecords(master,ScalarType.pvShort,"PVRshort01",properties);
        createRecords(master,ScalarType.pvLong,"PVRlong01",properties);
        createRecords(master,ScalarType.pvUByte,"PVRubyte01",properties);
        createRecords(master,ScalarType.pvUInt,"PVRuint01",properties);
        createRecords(master,ScalarType.pvUShort,"PVRushort01",properties);
        createRecords(master,ScalarType.pvULong,"PVRulong01",properties);
        createRecords(master,ScalarType.pvFloat,"PVRfloat01",properties);

        String[] choices = new String[2];
        choices[0] = "zero";
        choices[1] = "one";
        master.addRecord(new PVRecord(
                "PVRenum",standardPVField.enumerated(choices,properties)));

        createRecords(master,ScalarType.pvBoolean,"PVRboolean",properties);
        createRecords(master,ScalarType.pvByte,"PVRbyte",properties);
        createRecords(master,ScalarType.pvShort,"PVRshort",properties);
        createRecords(master,ScalarType.pvInt,"PVRint",properties);
        createRecords(master,ScalarType.pvLong,"PVRlong",properties);
        createRecords(master,ScalarType.pvFloat,"PVRfloat",properties);
        createRecords(master,ScalarType.pvDouble,"PVRdouble",properties);
        createRecords(master,ScalarType.pvDouble,"PVRdouble01",properties);
        createRecords(master,ScalarType.pvDouble,"PVRdouble02",properties);
        createRecords(master,ScalarType.pvDouble,"PVRdouble03",properties);
        createRecords(master,ScalarType.pvDouble,"PVRdouble04",properties);
        createRecords(master,ScalarType.pvDouble,"PVRdouble05",properties);
        createRecords(master,ScalarType.pvString,"PVRstring",properties);
        createStructureArrayRecord(master,"PVRstructureArray");
        createRegularUnionRecord(master,"PVRregularUnion");
        createVariantUnionRecord(master,"PVRvariantUnion");
        createRegularUnionArrayRecord(master,"PVRregularUnionArray");
        createVariantUnionArrayRecord(master,"PVRvariantUnionArray");
        createDumbPowerSupplyRecord(master,"PVRdumbPowerSupply");
        
        String recordName = "PVRhelloPutGet";
        PVRecord pvRecord = ExampleHelloRecord.create(recordName);
        master.addRecord(pvRecord);
        
        RPCServer rpcServer = new RPCServer();
        rpcServer.registerService("helloRPC", new ExampleHelloRPC());
        while(true) {
            System.out.print("waiting for exit: ");
            String value = System.console().readLine();
            if(value.equals("exit")) break;
        }
        context.destroy();
        master.destroy();
        System.out.println("ExampleDatabase exiting");
        System.exit(0);
    }
}
