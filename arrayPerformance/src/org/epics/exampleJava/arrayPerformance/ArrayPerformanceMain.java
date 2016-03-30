package org.epics.exampleJava.arrayPerformance;

import java.io.Console;

import org.epics.pvdatabase.pva.ContextLocal;

public class ArrayPerformanceMain {

    public static void main(String[] args)
    {
        int argc = args.length;

        String recordName;
        recordName = "arrayPerformance";
        int size = 10000000;
        double delay = .0001;
        String providerName ="local";
        int nMonitor = 1;
        if(argc==1 && args[0].endsWith("-help")) {
            System.out.println("recordName size delay providerName nMonitor");
            System.out.println("default");
            System.out.print(recordName +" " + size + " " + delay + " " + providerName );
            System.out.println(" " + nMonitor);
            System.exit(0);
        }
        if(argc>0) recordName = args[0];
        if(argc>1) size = Integer.parseInt(args[1]);
        if(argc>2) delay = Double.parseDouble(args[2]);
        if(argc>3) providerName = args[3];
        if(argc>4) nMonitor = Integer.parseInt(args[4]);
        System.out.print("arrayPerformance ");
        System.out.print(recordName + " ");
        System.out.print(size + " ");
        System.out.print(delay + " ");
        System.out.print(providerName + " ");
        System.out.println(nMonitor);
        ContextLocal context = new ContextLocal();
        context.start(false);
        ArrayPerformance arrayPerformance = ArrayPerformance.create(recordName,size,delay);
        try {
            LongArrayMonitor[] longArrayMonitor = new LongArrayMonitor[nMonitor];
            for(int i=0; i<nMonitor; ++i) longArrayMonitor[i]= new LongArrayMonitor(providerName,recordName);
            while(true) {
                Console cnsl = null;
                try{
                    cnsl = System.console();
                    if (cnsl != null) {
                        String value = System.console().readLine("waiting for exit: ");
                        if(value.equals("exit")) {
                            for(int i=0; i<nMonitor; ++i) longArrayMonitor[i].stop();
                            arrayPerformance.stop();
                            context.destroy();
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
        System.out.println("arrayPerformance exiting");
    }
}
