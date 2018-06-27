package org.chronotics.talaria.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SG Lee
 * @since 3/20/2015
 * @description
 * This Class is singleton class to manage multiple MessageQueues
 * Each MessageQue can be assigned to each sender, which means the key of 
 * MessageQueueMap could be a sort of ID that can distinguish senders.
 */

public class MessageQueueMap {
	private static class Holder {
		private static final MessageQueueMap theInstance=new MessageQueueMap();
	}
	
	public static MessageQueueMap getInstance() {
		return Holder.theInstance;
	}

	private MessageQueueMap() {
	}
	
	private Map<Object,MessageQueue<?>> map = 
			new ConcurrentHashMap<Object,MessageQueue<?>>();
	
	public MessageQueue<?> get(Object _key) {
		return map.get(_key);
	}
	
	public boolean put(Object _key,MessageQueue<?> _value) {
		MessageQueue<?> V = map.put(_key, _value);
		return V==null? true : false;
	}
	
	public List<Object> getKeys() {
		List<Object> rt = new ArrayList<Object>();
		rt.addAll(map.keySet());
		return rt;
	}
	
	public boolean containsKey(Object _key) {
		return map.containsKey(_key);
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public int size() {
		return map.size();
	}
	
	public Set<Entry<Object, MessageQueue<?>>> entrySet() {
		return map.entrySet();
	}
}
