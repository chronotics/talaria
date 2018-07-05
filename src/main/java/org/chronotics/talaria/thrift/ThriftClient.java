package org.chronotics.talaria.thrift;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import org.chronotics.talaria.thrift.gen.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftClient {

	private static final Logger logger = 
			LoggerFactory.getLogger(ThriftServer.class);
	
	private ThriftClientProperties properties = null;
	private TransferService.Client service = null;
	private TTransport transport = null;

	public ThriftClient() {}

	public ThriftClientProperties getProperties() {
		return properties;
	}
	
	public void setProperties(ThriftClientProperties _properties) {
		properties = _properties;
	}
	
	public TransferService.Client getService() {
		return service;
	}
	
	public void start(ThriftClientProperties _properties) {
		if(this.properties == null) {
			this.properties = new ThriftClientProperties();
		}
		
		if(properties.getIp() == _properties.getIp() &&  
				Integer.parseInt(properties.getPort()) 
				== Integer.parseInt(_properties.getPort())) {
			logger.error("The same ip address and port are already used");
			return;
		}
		
		this.properties.set(_properties);
		
		this.createClinet(_properties);
	}
	
	public void stop() {
		if(transport != null) {
			transport.close();
		} else {
			logger.info("client is not created");
		}
	}
	
	private void createClinet(ThriftClientProperties _properties) {
		if(transport == null) {
			transport = new TSocket(
							_properties.getIp(),
							Integer.valueOf(_properties.getPort()));
		} else {
			logger.info("transport is already created");
		}
		
		logger.info("ip:port is {}:{}",
				_properties.getIp(),
				_properties.getPort());
		
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
			service = new TransferService.Client(protocol);
		}
	}
	
}
