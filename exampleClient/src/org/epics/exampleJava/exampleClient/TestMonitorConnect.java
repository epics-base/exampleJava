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
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientMonitorRequester;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;



public class TestMonitorConnect
{

    static class ClientMonitor implements PvaClientChannelStateChangeRequester,PvaClientMonitorRequester
    {

        private String channelName = "";
        private String providerName = "";
        private String request = "";
        private boolean channelConnected = false;
        private boolean monitorConnected = false;
        private boolean isStarted = false;
        private boolean unlistenCalled = false;

        private PvaClientChannel pvaClientChannel = null;
        PvaClientMonitor pvaClientMonitor = null;

        void init(PvaClient pvaClient)
        {

            pvaClientChannel = pvaClient.createChannel(channelName,providerName);
            pvaClientChannel.setStateChangeRequester(this);
        }

        public static ClientMonitor create(
                PvaClient pvaClient,
                String channelName,
                String  providerName,
                String request)
        {
            ClientMonitor client = new ClientMonitor(channelName,providerName,request);
            client.init(pvaClient);
            return client;
        }

        public ClientMonitor(String channelName,
                String  providerName,
                String request)
        {
            this.channelName = channelName;
            this.providerName = providerName;
            this.request = request;
        }
        
        public void delete()
        {
            if(pvaClientMonitor!=null) pvaClientMonitor.destroy();
            if(pvaClientChannel!=null) pvaClientChannel.destroy();
        }

        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientChannelStateChangeRequester#channelStateChange(org.epics.pvaClient.PvaClientChannel, boolean)
         */
        public void channelStateChange(PvaClientChannel channel, boolean isConnected) {
            System.out.println("channelStateChange called"
                    + " isConnected " + isConnected);
            channelConnected = isConnected;
            if(isConnected && pvaClientMonitor==null) {
                pvaClientMonitor = pvaClientChannel.createMonitor(request);
                pvaClientMonitor.setRequester(this);
                pvaClientMonitor.issueConnect();
            }

        }
        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientMonitorRequester#monitorConnect(org.epics.pvdata.pv.Status, org.epics.pvaClient.PvaClientMonitor, org.epics.pvdata.pv.Structure)
         */
        public void monitorConnect(Status status,PvaClientMonitor monitor,Structure structure)
        {
            System.out.println("channelMonitorConnect " + channelName + " status ");
            if(!status.isOK()) return;
            monitorConnected = true;
            if(isStarted) return;
            isStarted = true;
        }

        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientMonitorRequester#unlisten(org.epics.pvaClient.PvaClientMonitor)
         */
        public void unlisten(PvaClientMonitor monitor) {
            System.out.println("unlisten called");
            unlistenCalled = true;

        }

        /* (non-Javadoc)
         * @see org.epics.pvaClient.PvaClientMonitorRequester#event(org.epics.pvaClient.PvaClientMonitor)
         */
        public void event(PvaClientMonitor monitor) {
            while(monitor.poll()) {
                PvaClientMonitorData monitorData = monitor.getData();
                System.out.println(channelName + " changed");
                System.out.println(monitorData.showChanged());
                System.out.println("overrun");
                System.out.println(monitorData.showOverrun());
                monitor.releaseEvent();
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
        
        public PvaClientMonitor getPvaClientMonitor() {
            return pvaClientMonitor;
        }
        
        public void stop()
        {
            if(isStarted) {
                isStarted = false;
                pvaClientMonitor.stop();
            }
        }

        public void start(String request)
        {
            if(!channelConnected || !monitorConnected)
            {
                System.out.println("notconnected");
                return;
            }
            isStarted = true;
            pvaClientMonitor.start(request);
        }
        public boolean isUnlisten() { return unlistenCalled;}
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
        System.out.println("_____monitor starting_______");
        try {
            if(debug) PvaClient.setDebug(true);
            PvaClient pva = PvaClient.get(provider);
            int num = channelNames.length;
            ClientMonitor[] clientMonitors = new ClientMonitor[num];
            for(i=0; i<num; ++i) {
                while(true) {
                    ClientMonitor clientMonitor = ClientMonitor.create(pva,channelNames[i],provider,request);
                    System.out.println("calling connect");
                    if(!clientMonitor.connect()) {
                        Thread.sleep(200);
                        clientMonitor.delete();
                    }
                    clientMonitors[i] = clientMonitor;
                    break;
                }
                clientMonitors[i] = ClientMonitor.create(pva,channelNames[i],provider,request);
            }
            while(true) {
                System.out.println("Enter one of: exit start stop");
                String valueIn = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                valueIn = br.readLine();
                if(valueIn.equals("exit")) break;
                if(valueIn.equals("start")) {
                    System.out.println("Enter request");
                    request = br.readLine();
                    for(i=0; i<num; ++i) {
                        try {
                            clientMonitors[i].start(request);
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
                if(valueIn.equals("stop")) {
                    for(i=0; i<num; ++i) {
                        try {
                            clientMonitors[i].stop();
                        } catch (Exception e) {
                            System.out.println(channelNames[i] + " exception " + e.getMessage());
                        }
                    }
                    continue;
                }
                for(i=0; i<num; ++i) {
                    if(clientMonitors[i].isUnlisten()) {
                        System.out.println(channelNames[i] + " unlisten was called");
                    }
                    boolean isConnected = clientMonitors[i].
                        getPvaClientMonitor().getPvaClientChannel().getChannel().isConnected();
                    System.out.println(channelNames[i] + " isConnected " + isConnected);
                }
            }
            for(i=0; i<num; ++i) {
                clientMonitors[i].delete();
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
