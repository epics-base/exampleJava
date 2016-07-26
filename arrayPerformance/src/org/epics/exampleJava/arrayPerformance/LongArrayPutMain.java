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

public class LongArrayPutMain {

    public static void main(String[] args)
    {
        int argc = args.length;
        String channelName = "arrayPerformance";
        int arraySize = 10;
        int iterBetweenCreateChannel = 0;
        int iterBetweenCreateChannelPut = 0;
        double delayTime = 1.0;
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("channelName arraySize iterBetweenCreateChannel "
                    + "iterBetweenCreateChannelPut delayTime");
            System.out.println("default");
            System.out.print(channelName);
            System.out.print(" " + arraySize);
            System.out.print(" " + iterBetweenCreateChannel);
            System.out.print(" " + iterBetweenCreateChannelPut);
            System.out.println(" " + delayTime);
            System.exit(0);
        }
        if(argc>0) channelName = args[0];
        if(argc>1) arraySize = Integer.parseInt(args[1]);
        if(argc>2) iterBetweenCreateChannel = Integer.parseInt(args[2]);
        if(argc>3) iterBetweenCreateChannelPut = Integer.parseInt(args[3]);
        if(argc>4) delayTime = Double.parseDouble(args[4]);
        try {
            LongArrayPut longArrayPut = new LongArrayPut(
                    "pva",
                    channelName,
                    arraySize,
                    iterBetweenCreateChannel,
                    iterBetweenCreateChannelPut,
                    delayTime);
            while(true) {
                Console cnsl = null;
                try{
                    cnsl = System.console();
                    if (cnsl != null) {
                        String value = System.console().readLine("waiting for exit: ");
                        if(value.equals("exit")) {
                            longArrayPut.stop();
                            System.exit(0);
                        }
                    }

                }catch(Exception ex){
                    // if any error occurs
                    ex.printStackTrace();      
                }
            }
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
            System.exit(1);
        }

        System.out.println("longArrayPutMain exiting");

    }
}
