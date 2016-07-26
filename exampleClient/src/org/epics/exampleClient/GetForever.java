/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.epics.pvaClient.*;


public class GetForever
{
    static class ChannelStateChangeRequester implements PvaClientChannelStateChangeRequester
    {
        private boolean connected = true;

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
        String channelName ="PVRdouble";
        boolean  debug = false;
        int nargs = args.length;
        if(nargs==1 && args[0].equals("-help")) {
            System.out.println("provider channelName  debug");
            System.out.println("default");
            System.out.println(provider + " " + channelName + " " + debug);
            return;
        }
        if(nargs>0) provider = args[0];
        if(nargs>1) channelName = args[1];
        if(nargs>2) {
            String value = args[2];
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
            + " pvaSrv " + pvaSrv
            + " caSrv " + caSrv
            + " channelName " + channelName
            + " debug " + debug
        );
        System.out.println("_____getForever starting_______");
        try {
            PvaClient pva= PvaClient.get(provider);
            if(debug) PvaClient.setDebug(true);
            PvaClientChannel channel = pva.channel(channelName,provider);
            ChannelStateChangeRequester stateChangeRequester = new ChannelStateChangeRequester();
            while(true) {
                if(stateChangeRequester.isConnected()) {
                    double value =  channel.get().getData().getDouble();
                    System.out.println("value " + value);
                } else {
                    System.out.println("did not issue get because connection lost");
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String valueIn = null;
                try {
                    valueIn = br.readLine();
                } catch (IOException ioe) {
                    System.out.println("IO error trying to read input!");
                }
                if(valueIn.equals("exit")) break;
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
