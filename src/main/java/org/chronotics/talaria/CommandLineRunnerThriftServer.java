package org.chronotics.talaria;

import org.chronotics.talaria.common.taskexecutor.ThriftServiceWithMessageQueue;
import org.chronotics.talaria.thrift.ThriftServer;
import org.chronotics.talaria.thrift.ThriftServerProperties;
import org.chronotics.talaria.thrift.ThriftService;
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
	
	static private ThriftServer thriftServer = null;
	
	@Override
	public void run(String... arg0) throws Exception {
		
		TalariaProperties properties = 
				(TalariaProperties)context.getBean("talariaProperties");
					
		assert(properties != null);
		if(properties == null) {
			return;
		}
//		if(properties.isNull()) {
//			logger.error("TalariaProperties is null");
//			return;
//		}
		
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
		ThriftService thriftServiceHandler = new ThriftServiceWithMessageQueue(null);
		thriftServer = new ThriftServer();
		thriftServer.start(thriftServiceHandler,thriftServerProperties);
	}
}
