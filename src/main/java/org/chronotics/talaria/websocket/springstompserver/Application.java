package org.chronotics.talaria.websocket.springstompserver;

import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.DummyMessageGenerator;
import org.chronotics.talaria.common.taskexecutor.MessageQueueToWebsocketServer;
import org.chronotics.talaria.ScheduledUpdates;
import org.chronotics.talaria.TalariaProperties;
import org.chronotics.talaria.common.MessageQueue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {
		"org.chronotics.talaria.common",
		"org.chronotics.talaria.websocket.springstompserver"})
public class Application {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);
	    
		// getProperties
		TalariaProperties properties = 
				(TalariaProperties)context.getBean("talariaProperties");		
		String mqMapKey = properties.getMqMapKey();
		
		// register message queue
		MessageQueue<String> msgqueue = 
				new MessageQueue<String>(
						String.class,
						MessageQueue.default_maxQueueSize,
						MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
		MessageQueueMap.getInstance().put(mqMapKey, msgqueue);
		
		SpringStompServerProperties stompProperties = properties.getSpringStompServerProperties();
		String targetDestination = stompProperties.getTargetDestination();
		
		ScheduledUpdates scheduledUpdates = context.getBean(ScheduledUpdates.class);
		
		TaskExecutor<SimpMessagingTemplate> executorWebsocketTask = 
				new MessageQueueToWebsocketServer(
						TaskExecutor.PROPAGATION_RULE.SIMULTANEOUSLY, null);
		
		executorWebsocketTask.putProperty(
				MessageQueueToWebsocketServer.targetDestination,
				targetDestination);
		
		scheduledUpdates.setAttribute(mqMapKey, executorWebsocketTask);
		
		Thread thread = new Thread(new DummyMessageGenerator());
		thread.start();
	}
}
