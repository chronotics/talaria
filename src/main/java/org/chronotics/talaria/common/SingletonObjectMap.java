package org.chronotics.talaria.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonObjectMap {

    private static class Holder {
        private static final SingletonObjectMap theInstance =
                new SingletonObjectMap();
    }

    public static SingletonObjectMap getInstance() {
        return Holder.theInstance;
    }

    private Map<String, Object> map =
            new ConcurrentHashMap<>();

    public Object get(Object _key) {
        return map.get(_key);
    }

    public boolean put(String _key,Object _value) {
        Object V = map.put(_key, _value);
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

    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public Object remove(Object _key) {
        Object o = get(_key);
        if(o == null) {
            return null;
        }
        return map.remove(_key);
    }

    public void clear() {
        map.clear();
    }
}
