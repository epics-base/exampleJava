/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author mrk
 */

package org.epics.exampleJava.exampleClient;


import org.epics.pvaClient.*;
import org.epics.pvaClient.PvaClientRPC;
import org.epics.pvaClient.PvaClientRPCRequester;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

public class HelloWorldRPC
{
    static class ClientRPCRequester implements PvaClientRPCRequester
    {
        private boolean requestDoneCalled = false;
        
        public ClientRPCRequester(){}
        
        public void requestDone(
                Status status,
                PvaClientRPC pvaClientRPC,
                PVStructure pvResponse)
        {
            if(status.isOK()) {
                System.out.println("response\n" + pvResponse.toString());
                requestDoneCalled = true;
            } else {
                System.out.println("response error\n" + status.getMessage());
            }
        }
        public void waitResponse()
        {
            while(true)
            {
                if(requestDoneCalled) {
                    requestDoneCalled = false;
                    return;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.err.println("exception " + e.getMessage());
                }
            }
        }
    } 
    
    static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    static void exampleSimple(PvaClient pva,String channelName)
    {
        System.out.println("_____exampleSimple___");
        Structure  topStructure = fieldCreate.createFieldBuilder().
                add("value",ScalarType.pvString).
                createStructure();
        PVStructure pvRequest = pvDataCreate.createPVStructure(topStructure);
        PVString pvArgument = pvRequest.getSubField(PVString.class,"value");
        pvArgument.put("World");
        System.out.println("send " + pvArgument.get());
        try {
            PVStructure pvResult = pva.channel(channelName).rpc(pvRequest);
            System.out.println("result\n" + pvResult);
        }
        catch (RPCRequestException ex)
        {
            // The client connected to the server, but the server request method issued its 
            // standard summary exception indicating it couldn't complete the requested task.
            System.err.println("Acquisition of greeting was not successful, " +
                    "service responded with an error: " + ex.getMessage());
        }
        catch (Exception ex)
        {
            // The client failed to connect to the server. The server isn't running or
            // some other network related error occurred.
            System.err.println("RPC request failed , " + ex.getMessage());
        } 
    }

    static void exampleMore(PvaClient pva,String channelName)
    {
        System.out.println("_____exampleMore___");
        Structure  topStructure = fieldCreate.createFieldBuilder().
                add("value",ScalarType.pvString).
                createStructure();
        PVStructure pvRequest = pvDataCreate.createPVStructure(topStructure);
        PVString pvArgument = pvRequest.getSubField(PVString.class,"value");
        PvaClientRPC rpc = pva.channel(channelName).createRPC();
        try {
            pvArgument.put("World");
            System.out.println("send " + pvArgument.get());
            PVStructure pvResult = rpc.request(pvRequest);
            System.out.println("result\n" + pvResult);
            pvArgument.put("Again");
            System.out.println("send " + pvArgument.get());
            pvResult = rpc.request(pvRequest);
            System.out.println("result\n" + pvResult);
        }
        catch (RPCRequestException ex)
        {
            // The client connected to the server, but the server request method issued its 
            // standard summary exception indicating it couldn't complete the requested task.
            System.err.println("Acquisition of greeting was not successful, " +
                    "service responded with an error: " + ex.getMessage());
        }
        catch (Exception ex)
        {
            // The client failed to connect to the server. The server isn't running or
            // some other network related error occurred.
            System.err.println("RPC request failed , " + ex.getMessage());
        } 
    }
    
    static void exampleEvenMore(PvaClient pva,String channelName)
    {
        System.out.println("_____exampleEvenMore___");
        Structure  topStructure = fieldCreate.createFieldBuilder().
                add("value",ScalarType.pvString).
                createStructure();
        PVStructure pvRequest = pvDataCreate.createPVStructure(topStructure);
        PVString pvArgument = pvRequest.getSubField(PVString.class,"value");
        PvaClientChannel pvaChannel = pva.createChannel(channelName);
        pvaChannel.issueConnect();
        Status status = pvaChannel.waitConnect(2.0);
        if(!status.isOK()) {System.out.println(" connect failed"); return;}
        ClientRPCRequester requester = new ClientRPCRequester();
        PvaClientRPC rpc = pvaChannel.createRPC();
        rpc.issueConnect();
        status = rpc.waitConnect();
        if(!status.isOK()) {System.out.println(" rpc connect failed"); return;}
        try {
            pvArgument.put("World");
            System.out.println("send " + pvArgument.get());
            rpc.request(pvRequest, requester);
            requester.waitResponse();
            pvArgument.put("Again");
            System.out.println("send " + pvArgument.get());
            rpc.request(pvRequest, requester);
            requester.waitResponse();
            //rpc.setResponseTimeout(.001);
            pvArgument.put("Once again");
            System.out.println("send " + pvArgument.get());
            System.out.println("This should succeed");
            rpc.request(pvRequest, requester);
            requester.waitResponse();
            System.out.println("This should fail");
            rpc.request(pvRequest, requester);
            rpc.request(pvRequest, requester);
        }
        catch (RPCRequestException ex)
        {
            // The client connected to the server, but the server request method issued its 
            // standard summary exception indicating it couldn't complete the requested task.
            System.err.println("Acquisition of greeting was not successful, " +
                    "service responded with an error: " + ex.getMessage());
        }
        catch (Exception ex)
        {
            // The client failed to connect to the server. The server isn't running or
            // some other network related error occurred.
            System.err.println("RPC request failed , " + ex.getMessage());
        }
    }

    public static void main( String[] args )
    {
        System.out.println("_____HelloWorldRPC starting_______");
        PvaClient pva= PvaClient.get("pva");
        try {
            String channelName = "PVRhelloRPC";
            exampleSimple(pva,channelName);
            exampleMore(pva,channelName);
            exampleEvenMore(pva,channelName);
            System.out.println("_____HelloWorldRPC done_______");
        }
        catch (Exception e)
        {
            System.err.println("exception " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
