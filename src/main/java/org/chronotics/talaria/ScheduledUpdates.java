package org.chronotics.talaria;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author SG Lee
 * @since 3/20/2015
 * @description
 * The registered executor will be called every fixedDelay.
 * This class is to handle Messagequeue that contains messages that 
 * might be sent by another Class.
 */

@Component
public class ScheduledUpdates<T> {
	private static final Logger logger = 
			LoggerFactory.getLogger(ScheduledUpdates.class);

	@Autowired
	private ApplicationContext context;

    @Scheduled(fixedDelayString = "${application.scheduledUpdatesDelay}")
    public void update(){
//		TalariaProperties properties =
//				(TalariaProperties)context.getBean("talariaProperties");
//		assert(properties != null);
//		if(properties == null) {
//			return;
//		}
//		MessageQueueMap mqMap = MessageQueueMap.getInstance();
//		List<String> mqKeyList = properties.getMessageQueueKeyList();
//		mqKeyList.forEach(mqKey -> {
//			MessageQueue<?> mq = mqMap.get(mqKey);
//			if(mq!=null) {
//				logger.info("The size of MQ with key {} is {}", mqKey, mq.size());
//			}
//		});
    }
}
