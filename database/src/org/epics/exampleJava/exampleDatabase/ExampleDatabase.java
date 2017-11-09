/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 * @since 2013.07.24
 */


package org.epics.exampleJava.exampleDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.epics.nt.NTEnum;
import org.epics.nt.NTEnumBuilder;
import org.epics.nt.NTScalar;
import org.epics.nt.NTScalarArray;
import org.epics.nt.NTScalarArrayBuilder;
import org.epics.nt.NTScalarBuilder;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.RemoveRecord;
import org.epics.pvdatabase.TraceRecord;
import org.epics.pvdatabase.pva.ChannelProviderLocalFactory;

public class ExampleDatabase {

    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    
    
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

    static void createRestrictedUnionRecord(
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
    
    static void createBigRecord(
            PVDatabase master,
            String recordName)
    {
        Structure top = fieldCreate.createFieldBuilder().
                add("timeStamp",standardField.timeStamp()) .
                addNestedStructure("scalar") .
                    addNestedStructure("boolean") .
                        add("value",ScalarType.pvBoolean) .
                    endNested().
                    addNestedStructure("byte") .
                        add("value",ScalarType.pvByte) .
                    endNested().
                    addNestedStructure("long") .
                        add("value",ScalarType.pvLong) .
                    endNested().
                    addNestedStructure("double") .
                        add("value",ScalarType.pvDouble) .
                    endNested().
                    addNestedStructure("string") .
                        add("value",ScalarType.pvString) .
                    endNested().
                endNested().
                addNestedStructure("scalarArray") .
                    addNestedStructure("boolean") .
                        addArray("value",ScalarType.pvBoolean) .
                    endNested().
                    addNestedStructure("byte") .
                        addArray("value",ScalarType.pvByte) .
                    endNested().
                    addNestedStructure("long") .
                        addArray("value",ScalarType.pvLong) .
                    endNested().
                    addNestedStructure("double") .
                        addArray("value",ScalarType.pvDouble) .
                    endNested().
                    addNestedStructure("string") .
                        addArray("value",ScalarType.pvString) .
                    endNested().
                endNested().
                addNestedStructureArray("structureArray").
                    add("name",ScalarType.pvString).
                    add("value",ScalarType.pvString).
                endNested().
                addNestedUnion("restrictedUnion").
                    add("string",ScalarType.pvString).
                    addArray("stringArray",ScalarType.pvString).
                endNested().
                add("variantUnion",fieldCreate.createVariantUnion()).
            createStructure();
        PVStructure pvStructure = pvDataCreate.createPVStructure(top);
        PVRecord pvRecord = new PVRecord(recordName,pvStructure);
        boolean result = master.addRecord(pvRecord); 
        if(!result) throw new RuntimeException(recordName + " not added");
    }

    private static void createRecords(
            PVDatabase master,
            ScalarType scalarType,
            String recordNamePrefix)
    {
        String recordName = recordNamePrefix;
        NTScalarBuilder ntScalarBuilder = NTScalar.createBuilder();
        PVStructure pvStructure = ntScalarBuilder.
                value(scalarType).
                addAlarm().addTimeStamp().
                createPVStructure();
        PVRecord pvRecord = new PVRecord(recordName,pvStructure);
        master.addRecord(pvRecord);
        recordName += "Array";
        NTScalarArrayBuilder ntScalarArrayBuilder = NTScalarArray.createBuilder();
        pvStructure = ntScalarArrayBuilder.
                value(scalarType).
                addAlarm().addTimeStamp().
                createPVStructure();
        pvRecord = new PVRecord(recordName,pvStructure);
        master.addRecord(pvRecord);
    }

    /**
     * Create a server for an example PVDatabase.
     * @param args Ignored.
     */
    public static void main(String[] args)
    {
        try {
            PVDatabase master = PVDatabaseFactory.getMaster();
            ChannelProvider channelProvider = ChannelProviderLocalFactory.getChannelProviderLocal();

            createRecords(master,ScalarType.pvBoolean,"PVRboolean");
            createRecords(master,ScalarType.pvByte,"PVRbyte");
            createRecords(master,ScalarType.pvShort,"PVRshort");
            createRecords(master,ScalarType.pvInt,"PVRint");
            createRecords(master,ScalarType.pvLong,"PVRlong");
            createRecords(master,ScalarType.pvUByte,"PVRubyte");
            createRecords(master,ScalarType.pvUShort,"PVRushort");
            createRecords(master,ScalarType.pvUInt,"PVRuint");
            createRecords(master,ScalarType.pvULong,"PVRulong");
            createRecords(master,ScalarType.pvFloat,"PVRfloat");
            createRecords(master,ScalarType.pvDouble,"PVRdouble");
            createRecords(master,ScalarType.pvString,"PVRstring");

            createRecords(master,ScalarType.pvDouble,"PVRdouble01");
            createRecords(master,ScalarType.pvDouble,"PVRdouble02");
            createRecords(master,ScalarType.pvDouble,"PVRdouble03");
            createRecords(master,ScalarType.pvDouble,"PVRdouble04");
            createRecords(master,ScalarType.pvDouble,"PVRdouble05");
            
            master.addRecord(TraceRecord.create("PVRtraceRecord"));
            master.addRecord(RemoveRecord.create("PVRremoveRecord"));
            
            NTEnumBuilder ntEnumBuilder = NTEnum.createBuilder();
            PVStructure pvStructure = ntEnumBuilder.
                    addAlarm().addTimeStamp().
                    createPVStructure();
            String[] choices = new String[2];
            choices[0] = "zero";
            choices[1] = "one";
            PVStringArray pvChoices = pvStructure.getSubField(PVStringArray.class,"value.choices");
            pvChoices.put(0, 2, choices, 0);
            PVRecord pvRecord = new PVRecord("PVRenum",pvStructure);
            master.addRecord(pvRecord);

            createStructureArrayRecord(master,"PVRstructureArray");
            createRestrictedUnionRecord(master,"PVRrestrictedUnion");
            createVariantUnionRecord(master,"PVRvariantUnion");
            createRegularUnionArrayRecord(master,"PVRrestrictedUnionArray");
            createVariantUnionArrayRecord(master,"PVRvariantUnionArray");
            createDumbPowerSupplyRecord(master,"PVRdumbPowerSupply");
            createBigRecord(master,"PVRBigRecord");

            String recordName = "PVRhelloPutGet";
            pvRecord = ExampleHelloRecord.create(recordName);
            master.addRecord(pvRecord);

            recordName = "PVRsoft";
            pvRecord = ExampleSoftRecord.create(recordName);
            master.addRecord(pvRecord);
            recordName = "PVRhelloRPC";
            pvRecord = ExampleHelloRPC.create(recordName);
            master.addRecord(pvRecord);
            ServerContextImpl context = ServerContextImpl.startPVAServer(channelProvider.getProviderName(),0,true,null); 
            System.out.println("ExampleDatabase started");
            while(true) {
                System.out.print("Type 'exit' to shut down: ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String value = null;
                try {
                    value = br.readLine();
                } catch (IOException ioe) {
                    System.out.println("IO error trying to read input!");
                }
                if(value.equals("exit")) break;
            }
            context.destroy();
            master.destroy();
            channelProvider.destroy();
        } catch (PVAException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("ExampleDatabase exiting");
    }
}
