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

public class LongArrayMonitorMain {

    public static void main(String[] args)
    {
        int argc = args.length;
        String channelName = "arrayPerformance";
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("channelName");
            System.out.println("default");
            System.out.println(channelName);
            System.exit(0);
        }
        if(argc>0) channelName = args[0];
        try {
            LongArrayMonitor longArrayMonitor = new LongArrayMonitor("pva",channelName);
            while(true) {
                Console cnsl = null;
                try{
                    cnsl = System.console();
                    if (cnsl != null) {
                        String value = System.console().readLine("waiting for exit: ");
                        if(value.equals("exit")) {
                            longArrayMonitor.stop();
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

        System.out.println("longArrayMonitorMain exiting");

    }
}
