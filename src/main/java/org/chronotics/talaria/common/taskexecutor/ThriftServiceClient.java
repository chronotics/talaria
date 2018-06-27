package org.chronotics.talaria.common.taskexecutor;

import org.apache.thrift.protocol.TProtocol;
import org.chronotics.talaria.thrift.gen.TransferService;

public class ThriftServiceClient extends TransferService.Client {

	public ThriftServiceClient(TProtocol prot) {
		super(prot);
	}

}
