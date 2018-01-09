/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Vector;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientChannelStateChangeRequester;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvaClient.PvaClientPutRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Type;


public class Put
{
    private static final Convert convert = ConvertFactory.getConvert();
    
    static class ClientPut implements PvaClientChannelStateChangeRequester,PvaClientPutRequester
    {
        private String channelName;
        private String providerName;
        private String request;
        private boolean channelConnected = false;
        private boolean putConnected = false;
        private PvaClientChannel pvaClientChannel = null;
        private PvaClientPut pvaClientPut = null;
        
        private void init(PvaClient pvaClient)
        {
            pvaClientChannel = pvaClient.createChannel(channelName,providerName);
            pvaClientChannel.setStateChangeRequester(this);
            pvaClientChannel.issueConnect();
        }
        
        static public ClientPut create(
            PvaClient pvaClient,String channelName,String providerName,String request)
        {
            ClientPut clientPut = new ClientPut(channelName,providerName,request);
            clientPut.init(pvaClient);
            return clientPut;
        }
        
        private ClientPut(String channelName,String providerName,String request)
        {
             this.channelName = channelName;
             this.providerName = providerName;
             this.request = request;
        }
        
        public void delete()
        {
            if(pvaClientPut!=null) pvaClientPut.destroy();
            if(pvaClientChannel!=null) pvaClientChannel.destroy();
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientChannelStateChangeRequester#channelStateChange(org.epics.pvaClient.PvaClientChannel, boolean)
         */
        public void channelStateChange(PvaClientChannel channel, boolean isConnected) {
            channelConnected = isConnected;
            if(isConnected && pvaClientPut==null) {
                pvaClientPut = pvaClientChannel.createPut(request);
                pvaClientPut.setRequester(this);
                pvaClientPut.issueConnect();
                
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientPutRequester#channelPutConnect(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientPut)
         */
        public void channelPutConnect(
                Status status,
                PvaClientPut clientPut)
        {
            putConnected = true;
            System.out.println("channelPutConnect " + channelName + " status ");
        }
        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientPutRequester#getDone(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientPut)
         */
        public void getDone(
                Status status,
                PvaClientPut clientPut)
        {
            System.out.println("getDone " + channelName + " status ");
        }

        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientPutRequester#putDone(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientPut)
         */
        public void putDone(
                Status status,
                PvaClientPut clientPut)
        {
            System.out.println("putDone " + channelName + " status ");
        }

        public void put(String value)
        {
            if(!channelConnected) {
                System.out.println(channelName + " channel not connected");
                return;
            }
            if(!putConnected) {
                System.out.println(channelName + " channelPut not connected");

                return;
            }
            PvaClientPutData putData = pvaClientPut.getData();
            PVStructure pvStructure = putData.getPVStructure();
            PVScalar pvScalar = pvStructure.getSubField(PVScalar.class,"value");
            PVScalarArray pvScalarArray = pvStructure.getSubField(PVScalarArray.class,"value");
            while(true)
            {
                if(pvScalar!=null) break;
                if(pvScalarArray!=null) break;
                PVField[] pvFields = pvStructure.getPVFields();
                PVField pvField = pvFields[0];
                if(pvField.getField().getType()==Type.scalar) {
                    pvScalar = (PVScalar)(pvField);
                    break;
                }
                if(pvField.getField().getType()==Type.scalarArray) {
                    pvScalarArray = (PVScalarArray)(pvField);
                    break;
                }
                if(pvField.getField().getType()==Type.structure) {
                    pvStructure = (PVStructure)(pvField);
                    continue;
                }

                System.out.println(channelName + " did not find a pvScalar field");
                return;
            }
            if(pvScalar!=null) {
                convert.fromString(pvScalar,value);
            } else {
                Vector<String> values = new Vector<String>();
                int pos =0;
                int n = 1;
                while(true) {
                    int offset = value.indexOf(" ",pos);
                    if(offset<0)
                    {
                        values.add(value.substring(pos));
                        break;
                    }
                    values.add(value.substring(pos,offset));
                    pos = offset+1;
                    n++;  
                }
                pvScalarArray.setCapacity(n);
                pvScalarArray.setLength(n);
                String[] vals = new String[n];
                for(int i=0; i<n; ++i) vals[i] = values.get(i);
                convert.fromStringArray(pvScalarArray,0,n,vals,0); 
            }
            pvaClientPut.put();
        }
        public void get()
        {
            if(!channelConnected) {
                System.out.println(channelName + " channel not connected");
                return;
            }
            if(!putConnected) {
                System.out.println(channelName + " channelPut not connected");

                return;
            }
            pvaClientPut.get();
            System.out.println(pvaClientPut.getData().getPVStructure());;
        }
    }
    
    public static void main( String[] args )
    {
        String provider ="pva";
        String channelName ="PVRdouble";
        String request = "value";
        boolean  debug = false;
        int nargs = args.length;
        int nextarg = 0;
        while(true) {
            if(nargs<=0) break;
            String arg = args[nextarg];
            if(arg.equals("-help")) {
                System.out.println("-p provider -r request -d debug channelNames");
                System.out.println("default");
                System.out.println(
                    "-p " + provider
                    + " -r " + request
                    + " -d " + debug
                    + " " + channelName
                    );
                return;
            }
            if(arg.equals("-p")) {
                nargs = nargs-2;
                nextarg++;
                provider = args[nextarg];
                nextarg++;
                continue;
            }
            if(arg.equals("-r")) {
                nargs = nargs-2;
                nextarg++;
                request = args[nextarg];
                nextarg++;
                continue;
            }
            if(arg.equals("-d")) {
                nargs = nargs-2;
                nextarg++;
                String value = args[nextarg];
                debug = (value.equals("true") ? true : false);
                nextarg++;
                continue;
            }
            if(arg.startsWith("-")) {
                System.err.printf("Illegal argument " + arg);
                System.exit(1);
            }
            break;
        }
        String[] channelNames = new String[(nargs==0) ? 1 : nargs];
        int i=0;
        if(nargs==0 ) {
            channelNames[0] = channelName;
        } else while(nargs>0) {
            channelNames[i] = args[nextarg];
            i++;
            nargs--;
            nextarg++;
        }
        System.out.println(
            "provider\"" + provider + "\""
            
            + " request " + request
            + " debug " + debug
            + " channelNames " + Arrays.toString(channelNames)
        );
        System.out.println("_____put starting_______");
        try {
            if(debug) PvaClient.setDebug(true);
            PvaClient pva = PvaClient.get(provider);
            int num = channelNames.length;
            ClientPut[] clientPuts = new ClientPut[num];
            for(i=0; i<num; ++i) {
                clientPuts[i] = ClientPut.create(pva,channelNames[i],provider,request);
            }
            while(true) {
                System.out.println("enter: exit or put or get");
                String value = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                value = br.readLine();
                if(value.equals("exit")) break;
                if(value.equals("put")) {
                    System.out.println("enter value");
                    value = br.readLine();
                    for(i=0; i<num; ++i) {
                        try {
                            clientPuts[i].put(value);
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
                if(value.equals("get")) {
                    for(i=0; i<num; ++i) {
                        try {
                            clientPuts[i].get();
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
            }
            for(i=0; i<num; ++i) {
                clientPuts[i].delete();
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
