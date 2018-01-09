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
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvaClient.PvaClientPutGet;
import org.epics.pvaClient.PvaClientPutGetRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.*;


public class PutGet
{
    private static final Convert convert = ConvertFactory.getConvert();
    
    static class ClientPutGet implements PvaClientChannelStateChangeRequester,PvaClientPutGetRequester
    {
        private String channelName;
        private String providerName;
        private String request;
        private boolean channelConnected = false;
        private boolean putGetConnected = false;
        private PvaClientChannel pvaClientChannel = null;
        private PvaClientPutGet pvaClientPutGet = null;
        
        private void init(PvaClient pvaClient)
        {
            pvaClientChannel = pvaClient.createChannel(channelName,providerName);
            pvaClientChannel.setStateChangeRequester(this);
            pvaClientChannel.issueConnect();
        }
        
        static public ClientPutGet create(
            PvaClient pvaClient,String channelName,String providerName,String request)
        {
            ClientPutGet clientPutGet = new ClientPutGet(channelName,providerName,request);
            clientPutGet.init(pvaClient);
            return clientPutGet;
        }
        
        private ClientPutGet(String channelName,String providerName,String request)
        {
             this.channelName = channelName;
             this.providerName = providerName;
             this.request = request;
        }
        
        public void delete()
        {
            if(pvaClientPutGet!=null) pvaClientPutGet.destroy();
            if(pvaClientChannel!=null) pvaClientChannel.destroy();
        }
        
        public void channelStateChange(PvaClientChannel channel, boolean isConnected) {
            channelConnected = isConnected;
            if(isConnected && pvaClientPutGet==null) {
                pvaClientPutGet = pvaClientChannel.createPutGet(request);
                pvaClientPutGet.setRequester(this);
                pvaClientPutGet.issueConnect();
                
            }
        }
        
        public void channelPutGetConnect(
                Status status,
                PvaClientPutGet clientPutGet)
        {
            putGetConnected = true;
            System.out.println("channelPutGetConnect " + channelName + " status ");
        }
        
        public void putGetDone(
                Status status,
                PvaClientPutGet clientPutGet)
        {
            if(status.isOK()) {
                System.out.println(pvaClientPutGet.getGetData().getPVStructure());;
            } else {
                System.out.println("putGetDone " + channelName + " status ");
            }
        }
        
        
        public void getGetDone(
                Status status,
                PvaClientPutGet clientPutGet)
        {
            if(status.isOK()) {
                System.out.println(pvaClientPutGet.getGetData().getPVStructure());;
            } else {
                System.out.println("getGetDone " + channelName + " status ");
            }
        }
        
        public void getPutDone(
                Status status,
                PvaClientPutGet clientPutGet)
        {
            if(status.isOK()) {
                System.out.println(pvaClientPutGet.getPutData().getPVStructure());
            } else {
                System.out.println("getPutDone " + channelName + " status ");
            }
        }


        public void putGet(String value)
        {
            if(!channelConnected) {
                System.out.println(channelName + " channel not connected");
                return;
            }
            if(!putGetConnected) {
                System.out.println(channelName + " channelPutGet not connected");

                return;
            }
            PvaClientPutData putData = pvaClientPutGet.getPutData();
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
            pvaClientPutGet.issuePutGet();
        }
        public void getPut()
        {
            if(!channelConnected) {
                System.out.println(channelName + " channel not connected");
                return;
            }
            if(!putGetConnected) {
                System.out.println(channelName + " channelPutGet not connected");

                return;
            }
            pvaClientPutGet.issueGetPut();
        }
        public void getGet()
        {
            if(!channelConnected) {
                System.out.println(channelName + " channel not connected");
                return;
            }
            if(!putGetConnected) {
                System.out.println(channelName + " channelPutGet not connected");

                return;
            }
            pvaClientPutGet.issueGetGet();
        }
    }
    
    public static void main( String[] args )
    {
        String provider ="pva";
        String channelName ="PVRhelloPutGet";
        String request = "putField(argument)getField(result)";
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
            ClientPutGet[] clientPutGets = new ClientPutGet[num];
            for(i=0; i<num; ++i) {
                clientPutGets[i] = ClientPutGet.create(pva,channelNames[i],provider,request);
            }
            while(true) {
                System.out.println("enter one of: exit putGet getPut getGet");
                String value = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                value = br.readLine();
                if(value.equals("exit")) break;
                if(value.equals("putGet")) {
                    System.out.println("enter value");
                    value = br.readLine();
                    for(i=0; i<num; ++i) {
                        try {
                            clientPutGets[i].putGet(value);
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
                if(value.equals("getPut")) {
                    for(i=0; i<num; ++i) {
                        try {
                            clientPutGets[i].getPut();
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
                if(value.equals("getGet")) {
                    for(i=0; i<num; ++i) {
                        try {
                            clientPutGets[i].getGet();
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
            }
            for(i=0; i<num; ++i) {
                clientPutGets[i].delete();
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
