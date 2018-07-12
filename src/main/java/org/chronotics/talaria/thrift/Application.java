package org.chronotics.talaria.thrift;

import org.chronotics.talaria.common.thriftservice.ThriftServiceWithMessageQueue;
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

		ThriftServerProperties thriftServerProperties = 
				(ThriftServerProperties)context.getBean("thriftServerProperties");
		if(thriftServerProperties == null) {
			System.out.println("check DI injection of ThriftServerProperties");
			return;
		}

		// start thrift server
		ThriftService thriftServiceHandler = new ThriftServiceWithMessageQueue(null);
		thriftServer = new ThriftServer(thriftServiceHandler, thriftServerProperties);
		thriftServer.start();
	}
}
