package org.chronotics.talaria;

import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.MessageQueueToWebsocketServer;
import org.chronotics.talaria.websocket.springstompserver.SpringStompServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootApplication
public class CommandLineRunnerSpringStompServer implements CommandLineRunner {

	@Autowired
	private ApplicationContext context;
	
	@Override
	public void run(String... arg0) throws Exception {
		
		TalariaProperties properties = 
				(TalariaProperties)context.getBean("talariaProperties");
		
		assert(properties != null);
		if(properties == null) {
			return;
		}
		
		// Websocket server properties
		SpringStompServerProperties stompProperties = 
				properties.getSpringStompServerProperties();
		assert(stompProperties != null);
		if(stompProperties == null) {
			return;
		}
		
		String targetDestination = stompProperties.getTargetDestination();
		
		// start websocket server
		TaskExecutor<SimpMessagingTemplate> executorWebsocketTask = 
				new MessageQueueToWebsocketServer(
						TaskExecutor.PROPAGATION_RULE.SIMULTANEOUSLY, 
						null);
		
		executorWebsocketTask.putProperty(
				MessageQueueToWebsocketServer.targetDestination,
				targetDestination);
		
		// Read MessageQueue and send data to Websocket Server
		ScheduledUpdates<SimpMessagingTemplate> scheduledUpdates = 
				context.getBean(ScheduledUpdates.class);		
		scheduledUpdates.setAttribute(properties.getMqMapKey(),executorWebsocketTask);
	}
}
