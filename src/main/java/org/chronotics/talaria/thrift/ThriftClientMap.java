package org.chronotics.talaria.thrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ThriftClientMap {
	private static class Holder {
		private static final ThriftClientMap theInstance=new ThriftClientMap();
	}
	
	public static ThriftClientMap getInstance() {
		return Holder.theInstance;
	}
	
	private Map<String, ThriftClient> clientMap = 
			new HashMap<String, ThriftClient>();
	
	private ThriftClient defaultClient = null;
	
	public ThriftClientMap() {}
	
	public ThriftClient getDefaultServer(String _key) {
		ThriftClient ret = this.get(_key);
		
		return ret;
	}
	
	public ThriftClient setDefaultServer(
			String _key, 
			ThriftClient _server) {
		if(this.isEmpty()) {
			defaultClient = _server;
			return defaultClient;
		}
		
		ThriftClient ret = this.get(_key);
		if(ret == null || ret != _server) {
			return null;
		}
		
		defaultClient = ret;
		return defaultClient;
	}
	
	public ThriftClient get(String _key) {
		return clientMap.get(_key);
	}
	
	public boolean put(String _key,ThriftClient _server) {
		if(clientMap.isEmpty()) {
			this.setDefaultServer(_key, _server);
		}
		ThriftClient V = clientMap.put(_key, _server);
		return V==null? true : false;
	}
	
	public List<String> getKeys() {
		List<String> rt = new ArrayList<String>();
		rt.addAll(clientMap.keySet());
		return rt;
	}
	
	public boolean containsKey(String _key) {
		return clientMap.containsKey(_key);
	}
	
	public boolean isEmpty() {
		return clientMap.isEmpty();
	}
	
	public int size() {
		return clientMap.size();
	}
	
	public Set<Entry<String, ThriftClient>> entrySet() {
		return clientMap.entrySet();
	}
}
