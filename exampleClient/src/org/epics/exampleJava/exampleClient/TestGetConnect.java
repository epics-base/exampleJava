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

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientChannelStateChangeRequester;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetRequester;
import org.epics.pvdata.pv.Status;


public class TestGetConnect
{
    
    static class ClientGet implements PvaClientChannelStateChangeRequester,PvaClientGetRequester
    {
        private String channelName;
        private String providerName;
        private String request;
        private boolean channelConnected = false;
        private boolean getConnected = false;
        private PvaClientChannel pvaClientChannel = null;
        private PvaClientGet pvaClientGet = null;
        
        private void init(PvaClient pvaClient)
        {
            pvaClientChannel = pvaClient.createChannel(channelName,providerName);
            pvaClientChannel.setStateChangeRequester(this);
        }
        
        static public ClientGet create(
            PvaClient pvaClient,String channelName,String providerName,String request)
        {
            ClientGet clientGet = new ClientGet(channelName,providerName,request);
            clientGet.init(pvaClient);
            return clientGet;
        }
        

        private ClientGet(String channelName,String providerName,String request)
        {
             this.channelName = channelName;
             this.providerName = providerName;
             this.request = request;
        }
        
        public void delete()
        {
            if(pvaClientGet!=null) pvaClientGet.destroy();
            if(pvaClientChannel!=null) pvaClientChannel.destroy();
        }
           
        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientChannelStateChangeRequester#channelStateChange(org.epics.pvaClient.PvaClientChannel, boolean)
         */
        public void channelStateChange(PvaClientChannel channel, boolean isConnected) {
            channelConnected = isConnected;
            if(isConnected && pvaClientGet==null) {
                pvaClientGet = pvaClientChannel.createGet(request);
                pvaClientGet.setRequester(this);
                pvaClientGet.issueConnect();
                
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientGetRequester#channelGetConnect(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientGet)
         */
        public void channelGetConnect(
                Status status,
                PvaClientGet clientGet)
        {
            getConnected = true;
            System.out.println("channelGetConnect " + channelName + " status ");
        }
        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientGetRequester#getDone(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientGet)
         */
        public void getDone(
                Status status,
                PvaClientGet clientGet)
        {
            if(status.isOK()) {
                System.out.println(pvaClientGet.getData().getPVStructure());;
            } else {
                System.out.println("getDone " + channelName + " status ");
            }
        }
        
        public boolean connect() 
        {
            try {
                pvaClientChannel.connect(2.0);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        public void get()
        {
            if(!channelConnected) {
                System.out.println(channelName + " channel not connected");
                return;
            }
            if(!getConnected) {
                System.out.println(channelName + " channelGet not connected");

                return;
            }
            pvaClientGet.issueGet();
        }
    }
    
    public static void main( String[] args )
    {
        String provider ="pva";
        String channelName ="PVRdouble";
        String request = "value,alarm,timeStamp";
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
        System.out.println("_____get starting_______");
        try {
            if(debug) PvaClient.setDebug(true);
            PvaClient pva = PvaClient.get(provider);
            int num = channelNames.length;
            ClientGet[] clientGets = new ClientGet[num];
            for(i=0; i<num; ++i) {
                while(true) {
                    ClientGet clientGet = ClientGet.create(pva,channelNames[i],provider,request);
                    System.out.println("calling connect");
                    if(!clientGet.connect()) {
                        Thread.sleep(200);
                        clientGet.delete();
                    }
                    clientGets[i] = clientGet;
                    break;
                }
            }
            while(true) {
                System.out.println("enter: exit or get");
                String value = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                value = br.readLine();
                if(value.equals("exit")) break;
                if(value.equals("get")) {
                    for(i=0; i<num; ++i) {
                        try {
                            clientGets[i].get();
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
            }
            for(i=0; i<num; ++i) {
                clientGets[i].delete();
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
