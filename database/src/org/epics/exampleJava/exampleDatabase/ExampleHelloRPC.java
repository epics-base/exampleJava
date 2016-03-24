// Copyright information and license terms for this software can be
// found in the file LICENSE that is included with the distribution

/**
 * @author mrk
 * @date 2013.07.24
 */


package org.epics.exampleJava.exampleDatabase;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvaccess.server.rpc.*;
import org.epics.pvdatabase.*;

public class ExampleHelloRPC extends PVRecord {
	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();


	private final static Structure resultStructure = fieldCreate.createFieldBuilder().
			add("value",ScalarType.pvString).createStructure();

	private final static PVStructure pvResult = pvDataCreate
			.createPVStructure(resultStructure);

	private boolean     underControl = false;

	synchronized boolean takeControl() {
		if (!underControl) {
			underControl = true;
			return true;
		}
		return false;
	}

	synchronized void releaseControl() {
		underControl = false;
	}

	static class RPCServiceImpl implements RPCService {

		private ExampleHelloRPC pvRecord;

		RPCServiceImpl(ExampleHelloRPC record) {
			pvRecord = record;
		}

		public PVStructure request(PVStructure args) throws RPCRequestException
		{
			boolean haveControl = pvRecord.takeControl();
			if (!haveControl)
				throw new RPCRequestException(StatusType.ERROR,
						"Device busy");
			PVString pvFrom = args.getSubField(PVString.class,"value");
			if (pvFrom == null)
				throw new RPCRequestException(StatusType.ERROR,
						"PVString field with name 'value' expected.");

			PVString pvTo = pvResult.getSubField(PVString.class,"value");
			pvTo.put("Hello " + pvFrom.get());
			pvRecord.releaseControl();
			return pvResult;

		}
	}

	public static ExampleHelloRPC create(String recordName)
	{

		ExampleHelloRPC pvRecord = new ExampleHelloRPC(recordName,pvResult);
		PVDatabase master = PVDatabaseFactory.getMaster();
		master.addRecord(pvRecord);
		return pvRecord;
	}

	public ExampleHelloRPC(String recordName, PVStructure pvStructure) {
		super(recordName, pvStructure);
		process();
	}


	public Service getService(PVStructure pvRequest)
	{
		return new RPCServiceImpl(this);
	}

}
