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

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientChannelStateChangeRequester;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.Type;


public class PutForever
{
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
        String channelName ="PVRint";
        String request = "value";
        boolean  debug = false;
        int nargs = args.length;
        if(nargs==1 && args[0].equals("-help")) {
            System.out.println("channelName request debug");
            System.out.println("default");
            System.out.println(channelName  + " request " + request + " " + debug);
            return;
        }
        if(nargs>0) provider = args[0];
        if(nargs>1) channelName = args[1];
        if(nargs>2) request = args[2];
        if(nargs>3) {
            String value = args[3];
            debug = (value.equals("true") ? true : false);
        }
        
        System.out.println(" channelName " + channelName + " request " + request + " debug " + debug);
        System.out.println("____PutForever starting_______");
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
            if(pvField.getField().getType()!=Type.scalar){
                System.err.println("value is not a PVScalar");
                return;
            }
            PVScalar pvScalar = (PVScalar)pvField;            
            String value = "0";
            while(true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                try {
                    value = br.readLine();
                } catch (IOException ioe) {
                    System.out.println("IO error trying to read input!");
                }
                if(value.equals("exit")) break;
                if(stateChangeRequester.isConnected()) {
                    System.out.println("value " + value);
                    convert.fromString(pvScalar,value);
                    pvaClientPut.put();
                    System.out.println("put complete");
                } else {
                    System.out.println("did not issue put because connection lost");
                }
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
