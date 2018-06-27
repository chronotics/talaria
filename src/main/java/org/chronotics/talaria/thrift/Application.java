package org.chronotics.talaria.thrift;

import org.chronotics.talaria.common.taskexecutor.ThriftServiceWithMessageQueue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"org.chronotics.talaria.thrift"})
public class Application {
	
	private static ThriftServer thriftServer = null;
	public static void main(String[] args) {
		// run spring boot
		ApplicationContext context = SpringApplication.run(Application.class,args);

//		// getProperties
//		TalariaProperties properties = 
//				(TalariaProperties)context.getBean("talariaProperties");
//		String queueMapKey = properties.getQueueMapKey();
//		ThriftServerProperties thriftServerProperties = properties.getThriftServerProperties();
//		if(thriftServerProperties == null) {
//			System.out.println("check DI injection of ThriftServerProperties");
//			return;
//		}
		
		ThriftServerProperties thriftServerProperties = 
				(ThriftServerProperties)context.getBean("thriftServerProperties");
		if(thriftServerProperties == null) {
			System.out.println("check DI injection of ThriftServerProperties");
			return;
		}

//		// start thrift server
//		ThriftService thriftServiceHandler = 
//				new ThriftServiceWithMessageQueue(null);
//		thriftServer = new ThriftServer();
//		thriftServer.start(thriftServiceHandler,thriftServerProperties);
		// thrift server properties

		
		// start thrift server
		ThriftService thriftServiceHandler = new ThriftServiceWithMessageQueue(null);
		thriftServer = new ThriftServer();
		thriftServer.start(thriftServiceHandler,thriftServerProperties);
	}
}
