package org.epics.exampleJava.arrayPerformance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.pva.ChannelProviderLocalFactory;

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
        try {
        	PVDatabase master = PVDatabaseFactory.getMaster();
        	ChannelProvider channelProvider = ChannelProviderLocalFactory.getChannelServer();
        	ServerContextImpl context = ServerContextImpl.startPVAServer(PVAConstants.PVA_ALL_PROVIDERS,0,true,null);
        	PvaClient pva= PvaClient.get();
        	ArrayPerformance arrayPerformance = ArrayPerformance.create(recordName,size,delay);
        	master.addRecord(arrayPerformance);
        	arrayPerformance.startThread();
        	LongArrayMonitor[] longArrayMonitor = new LongArrayMonitor[nMonitor];
        	for(int i=0; i<nMonitor; ++i) longArrayMonitor[i]= new LongArrayMonitor(providerName,recordName);
        	while(true) {
        		System.out.print("waiting for exit: ");
        		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        		String value = null;
        		try {
        			value = br.readLine();
        		} catch (IOException ioe) {
        			System.out.println("IO error trying to read input!");
        		}
        		if(value.equals("exit")) break;
        	}
        	for(int i=0; i<nMonitor; ++i) {
                longArrayMonitor[i].stop();
            }
            arrayPerformance.stop();
        	context.destroy();
        	master.destroy();
        	channelProvider.destroy();
        	pva.destroy();
        } catch (PVAException e) {
        	System.out.println("exception " + e.getMessage());
        	System.exit(1);
        }
        System.out.println("arrayPerformance exiting");
    }
}
