package org.chronotics.talaria.thrift;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
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

	public static int delay_to_start = 10; // ms
	public static int delay_to_stop = 10; // ms
	public static int timeout_to_start = 2000; // ms
	public static int timeout_to_stop = 2000; // ms

	public static enum SERVERTYPE {
		SIMPLE("simple"),
		THREADPOOL("threadpool");

		private String type;

		SERVERTYPE(String _type) {
			type = _type;
		}

		public String toString() { return type; }
	}

	private TransferService.Processor<TransferService.Iface> processor;
	private ThriftServerProperties properties;
	private TServer server = null;

	private ThriftServer() {}

	public ThriftServer(
		TransferService.Iface _service,
		ThriftServerProperties _properties) {
		this.properties = _properties;
		processor = new TransferService.Processor<TransferService.Iface>(_service);
	}

	public ThriftServerProperties getProperties() {
		return properties;
	}
	
	public void setProperties(
			ThriftServerProperties _properties) {
		properties = _properties;
	}
	
	public void start() {
        Runnable serverService = new Runnable() {
        	@Override
            public void run() {
                try {
                    SERVERTYPE type;
                    if(properties.getServerType().equals(SERVERTYPE.SIMPLE.toString())) {
                        type = SERVERTYPE.SIMPLE;
                    } else if(properties.getServerType().equals(SERVERTYPE.THREADPOOL.toString())) {
                        type = SERVERTYPE.THREADPOOL;
                    } else {
                        logger.error(properties.getServerType());
                        logger.error("Unknown Thrift server type");
                        return;
                    }
                    createServer(processor, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(serverService).start();

        int t = 0;
        while(!this.isRunning()) {
            try {
                Thread.sleep(delay_to_start);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            t += delay_to_start;
            if(t > timeout_to_start) {
                this.server.stop();
                logger.error("Timeout occurred when thrift server starts");
                break;
            }
        }
	}
	
	public void stop() {
		if(server.isServing()) {
			server.stop();
			int t = 0;
			while(this.isRunning()) {
				try {
					Thread.sleep(delay_to_stop);
					logger.info("waiting for the stop of Thrift server...");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				t += delay_to_stop;
				if(t > timeout_to_stop) {
				    logger.error("Timeout occurred when thrift server stops");
				    break;
                }
			}
			logger.info("Thrift server is stopped ... ");
		} else {
			logger.info("Thrift server is already stopped ... ");
		}
	}
	
	public boolean isRunning() {
		if(server == null) {
			logger.info("Thrift server is null. This can be seen few times when the process starts or ends");
			return false;
		}
		return server.isServing() ? true : false;
	}
    
	private void createServer(
			TransferService.Processor<TransferService.Iface> processor,
			SERVERTYPE _type)
			throws Exception {
		
		InetAddress listenAddress = InetAddress.getByName(properties.getIp());
		TServerTransport serverTransport = new TServerSocket(
				new InetSocketAddress(listenAddress, Integer.parseInt(properties.getPort())));

		if(_type == SERVERTYPE.SIMPLE) {
			// Simple server
			server = new TSimpleServer(
					new TServer.Args(serverTransport).processor(processor));
		} else if(_type == SERVERTYPE.THREADPOOL) {
			// Use this for a multithreaded server
			server = new TThreadPoolServer(
					new TThreadPoolServer.Args(serverTransport).processor(processor));
		} else {
			logger.error("Unknown Thrift server type");
			throw new Exception("Unknown Thrift server type");
		}

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