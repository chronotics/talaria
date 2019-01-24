package org.chronotics.talaria.thrift;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.BypassExecutor;
import org.chronotics.talaria.common.taskexecutor.NullReturnExecutor;
import org.chronotics.talaria.common.thriftservice.ThriftServiceWithMessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"org.chronotics.talaria.thrift"})
public class Application {

	private static final Logger logger =
			LoggerFactory.getLogger(Application.class);

	private static ThriftServer thriftServer = null;

	public static void main(String[] args) {
		// run spring boot
		ApplicationContext context = SpringApplication.run(Application.class,args);

		ThriftServerProperties thriftServerProperties =
				(ThriftServerProperties)context.getBean("thriftServerProperties");
		if(thriftServerProperties == null) {
			logger.error("check DI injection of ThriftServerProperties");
			return;
		}

		// start thrift server
		TaskExecutor<Object> executorToRead =
				new BypassExecutor<>();
		TaskExecutor<Object> executorToWrite =
				new NullReturnExecutor<>();
		ThriftServiceExecutor thriftServiceExecutor =
				new ThriftServiceExecutor(executorToRead, executorToWrite);
		ThriftServiceHandler thriftServiceHandler =
				new ThriftServiceWithMessageQueue(thriftServiceExecutor);

		thriftServer = new ThriftServer(thriftServiceHandler, thriftServerProperties);
		thriftServer.start();
	}
}
