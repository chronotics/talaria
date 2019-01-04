package org.chronotics.talaria.thrift;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.SimplePrintExecutor;
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

		ThriftServerProperties thriftServerProperties =
				(ThriftServerProperties)context.getBean("thriftServerProperties");
		if(thriftServerProperties == null) {
			System.out.println("check DI injection of ThriftServerProperties");
			return;
		}

		///////////////////////////////////////////////////
		thriftServerProperties.setIp("192.168.0.13");
		System.out.println(thriftServerProperties.getIp());
		System.out.println(thriftServerProperties.getPort());
		///////////////////////////////////////////////////

		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		MessageQueue<String> mq =
				new MessageQueue<>(String.class,
						MessageQueue.default_maxQueueSize,
						MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
		mqMap.put("id", mq);

		// start thrift server
//		TaskExecutor<Object> executorToRead =
//				new SimplePrintExecutor<>(
//                        TaskExecutor.PROPAGATION_RULE.STEP_BY_STEP_ORIGINAL_ARG,
//                        null);
//		TaskExecutor<Object> executorToWrite =
//				new SimplePrintExecutor<>(
//						TaskExecutor.PROPAGATION_RULE.STEP_BY_STEP_ORIGINAL_ARG,
//						null);
//		ThriftServiceExecutor thriftServiceExecutor =
//				new ThriftServiceExecutor(executorToRead, executorToWrite);
//		ThriftService thriftServiceHandler =
//				new ThriftServiceWithMessageQueue(thriftServiceExecutor);
		ThriftService thriftServiceHandler =
				new ThriftServiceWithMessageQueue(null);

		thriftServer = new ThriftServer(thriftServiceHandler, thriftServerProperties);
		thriftServer.start();
	}
}
