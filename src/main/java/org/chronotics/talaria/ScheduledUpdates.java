package org.chronotics.talaria;

import java.util.concurrent.Future;

import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.common.taskexecutor.MessageQueueToWebsocketServer;
import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private SimpMessagingTemplate simpMessagingTemplate;

	public String queueMapKey = null;

	private TaskExecutor<T> executor = null;
	
	public void setAttribute(
			String _queueMapKey, 
			TaskExecutor<T> _executor) {
		queueMapKey = _queueMapKey;
		executor = _executor;
		executor.setProperty(_executor);
	}
	
    @Scheduled(fixedDelayString = "${application.scheduledUpdatesDelay}")
    public void update(){
    	if(executor == null) {
    		logger.error("Executor is not defined. This can be occurred few times when the process is initialized");
    		return;
    	}
	
    	assert(queueMapKey != null);
    	if(queueMapKey == null) {
    		throw new NullPointerException("queueMapKey is null");
    	}
    	
		MessageQueue<T> msgqueue = (MessageQueue<T>)
				MessageQueueMap.getInstance().
				get(queueMapKey);
		assert(msgqueue != null);
		if(msgqueue == null) {
			throw new NullPointerException();
		}
		
		if(msgqueue.isEmpty()) {
			return;
		}  	
		int count = msgqueue.size();
		for (int i = 0; i < count; i++) {	
			try {
				T v = msgqueue.peek();
				if(v != null) {
					Future<T> future = executor.execute(v);//(SimpMessagingTemplate)template);
					T rt = future.get();
					if(rt == null) {
						//System.out.println("future is null");
						logger.error("TaskExecutor execution error, future return is null");
					} else {
						msgqueue.poll();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
}
