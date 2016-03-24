// Copyright information and license terms for this software can be
// found in the file LICENSE that is included with the distribution

/**
 * @author mrk
 * @date 2013.07.24
 */


package org.epics.exampleJava.exampleDatabase;

import org.epics.nt.NTEnum;
import org.epics.nt.NTEnumBuilder;
import org.epics.nt.NTScalar;
import org.epics.nt.NTScalarArray;
import org.epics.nt.NTScalarArrayBuilder;
import org.epics.nt.NTScalarBuilder;
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
import org.epics.pvdatabase.pva.ContextLocal;


/**
 * @author Marty Kraimer
 *
 */
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

    public static void main(String[] args)
    {
        PVDatabase master = PVDatabaseFactory.getMaster();
        
        
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
        
        String recordName = "PVRhelloPutGet";
        pvRecord = ExampleHelloRecord.create(recordName);
        master.addRecord(pvRecord);
        
        recordName = "helloRPC";
        pvRecord = ExampleHelloRPC.create(recordName);
        master.addRecord(pvRecord);
                
        ContextLocal context = new ContextLocal();
        context.start(true);
        System.out.println("ExampleDatabase exiting");
    }
}
