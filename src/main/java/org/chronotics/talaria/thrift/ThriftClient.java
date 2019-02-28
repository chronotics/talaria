package org.chronotics.talaria.thrift;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import org.chronotics.talaria.thrift.gen.ThriftRWService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftClient {

	private static final Logger logger = 
			LoggerFactory.getLogger(ThriftServer.class);
	
	private ThriftClientProperties properties = null;
	private ThriftRWService.Client service = null;
//	private ThriftRWService.Iface service = null;
	private TTransport transport = null;

	private ThriftClient() {}

	public ThriftClient(ThriftClientProperties _properties) {
		properties = _properties;
	}

	public ThriftClientProperties getProperties() {
		return properties;
	}
	
	public void setProperties(ThriftClientProperties _properties) {
		properties = _properties;
	}
	
	public ThriftRWService.Client getService() {
		return service;
	}
//	public ThriftRWService.Iface getService() {
//		return service;
//	}
	
	public void start() {
		this.createClinet();
	}
	
	public void stop() {
		if(transport != null) {
			transport.close();
		} else {
			logger.info("client is not created");
		}
	}
	
	private void createClinet() {
		assert(this.properties != null);
		if(this.properties == null) {
			logger.error("ThriftClientProperties is null");
			return;
		}

//		if(properties.getIp() == _properties.getIp() &&
//				Integer.parseInt(properties.getPort())
//				== Integer.parseInt(_properties.getPort())) {
//			logger.error("The same ip address and port are already used");
//			return;
//		}
//
//		this.properties.set(_properties);
		if(transport == null) {
			transport = new TSocket(
							properties.getIp(),
							Integer.valueOf(properties.getPort()));
		} else {
			logger.info("transport is already created");
		}
		
		logger.info("ip:port is {}:{}",
				properties.getIp(),
				properties.getPort());
		
		if(!transport.isOpen()) {
			try {
				transport.open();
			} catch (TTransportException e) {
				e.printStackTrace();
				logger.error(e.toString());
				return;
			}
		} else {
			logger.info("transport for thrift client is already opened");
		}
		
		TProtocol protocol = new TBinaryProtocol(transport);
		
		if(service == null) {
			service = new ThriftRWService.Client(protocol);
//			service = new ThriftServiceWithMessageQueue(null);
		}
	}
	
}
