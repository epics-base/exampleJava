/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author Dave Hickin
 *
 */

package org.epics.exampleJava.pvDatabaseRPC;

import java.util.Arrays;
import org.epics.pvaccess.client.rpc.RPCClient;
import org.epics.pvaccess.client.rpc.RPCClientFactory;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.*;
import org.epics.pvdata.pv.*;


public class Control
{
    static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    static final double REQUEST_TIMEOUT = 3.0;
    static final String DEVICE_NAME = "mydevice";

    static Structure requestStructure = fieldCreate.createFieldBuilder().
            add("method", ScalarType.pvString).
            createStructure();

    static Structure pointStructure = fieldCreate.createFieldBuilder().
            setId("point_t").
            add("x", ScalarType.pvDouble).
            add("y", ScalarType.pvDouble).
            createStructure();

    static Structure argStructure = fieldCreate.createFieldBuilder().
            createStructure();

    static Structure configArgStructure = fieldCreate.createFieldBuilder().
            addArray("value", pointStructure).
            createStructure();

    static Structure rewindArgStructure = fieldCreate.createFieldBuilder().
            add("value", ScalarType.pvInt).
            createStructure();

    private static void usage() {
        System.out.println("Usage:\n"
                +  "Run application supplying the arguments:\n"
                +  " <command> [<command-arguments>]\n"
                +  "Controls a device (" + DEVICE_NAME + ")\n" 
                +  "Available commands are:\n"
                +  "help\n"
                +  "configure\n"
                +  "run\n"
                +  "pause\n"
                +  "resume\n"
                +  "rewind\n"
                +  "stop\n"
                +  "scan\n"
                +  "abort.\n"
                + "<command-arguments> is command-dependent.\n"
                +  "For help on commands run supplying the arguments:\n"
                +  "help <command>\n"
                );
    }

    private static void usage(String command) {
        if (command.equals("configure"))
            usage_configure();
        else if (command.equals("run"))
            usage_run();
        else if (command.equals("scan"))
            usage_scan();
        else if (command.equals("pause"))
            usage_pause();
        else if (command.equals("resume"))
            usage_resume();
        else if (command.equals("rewind"))
            usage_rewind();
        else if (command.equals("stop"))
            usage_stop();
        else if (command.equals("abort"))
            usage_abort();
        else if (command.equals("help"))
            usage_help();
        else
        {
            System.out.println("Unknown command " + command);
            usage();
        }
    }


    private static void usage_help() {
        System.out.println("Usage:\n"
                +  "For help on commands run supplying the arguments:\n"
                +  "help <command>\n"
                );
    }

    private static void usage_configure() {
        System.out.println("Usage:\n"
                +  "Run application supplying the arguments:\n"
                +  "configure x_1 y_1 [x_2 y_2] ... [x_n y_n]\n"
                +  "Sets the sequence of points through which "
                +  DEVICE_NAME + " will move\n"
                +  "to (x_i,y_i), i = 1..n"
                +  " and changes state to READY.\n"
                +  "Device must be IDLE.\n"
                );
    }

    private static void usage_run() {
        System.out.println("Usage:\n"
                +  "Run application supplying the argument:\n"
                +  "run\n"
                +  "Starts the device moving through the sequence of points supplied on\n"
                +  "configuration, changes state to RUNNING and returns.\n"             +  "No additional arguments are required.\n"
                +  "Device must be READY.\n"
                );
    }

    private static void usage_scan() {
        System.out.println("Usage:\n"
                +  "Run application supplying the argument:\n"
                +  "scan\n"
                +  "Starts the device moving through the sequence of points supplied on\n"
                +  "configuration, changes state to RUNNING and blocks until completion.\n"
                +  "No additional arguments are required.\n"
                +  "Returns an error if scan is stopped or aborted.\n"
                +  "Device must be READY.\n"
                );
    }

    private static void usage_pause() {
        System.out.println("Usage:\n"
                +  "Run application supplying the argument:\n"
                +  "pause\n"
                +  "Pauses the current scan and changes state to PAUSED.\n"
                +  "No additional arguments are required.\n"
                +  "Device must be RUNNING.\n"
                );
    }

    private static void usage_resume() {
        System.out.println("Usage:\n"
                +  "Run application supplying the argument:\n"
                +  "resume\n"
                +  "Resumes the current scan and changes state to RUNNING.\n"
                +  "No additional arguments are required.\n"
                +  "Device must be PAUSED.\n"
                );
    }

