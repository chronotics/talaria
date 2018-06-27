package org.chronotics.talaria.thrift;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.apache.thrift.transport.TTransport;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class FactoryTTransport {
	private static class Holder {
		private static final FactoryTTransport theInstance=new FactoryTTransport();
	}
	
	public static FactoryTTransport getInstance() {
		return Holder.theInstance;
	}
	
	private Map<String,TTransport> map=new ConcurrentHashMap<String,TTransport>();
	
	public TTransport put(String _key,TTransport _value) {
		return map.put(_key,_value);
	}
	
	public TTransport get(String _key) {
		return map.get(_key);
	}
	
//	Parameters:
//	key - key whose mapping is to be removed from the map
//	Returns:
//	the previous value associated with key, or null if there was no mapping for key
	public TTransport remove(String _key) {
		TTransport transport=map.get(_key);
		if(transport==null) {
			return null;
		} else {
			transport.close();
			return map.remove(_key);
		}
	}
	
	@PreDestroy
	protected void close() {
		for(Map.Entry<String,TTransport> entry : map.entrySet()) {
			entry.getValue().close();
		}
		map.clear();
		
		System.out.println("finalize() is called");
	}
}
