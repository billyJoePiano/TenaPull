package nessusTools.util;

import java.lang.reflect.*;
import java.util.*;

public class ReverseMap<K, V> implements Map<K, Set<V>> {
    private final Class<? extends Map> mapType;
    private final Class<? extends Set> setType;
    private Set<V> testSet;

    private final Map<K, Set<V>> map;

    public ReverseMap() {
        this.mapType = LinkedHashMap.class;
        this.setType = LinkedHashSet.class;
        this.map = new LinkedHashMap<>();
    }

    public ReverseMap(Map<V, K> map) {
        this();
        this.putAllReverse(map);
    }

    public ReverseMap(Class<? extends Map> mapType, Class<? extends Set> setType)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        this.mapType = mapType;
        this.setType = setType;

        this.map = mapType.getDeclaredConstructor().newInstance();
        this.testSet = setType.getDeclaredConstructor().newInstance();
    }

    public ReverseMap(Class<? extends Map> mapType, Class<? extends Set> setType, Map<V, K> map)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        this(mapType, setType);
        this.putAllReverse(map);
    }

    protected Set<V> makeValueSet() {
        if (testSet != null) {
            Set<V> set = testSet;
            testSet = null;
            return set;
        }

        try {
            return this.setType.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void putOne(K key, V value) {
        Set<V> set = this.get(key);
        if (set == null) {
            set = new LinkedHashSet<>();
            this.put(key, set);
        }
        set.add(value);
    }

    public void putAllReverse(Map<V, K> map) {
        for (Map.Entry<V, K> entry : map.entrySet()) {
            this.putOne(entry.getValue(), entry.getKey());
        }
    }

    public void putAllForward(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            this.putOne(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<V> get(Object key) {
        return map.get(key);
    }
    
    @Override
    public Set<V> put(K key, Set<V> value) {
        return map.put(key, value);
    }

    @Override
    public Set<V> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Set<V>> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Set<V>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        return map.entrySet();
    }
}
