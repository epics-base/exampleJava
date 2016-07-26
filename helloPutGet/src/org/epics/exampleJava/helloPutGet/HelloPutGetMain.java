/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.helloPutGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.ChannelProviderLocalFactory;


/**
 * @author Marty Kraimer
 *
 */
public class HelloPutGetMain {

    public static void main(String[] args)
    {
        try {
            PVDatabase master = PVDatabaseFactory.getMaster();
            ChannelProvider channelProvider = ChannelProviderLocalFactory.getChannelProviderLocal();
            String recordName = "helloPutGet";
            PVRecord pvRecord = HelloPutGetRecord.create(recordName);
            master.addRecord(pvRecord);
            ServerContextImpl context = ServerContextImpl.startPVAServer(channelProvider.getProviderName(),0,true,null);
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
            context.destroy();
            master.destroy();
            channelProvider.destroy();
        } catch (PVAException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("ExampleLink exiting");
    }
}
