package org.chronotics.talaria.thrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ThriftServerMap {
	
	private static class Holder {
		private static final ThriftServerMap theInstance=new ThriftServerMap();
	}
	
	public static ThriftServerMap getInstance() {
		return Holder.theInstance;
	}
	
	private Map<String, ThriftServer> serverMap = 
			new HashMap<String, ThriftServer>();
	
	private ThriftServer defaultServer = null;
	
	public ThriftServerMap() {}
	
	public ThriftServer getDefaultServer(String _key) {
		ThriftServer ret = this.get(_key);
		
		return ret;
	}
	
	public ThriftServer setDefaultServer(
			String _key, 
			ThriftServer _server) {
		if(this.isEmpty()) {
			defaultServer = _server;
			return defaultServer;
		}
		
		ThriftServer ret = this.get(_key);
		if(ret == null || ret != _server) {
			return null;
		}
		
		defaultServer = ret;
		return defaultServer;
	}
	
	public ThriftServer get(String _key) {
		return serverMap.get(_key);
	}
	
	public boolean put(String _key,ThriftServer _server) {
		if(serverMap.isEmpty()) {
			this.setDefaultServer(_key, _server);
		}
		ThriftServer V = serverMap.put(_key, _server);
		return V==null? true : false;
	}
	
	public List<String> getKeys() {
		List<String> rt = new ArrayList<String>();
		rt.addAll(serverMap.keySet());
		return rt;
	}
	
	public boolean containsKey(String _key) {
		return serverMap.containsKey(_key);
	}
	
	public boolean isEmpty() {
		return serverMap.isEmpty();
	}
	
	public int size() {
		return serverMap.size();
	}
	
	public Set<Entry<String, ThriftServer>> entrySet() {
		return serverMap.entrySet();
	}
}
