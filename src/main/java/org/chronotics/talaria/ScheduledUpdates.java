package org.chronotics.talaria;

import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

//	@Autowired
//    private SimpMessagingTemplate simpMessagingTemplate;

	public String mqKey = null;

	private TaskExecutor<T> executor = null;
	
//	public void setAttribute(
//			TaskExecutor<T> _executor) {
//		executor = _executor;
//		executor.setProperty(_executor);
//	}
	
    @Scheduled(fixedDelayString = "${application.scheduledUpdatesDelay}")
    public void update(){
		TalariaProperties properties =
				(TalariaProperties)context.getBean("talariaProperties");
		assert(properties != null);
		if(properties == null) {
			return;
		}

		mqKey = properties.getMessageQueueKey();

		MessageQueueMap mqMap = MessageQueueMap.getInstance();
		MessageQueue<String> mq = (MessageQueue<String>)mqMap.get(mqKey);
		if(mq == null) {
			return;
		}

		// add value
		mq.addLast(String.valueOf(System.currentTimeMillis()));
		//
		logger.info("The current time is inserted");

    	if(executor == null) {
    		logger.error("Executor is not defined. This can be occurred few times when the process is initialized");
    		return;
    	}

//    	assert(mqKey != null);
//    	if(mqKey == null) {
//    		throw new NullPointerException("queueMapKey is null");
//    	}
//
//		MessageQueue<T> mq = (MessageQueue<T>)
//				MessageQueueMap.getInstance().
//				get(mqKey);
//		assert(mq != null);
//		if(mq == null) {
//			throw new NullPointerException();
//		}
//
//		if(mq.isEmpty()) {
//			return;
//		}
//		int count = mq.size();
//		for (int i = 0; i < count; i++) {
//			try {
//				T v = mq.getFirst();
//				if(v != null) {
//					Future<T> future = executor.execute(v);
//					T rt = future.get();
//					if(rt == null) {
//						logger.error("TaskExecutor execution error, future return is null");
//					} else {
//						mq.removeFirst();
//					}
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
    }
}
