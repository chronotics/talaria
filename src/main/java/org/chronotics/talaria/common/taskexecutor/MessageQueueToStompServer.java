package org.chronotics.talaria.common.taskexecutor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chronotics.talaria.common.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class MessageQueueToStompServer<T> extends TaskExecutor<T> {
	private static final Logger logger =
			LoggerFactory.getLogger(MessageQueueToStompServer.class);

	public MessageQueueToStompServer(
			PROPAGATION_RULE _propagationRule,
			TaskExecutor<T> _nextExecutor) {
		super(_propagationRule, _nextExecutor);
		// TODO Auto-generated constructor stub
	}

	public static int futureTimeout = 500;
	public static String targetDestination = "targetDestination";
	public static String simpleMessagingTemplate = "simpleMessagingTemplate";
	
	private static JsonFactory factory = new JsonFactory();
    private static ObjectMapper mapper = new ObjectMapper(factory);

	@Override
	public T call() throws Exception {
		String targetDestination = 
				(String)super.properties.get(MessageQueueToStompServer.targetDestination);
		if(targetDestination == null) {
			throw new NullPointerException("targetDestination is not defined");
		}
		
		SimpMessagingTemplate simpMessagingTemplate = 
				(SimpMessagingTemplate)super.properties.get(MessageQueueToStompServer.simpleMessagingTemplate);
		if(simpMessagingTemplate == null) {
			throw new NullPointerException("simpleMessagingTemplate is not defined");
		}
		
		T value = this.getValue();
		if(value == null) {
			return null;
		}
	
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>)value.getClass();
		try {
			if(cls == Boolean.class) {
				simpMessagingTemplate.convertAndSend(targetDestination,(Boolean)value);	
			} else if (cls == Integer.class) {
				simpMessagingTemplate.convertAndSend(targetDestination,(Integer)value);	
			} else if (cls == Short.class) {
				simpMessagingTemplate.convertAndSend(targetDestination,(Short)value);	
			} else if (cls == Long.class) {
				simpMessagingTemplate.convertAndSend(targetDestination,(Long)value);	
			} else if (cls == Double.class) {
				simpMessagingTemplate.convertAndSend(targetDestination,(Double)value);	
			} else if (cls == String.class) {
				simpMessagingTemplate.convertAndSend(targetDestination,(String)value);
//				JsonNode rootnode = mapper.readTree((String)value);
//				JsonNode timenode = rootnode.findValue("timestamp");
//				System.out.println(timenode.asText());
				
			} else {
				throw new ClassCastException("undefine type");
			}
		} catch (MessagingException e) {
			logger.error(e.getMessage());
		}
		return value;
	}

	@Override
	public int getFutureTimeout() {
		return MessageQueueToStompServer.futureTimeout;
	}

}
