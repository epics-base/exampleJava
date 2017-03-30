/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientChannelStateChangeRequester;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.pv.Union;


public class PutUnionForever
{
    private static final Pattern blankPattern = Pattern.compile("[ ]");
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final Convert convert = ConvertFactory.getConvert();
    
    static class ChannelStateChangeRequester implements PvaClientChannelStateChangeRequester
    {
        private boolean connected = false;

        @Override
        public void channelStateChange(PvaClientChannel channel, boolean isConnected) {
            System.out.println("channelStateChange state "
                    + (isConnected ? "true" : "false"));
            connected = isConnected;
        }
        public boolean isConnected() {return connected;}
    }
    
    public static void main( String[] args )
    {
        String provider ="pva";
        String channelName ="PVRrestrictedUnion";
        String request = "value";
        boolean  debug = false;
        int nargs = args.length;
        if(nargs==1 && args[0].equals("-help")) {
            System.out.println("channelName request debug");
            System.out.println("default");
            System.out.println(channelName  + " request " + request + " " + debug);
            return;
        }
        if(nargs>0) channelName = args[0];
        if(nargs>1) request = args[1];
        if(nargs>2) {
            String value = args[2];
            debug = (value.equals("true") ? true : false);
        }
        
        System.out.println(" channelName " + channelName + " request " + request + " debug " + debug);
        System.out.println("____PutUnionForever starting_______");
        try {
            PvaClient pva= PvaClient.get(provider);
            if(debug) PvaClient.setDebug(true);
            PvaClientChannel channel = pva.channel(channelName,provider,0.0);
            ChannelStateChangeRequester stateChangeRequester = new ChannelStateChangeRequester();
            channel.setStateChangeRequester(stateChangeRequester);
            PvaClientPut pvaClientPut = channel.put(request);
            PvaClientPutData putData = pvaClientPut.getData();
            PVField pvField = putData.getPVStructure().getPVFields()[0];
            if(pvField==null) {
                System.err.println("no value field");
                return;
            }
            if(pvField.getField().getType()!=Type.union){
                System.err.println("value is not a PVUnion");
                return;
            }
            PVUnion pvUnion = (PVUnion)pvField;
            Union u = pvUnion.getUnion();
            if(!u.isVariant()) {
                Field field = u.getField("string");
                if(field==null) {
                    System.err.println("union does not have a field named string");
                    return;
                }
                if(field.getType()!=Type.scalar) {
                    System.err.println("union field string is not a scalar");
                    return;
                }
                Scalar scalar = (Scalar)field;
                if(scalar.getScalarType()!=ScalarType.pvString) {
                    System.err.println("union field string does not have type string");
                    return;
                }
                field = u.getField("stringArray");
                if(field==null) {
                    System.err.println("union does not have a field named stringArray");
                    return;
                }
                if(field.getType()!=Type.scalarArray) {
                    System.err.println("union field stringArray is not a scalarArray");
                    return;
                }
                ScalarArray scalarArray = (ScalarArray)field;
                if(scalarArray.getElementType()!=ScalarType.pvString) {
                    System.err.println("union field stringArray does not have elementType string");
                    return;
                }
            }
            String value = "firstPut";
            while(true) {
                if(stateChangeRequester.isConnected()) {
                    System.out.println("value " + value);
                    String[] items = blankPattern.split(value);
                    int nitems = items.length;
                    boolean isArray = (nitems==1) ? false : true;
                    if(isArray) {
                        if(u.isVariant()) {
                            PVStringArray pvStringArray = (PVStringArray)pvDataCreate.createPVScalarArray(ScalarType.pvString);
                            convert.fromStringArray(pvStringArray,0,nitems,items,0);
                            pvUnion.set(pvStringArray);
                        } else {
                             PVStringArray pvStringArray = (PVStringArray)pvUnion.select("stringArray");
                             convert.fromStringArray(pvStringArray,0,nitems,items,0);
                        }
                    } else {
                         if(u.isVariant()) {
                             PVString pvString = (PVString)pvDataCreate.createPVScalar(ScalarType.pvString);
                             pvString.put(value);
                             pvUnion.set(pvString);
                         } else {
                             PVString pvString = (PVString)pvUnion.select("string");
                             pvString.put(value);
                         }
                     }
                     putData.getChangedBitSet().set(pvUnion.getFieldOffset());
                     pvaClientPut.put();
                } else {
                    System.out.println("did not issue get because connection lost");
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                try {
                    value = br.readLine();
                } catch (IOException ioe) {
                    System.out.println("IO error trying to read input!");
                }
                if(value.equals("exit")) break;
            }
            pva.destroy();
        }
        catch (Exception e)
        {
            System.err.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