    private static void usage_rewind() {
        System.out.println("Usage:\n"
                +  "Run application supplying the arguments:\n"
                +  "rewind <steps>\n"
                +  "Rewinds the current scan the requested number of steps or to start.\n"
               +  "Required argument is the number of steps, which must be non-negative.\n"
                +  "Device must be RUNNING or PAUSED.\n"
                );
    }

    private static void usage_stop() {
        System.out.println("Usage:\n"
                +  "Run application supplying the argument:\n"
                +  "stop\n"
                +  "Stops any scan in progress and changes state to READY.\n"
                +  "Blocking scan operation in progress will return an error.\n"
                +  "No additional arguments are required.\n"
                +  "Device can be any state except IDLE.\n"
                );
    }

    private static void usage_abort() {
        System.out.println("Usage:\n"
                +  "Run application supplying the argument:\n"
                +  "abort\n"
                +  "Aborts any scan or motion in progress and changes state to IDLE.\n"
                +  "Blocking scan operation in progress will return an error.\n"
                +  "No additional arguments are required.\n"
                +  "Device can be any state.\n"
                );
    }

    private static void checkArgs(String[] args)
    {
        int argc = args.length;
        if (argc == 0)
        {
            usage();
            System.exit(1);
        }
            
        if (args[0].equals("configure"))
        {
            if(argc < 3)
            {
                System.out.println("configure  must have at least one pair of position coordinate arguments.");
                usage_configure();
                System.exit(1);
            }

            if ((argc % 2) != 1)
            {
                System.out.println("configure requires an even number arguments.");
                usage_configure();
                System.exit(1);
            }
        }

        if (args[0].equals("rewind") && argc < 2)
        {
            System.out.println("rewind requires an argument.");
            usage_rewind();
            System.exit(1);
        }
    }

    private static PVStructure createConfigArgs(String[] args)
    {
        PVStructure pvArgs = pvDataCreate.createPVStructure(configArgStructure);

        PVStructureArray pvStructureArray = pvArgs.getSubField(PVStructureArray.class, "value");

        final int npoints = args.length/2;
        PVStructure[] values = new PVStructure[npoints];
        int indarg = 0;
        for(int i=0; i < npoints; ++i) {
            values[i] = pvDataCreate.createPVStructure(pointStructure);
            PVDouble pvDouble = values[i].getSubField(PVDouble.class,"x");
            pvDouble.put(Double.valueOf(args[indarg++]));
            pvDouble = values[i].getSubField(PVDouble.class,"y");
            pvDouble.put(Double.valueOf(args[indarg++]));
        }
        pvStructureArray.put(0, npoints, values, 0);
        pvStructureArray.setLength(npoints);
        return pvArgs;
    }

    private static PVStructure createRewindArgs(String arg)
    {
        PVStructure pvArgs = pvDataCreate.createPVStructure(rewindArgStructure);
        pvArgs.getSubField(PVInt.class, "value").put(Integer.valueOf(arg));
        return pvArgs;
    }


    public static void main( String[] args)
    {
        checkArgs(args);

        if(args[0].equals("help") || args[0].equals("-help"))
        {
            if (args.length >= 2)
                usage(args[1]);
            else
                usage();
            return;
        }

        PVStructure pvRequest = pvDataCreate.createPVStructure(requestStructure);
        pvRequest.getSubField(PVString.class, "method").put(args[0]);

        double timeout = 3.0;
        PVStructure pvArgs = null;
        if (args[0].equals("configure"))
            pvArgs = createConfigArgs(Arrays.copyOfRange(args, 1, args.length));
        else if (args[0].equals("rewind"))
            pvArgs = createRewindArgs(args[1]);
        else
            pvArgs = pvDataCreate.createPVStructure(argStructure);

        if (args[0].equals("scan"))
            timeout = 1e9;

        org.epics.pvaccess.ClientFactory.start();
        try {
            RPCClient rpcClient = RPCClientFactory.create(DEVICE_NAME, pvRequest);
            PVStructure pvResult = rpcClient.request(pvArgs, timeout);
            //System.out.println(pvResult);
            System.out.println("Done");
        } catch (RPCRequestException e) {
            System.out.println("exception " + e.getMessage());
        }
        finally
        {
            // Stop pvAccess client, so that this application exits cleanly.
            org.epics.pvaccess.ClientFactory.stop();
        }
    }

}
