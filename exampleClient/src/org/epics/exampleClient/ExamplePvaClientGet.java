/*ExamplePvaClientGet.java */
/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
/**
 * @author mrk
 */

/* Author: Marty Kraimer */

package org.epics.exampleClient;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvdata.pv.Status;


public class ExamplePvaClientGet
{


    static void exampleDouble(PvaClient pva)
    {
        System.out.println("__exampleDouble__");
        double value;
        try {
            System.out.println("short way");
            value =  pva.channel("PVRdouble").get().getData().getDouble();
            System.out.println("as double " + value);
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
        }
        System.out.println("long way");
        PvaClientChannel pvaChannel = pva.createChannel("PVRdouble");
        pvaChannel.issueConnect();
        Status status = pvaChannel.waitConnect(2.0);
        if(!status.isOK()) {System.out.println(" connect failed"); return;}
        PvaClientGet pvaGet = pvaChannel.createGet();
        pvaGet.issueConnect();
        status = pvaGet.waitConnect();
        if(!status.isOK()) {System.out.println(" createGet failed"); return;}
        PvaClientGetData pvaData = pvaGet.getData();
        value = pvaData.getDouble();
        System.out.println("as double " + value);
    }

    static void exampleDoubleArray(PvaClient pva)
    {
        System.out.println("__exampleDoubleArray__");
        double[] value;
        try {
            System.out.println("short way");
            value =  pva.channel("PVRdoubleArray").get().getData().getDoubleArray();
            System.out.print("as doubleArray");
            for(int i=0; i<value.length; ++i) {
                System.out.print(" ");
                System.out.print(value[i]);
            }
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
        }
        try {
            System.out.println("long way");
            PvaClientChannel pvaChannel = pva.createChannel("PVRdoubleArray");
            pvaChannel.connect(2.0);
            PvaClientGet pvaGet = pvaChannel.createGet();
            PvaClientGetData pvaData = pvaGet.getData();
            value = pvaData.getDoubleArray();
            System.out.print("as doubleArray");
            for(int i=0; i<value.length; ++i) {
                System.out.print(" ");
                System.out.print(value[i]);
            }
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
        }
    }

    static void exampleCADouble(PvaClient pva)
    {
        System.out.println("__exampleCADouble__");
        double value;
        try {
            System.out.println("short way");
            value =  pva.channel("DBRdouble00","ca",5.0).get().getData().getDouble();
            System.out.println("as double " + value);
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
        }
        System.out.println("long way");
        PvaClientChannel pvaChannel = pva.createChannel("DBRdouble00","ca");
        pvaChannel.issueConnect();
        Status status = pvaChannel.waitConnect(2.0);
        if(!status.isOK()) {System.out.println(" connect failed"); return;}
        PvaClientGet pvaGet = pvaChannel.createGet();
        pvaGet.issueConnect();
        status = pvaGet.waitConnect();
        if(!status.isOK()) {System.out.println(" createGet failed"); return;}
        PvaClientGetData pvaData = pvaGet.getData();
        value = pvaData.getDouble();
        System.out.println("as double " + value);
    }

    static void exampleCADoubleArray(PvaClient pva)
    {
        System.out.println("__exampleCADoubleArray__");
        double[] value;
        try {
            System.out.println("short way");
            value =  pva.channel("DBRdoubleArray","ca",5.0).get().getData().getDoubleArray();
            System.out.print("as doubleArray");
            for(int i=0; i<value.length; ++i) {
                System.out.print(" ");
                System.out.print(value[i]);
            }
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
        }
        try {
            System.out.println("long way");
            PvaClientChannel pvaChannel = pva.createChannel("DBRdoubleArray","ca");
            pvaChannel.connect(2.0);
            PvaClientGet pvaGet = pvaChannel.createGet();
            PvaClientGetData pvaData = pvaGet.getData();
            value = pvaData.getDoubleArray();
            System.out.print("as doubleArray");
            for(int i=0; i<value.length; ++i) {
                System.out.print(" ");
                System.out.print(value[i]);
            }
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("exception " + e.getMessage());
        }
    }

    public static void main( String[] args )
    {
        System.out.println("_____examplePvaClientGet starting_______");
        PvaClient pva= PvaClient.get();
        exampleDouble(pva);
        exampleDoubleArray(pva);
        PvaClientChannel pvaChannel = pva.createChannel("DBRdouble00","ca");
        pvaChannel.issueConnect();
        Status status = pvaChannel.waitConnect(2.0);
        if(status.isOK()) {
            exampleCADouble(pva);
            exampleCADoubleArray(pva);
        } else {
            System.out.println("DBRdouble00 not found");
        }
        System.out.println("_____examplePvaClientGet done_______");
    }

}
