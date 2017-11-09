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

import org.epics.pvaClient.*;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;



public class MonitorForever
{
    
    static class ClientMonitorRequester implements PvaClientMonitorRequester, PvaClientUnlistenRequester
    {
        private boolean unlistenCalled = false;
        
        public ClientMonitorRequester(){}

        @Override
        public void unlisten(PvaClientMonitor monitor) {
            System.out.println("unlisten called");
            unlistenCalled = true;
           
        }

        @Override
        public void event(PvaClientMonitor monitor) {
            while(monitor.poll()) {
                PvaClientMonitorData monitorData = monitor.getData();
                System.out.println("changed");
                System.out.println(monitorData.showChanged());
                System.out.println("overrun");
                System.out.println(monitorData.showOverrun());
                monitor.releaseEvent();
            }
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
        if(nargs==1 && args[0].equals("-help")) {
            System.out.println("provider channelName request debug");
            System.out.println("default");
            System.out.println(provider
                + " " + channelName
                + " " + request
                + " " + debug);
            return;
        }
        if(nargs>0) provider = args[0];
        if(nargs>1) channelName = args[1];
        if(nargs>2) request = args[2];
        if(nargs>3) {
            String value = args[3];
            debug = (value.equals("true") ? true : false);
        }
        boolean pvaSrv = provider.contains("pva") ? true : false;
        boolean caSrv = provider.contains("ca") ? true : false;
        if(pvaSrv&&caSrv) {
            System.err.println("multiple providers are not allowed");
            return;
        }
        System.out.println(
            "provider\"" + provider + "\""
            + " pvaSrv" + pvaSrv
            + " caSrv" + caSrv
            + " channelName " + channelName
            + " request " + request
            + " debug " + debug
        );
        System.out.println("_____monitorForever starting_______");
        try {
            PvaClient pva = PvaClient.get(provider);
            if(debug) PvaClient.setDebug(true);
            ClientMonitorRequester monitorRequester = new ClientMonitorRequester();
            PvaClientMonitor monitor = pva.channel(channelName,provider).monitor(request,monitorRequester,monitorRequester);
            System.out.println("Type exit to stop:");
            while(true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String valueIn = null;
                if(br.ready()) {
                    try {
                        valueIn = br.readLine();
                    } catch (IOException ioe) {
                        System.out.println("IO error trying to read input!");
                    }
                    if(valueIn.equals("exit")) break;
                    if(valueIn.equals("stop")) monitor.stop();
                    if(valueIn.equals("start")) monitor.start();
                    if(monitorRequester.unlistenCalled) {
                        System.out.println("exiting because unlisten was called");
                        break;
                    }
                }

            }
            monitor.destroy();
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
