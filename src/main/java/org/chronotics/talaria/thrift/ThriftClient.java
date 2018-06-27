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
	private TransferService.Client client = null;
	private TTransport transport = null;
	
	public ThriftClientProperties getProperties() {
		return properties;
	}
	
	public void setProperties(ThriftClientProperties _properties) {
		properties = _properties;
	}
	
	public TransferService.Client getService() {
		return client;
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
		
		if(client == null) {
			client = new TransferService.Client(protocol);
		}
	}
	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
///*	    if (args.length != 1) {
//	        System.out.println("Please enter 'simple' or 'secure'");
//	        System.exit(0);
//	      }*/
//
//	      try {
//	        TTransport transport;
//	        
//	        transport = new TSocket("192.168.0.92", 9091);
//	          transport.open();
//	          
//	          FactoryTTransport TTransportFactory=FactoryTTransport.getInstance();
//	          TTransportFactory.put("192.168.0.92:9091",transport);
//	          
///*	        if (args[0].contains("simple")) {
//	          transport = new TSocket("localhost", 9090);
//	          transport.open();
//	        }
//	        else {
//	          
//	           * Similar to the server, you can use the parameters to setup client parameters or
//	           * use the default settings. On the client side, you will need a TrustStore which
//	           * contains the trusted certificate along with the public key. 
//	           * For this example it's a self-signed cert. 
//	           
//	          TSSLTransportParameters params = new TSSLTransportParameters();
//	          params.setTrustStore("../../lib/java/test/.truststore", "thrift", "SunX509", "JKS");
//	          
//	           * Get a client transport instead of a server transport. The connection is opened on
//	           * invocation of the factory method, no need to specifically call open()
//	           
//	          transport = TSSLTransportFactory.getClientSocket("localhost", 9091, 0, params);
//	        }*/  
//	        
////	        TTransport transport=TransportFactory.getInstance().get("192.168.0.92");
//	    	TProtocol protocol = new  TBinaryProtocol(transport);
//	    	TransferService.Client client = new TransferService.Client(protocol);
//	        System.out.println("f1 is called");
////				client.callFunction1();
//			System.out.println("f1 ends");
//	        
//	        
//	        transport.close();
//	        
//	        System.out.println("end");
//	      } catch (TException x) {
//	        x.printStackTrace();
//	      } 
//	}
//
//	      private static void perform(TransferService.Client client) throws TException
//	      {
////	        client.callFunction1();
//	        System.out.println("ping()");
//
///*	        int sum = client.add(1,1);
//	        System.out.println("1+1=" + sum);
//
//	        Work work = new Work();
//
//	        work.op = Operation.DIVIDE;
//	        work.num1 = 1;
//	        work.num2 = 0;
//	        try {
//	          int quotient = client.calculate(1, work);
//	          System.out.println("Whoa we can divide by 0");
//	        } catch (InvalidOperation io) {
//	          System.out.println("Invalid operation: " + io.why);
//	        }
//
//	        work.op = Operation.SUBTRACT;
//	        work.num1 = 15;
//	        work.num2 = 10;
//	        try {
//	          int diff = client.calculate(1, work);
//	          System.out.println("15-10=" + diff);
//	        } catch (InvalidOperation io) {
//	          System.out.println("Invalid operation: " + io.why);
//	        }
//
//	        SharedStruct log = client.getStruct(1);
//	        System.out.println("Check log: " + log.value);*/
//	      }

}
