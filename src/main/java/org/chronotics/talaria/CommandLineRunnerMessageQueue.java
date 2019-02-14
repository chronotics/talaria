package org.chronotics.talaria;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class CommandLineRunnerMessageQueue implements CommandLineRunner {

	private static Logger logger =
			LoggerFactory.getLogger(CommandLineRunnerMessageQueue.class);

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
		
		List<String> mqKeyList = properties.getMessageQueueKeyList();

		mqKeyList.forEach(mqKey -> {
			// register message queue
			if(MessageQueueMap.getInstance().get(mqKey) == null) {
				MessageQueue<String> mq =
						new MessageQueue<String>(
								String.class,
								10,
//							MessageQueue.default_maxQueueSize,
								MessageQueue.OVERFLOW_STRATEGY.DELETE_FIRST);
				MessageQueueMap.getInstance().put(mqKey, mq);
			}
		});
	}
}
