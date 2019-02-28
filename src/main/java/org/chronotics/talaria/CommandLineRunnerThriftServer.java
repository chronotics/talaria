package org.chronotics.talaria;

import org.chronotics.talaria.common.ChainExecutor;
import org.chronotics.talaria.common.chainexecutor.BypassExecutor;
import org.chronotics.talaria.common.chainexecutor.NullReturnExecutor;
import org.chronotics.talaria.thrift.thriftservicehandler.ThriftServiceWithMessageQueue;
import org.chronotics.talaria.thrift.ThriftServer;
import org.chronotics.talaria.thrift.ThriftServerProperties;
import org.chronotics.talaria.thrift.ThriftServiceExecutor;
import org.chronotics.talaria.thrift.ThriftServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CommandLineRunnerThriftServer implements CommandLineRunner {

	private static final Logger logger = 
			LoggerFactory.getLogger(CommandLineRunnerThriftServer.class);
					
	@Autowired
	private ApplicationContext context;
	
	private static ThriftServer thriftServer = null;
	
	@Override
	public void run(String... arg0) throws Exception {
		
		TalariaProperties properties = 
				(TalariaProperties)context.getBean("talariaProperties");
		assert(properties != null);
		if(properties == null) {
			return;
		}

		// thrift server properties
		ThriftServerProperties thriftServerProperties = 
				properties.getThriftServerProperties();
		assert(thriftServerProperties != null);
		if(thriftServerProperties == null) {
			return;
		}
		assert(!thriftServerProperties.isNull());
		if(thriftServerProperties.isNull()) {
			logger.error("ThriftServerProperties is null");
			return;
		}
		
		// start thrift server
		/**
		 * start Thrift server
		 * BypassExecutor to Read: bypass a read value
		 * NullReturnExecutor to Write: write and return null
		 */
		ChainExecutor<Object> executorToRead =
				new BypassExecutor<>();
		ChainExecutor<Object> executorToWrite =
				new NullReturnExecutor<>();
		ThriftServiceExecutor thriftServiceExecutor =
				new ThriftServiceExecutor(executorToRead, executorToWrite);
		ThriftServiceHandler thriftServiceHandler =
				new ThriftServiceWithMessageQueue(thriftServiceExecutor);

		thriftServer = new ThriftServer(thriftServiceHandler, thriftServerProperties);
		thriftServer.start();

		logger.info("Thrift server is started...");
		logger.info("URL of Thrift server is {}:{}",
				thriftServerProperties.getIp(),
				thriftServerProperties.getPort());
	}
}
