/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 *
 */

package org.epics.exampleJava.arrayPerformance;

import java.io.Console;

public class LongArrayGetMain {

    public static void main(String[] args)
    {
        int argc = args.length;
        String channelName = "arrayPerformance";
        int iterBetweenCreateChannel = 0;
        int iterBetweenCreateChannelGet = 0;
        double delayTime = 1.0;
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("channelName iterBetweenCreateChannel iterBetweenCreateChannelGet delayTime");
            System.out.println("default");
            System.out.print(channelName);
            System.out.print(" " + iterBetweenCreateChannel);
            System.out.print(" " + iterBetweenCreateChannelGet);
            System.out.println(" " + delayTime);
            System.exit(0);
        }
        if(argc>0) channelName = args[0];
        if(argc>1) iterBetweenCreateChannel = Integer.parseInt(args[1]);
        if(argc>2) iterBetweenCreateChannelGet = Integer.parseInt(args[2]);
        if(argc>3) delayTime = Double.parseDouble(args[3]);
        try {
            LongArrayGet longArrayGet = new LongArrayGet(
                    "pva",channelName,iterBetweenCreateChannel,iterBetweenCreateChannelGet,delayTime);
            while(true) {
                Console cnsl = null;
                try{
                    cnsl = System.console();
                    if (cnsl != null) {
                        String value = System.console().readLine("waiting for exit: ");
                        if(value.equals("exit")) {
                            longArrayGet.stop();
                            System.exit(0);
                        }
                    }

                }catch(Exception ex){
                    System.out.println("exception " + ex.getMessage());
                    System.exit(1);  
                }
            }
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
            System.exit(1);
        }
        System.out.println("longArrayGetMain exiting");

    }
}
