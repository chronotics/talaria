package org.chronotics.talaria.thrift;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.chronotics.talaria.thrift.gen.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

public class ThriftServer {
	
	private static final Logger logger = 
			LoggerFactory.getLogger(ThriftServer.class);
	
	private ThriftServerProperties properties;
	private TServer server = null;
	
	public ThriftServerProperties getProperties() {
		return properties;
	}
	
	public void setProperties(ThriftServerProperties _properties) {
		properties = _properties;
	}
	
	public void start(
			TransferService.Iface _service, 
			ThriftServerProperties _properties) {
		
		if(this.properties == null) {
			this.properties = new ThriftServerProperties();
		}
		
		if(properties.getIp() == _properties.getIp() &&  
				Integer.parseInt(properties.getPort()) 
				== Integer.parseInt(_properties.getPort())) {
			logger.error("The same ip address and port are already used");
			return;
		}
		
		this.properties.set(_properties);

		try {			
			TransferService.Processor<TransferService.Iface> processor = 
					new TransferService.Processor<TransferService.Iface>(_service);
			
			Runnable server = new Runnable() {
				public void run() {
					try {
						createServer(processor);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TTransportException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};      	      
			new Thread(server).start();
			
			while(!this.isRunning()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		if(server.isServing()) {
			server.stop();
			while(this.isRunning()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.info("Thrift server is stopped ... ");
		} else {
			logger.info("Thrift server is already stopped ... ");
		}
	}
	
	public boolean isRunning() {
		if(server == null) {
			return false;
		}
		return server.isServing() ? true : false;
	}
    
	public void createServer(
			TransferService.Processor<TransferService.Iface> processor) 
					throws UnknownHostException, TTransportException {
		
		InetAddress listenAddress = InetAddress.getByName(properties.getIp());
		TServerTransport serverTransport = new TServerSocket(
				new InetSocketAddress(listenAddress, Integer.parseInt(properties.getPort())));
//		// Simple server
//		server = new TSimpleServer(
//						new Args(serverTransport).processor(processor));
		// Use this for a multithreaded server
		server = new TThreadPoolServer(
						new TThreadPoolServer.Args(serverTransport).processor(processor));
		
		logger.info("Thrift server is started ... ");
		server.serve();
	}
	
	/**
	 * don't use this function
	 * should be modified
	 * @param processor
	 * @throws TTransportException
	 */
	public void createSecure(
			TransferService.Processor<TransferService.Iface> processor) throws TTransportException {

		int port = Integer.parseInt(properties.getSecurePort());
		String keyStore = properties.getSecureKeyStore();
		String keyPass = properties.getSecureKeyPass();

		/*
		 * Use TSSLTransportParameters to setup the required SSL parameters. In this example
		 * we are setting the keystore and the keystore password. Other things like algorithms,
		 * cipher suites, client auth etc can be set. 
		 */
		TSSLTransportParameters params = new TSSLTransportParameters();
		// The Keystore contains the private key
		params.setKeyStore(keyStore, keyPass, null, null);
		
		/*
		 * Use any of the TSSLTransportFactory to get a server transport with the appropriate
		 * SSL configuration. You can use the default settings if properties are set in the command line.
		 * Ex: -Djavax.net.ssl.keyStore=.keystore and -Djavax.net.ssl.keyStorePassword=thrift
		 * 
		 * Note: You need not explicitly call open(). The underlying server socket is bound on return
		 * from the factory class. 
		 */
		TServerTransport serverTransport = TSSLTransportFactory.getServerSocket(port, 0, null, params);
		//TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));
		
		// Use this for a multi threaded server
		server = new TThreadPoolServer(
				new TThreadPoolServer.Args(serverTransport).processor(processor));
		System.out.println("Starting the secure server...");
		server.serve();
	}
}