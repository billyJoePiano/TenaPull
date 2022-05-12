package nessusTools.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * A map which reverses the roles of key and value, holding a set
 * of "keys" as the value for each "value" key.  Used by InstancesTracker
 * to track the relationship between keys and instances
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class ReverseMap<K, V> implements Map<K, Set<V>> {
    private final Class<? extends Map> mapType;
    private final Class<? extends Set> setType;
    private Set<V> testSet;

    private final Map<K, Set<V>> map;

    /**
     * Instantiates a new Reverse map.
     */
    public ReverseMap() {
        this.mapType = LinkedHashMap.class;
        this.setType = LinkedHashSet.class;
        this.map = new LinkedHashMap<>();
    }

    /**
     * Instantiates a new Reverse map using the provided map
     *
     * @param map the map to copy
     */
    public ReverseMap(Map<V, K> map) {
        this();
        this.putAllReverse(map);
    }

    /**
     * Instantiates a new Reverse map using the specified types for the key sets and backing map,
     * e.g. if you want to specify a TreeMap/TreeSet, or WeakHashMap, etc...
     *
     * @param mapType the map type
     * @param setType the set type
     * @throws NoSuchMethodException     if there are problem instantiating the mapType or setType
     * @throws InvocationTargetException if there are problem instantiating the mapType or setType
     * @throws InstantiationException    if there are problem instantiating the mapType or setType
     * @throws IllegalAccessException    if there are problem instantiating the mapType or setType
     */
    public ReverseMap(Class<? extends Map> mapType, Class<? extends Set> setType)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        this.mapType = mapType;
        this.setType = setType;

        this.map = mapType.getDeclaredConstructor().newInstance();
        this.testSet = setType.getDeclaredConstructor().newInstance();
    }

    /**
     * Instantiates a new Reverse map, copying the provided map, and using the specified types
     * for the key sets and backing map, e.g. if you want to specify a TreeMap/TreeSet,
     * or WeakHashMap, etc...
     *
     * @param mapType the map type
     * @param setType the set type
     * @param map     the map to copy
     * @throws NoSuchMethodException     if there are problem instantiating the mapType or setType
     * @throws InvocationTargetException if there are problem instantiating the mapType or setType
     * @throws InstantiationException    if there are problem instantiating the mapType or setType
     * @throws IllegalAccessException    if there are problem instantiating the mapType or setType
     */
    public ReverseMap(Class<? extends Map> mapType, Class<? extends Set> setType, Map<V, K> map)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        this(mapType, setType);
        this.putAllReverse(map);
    }

    /**
     * Makes a set for the values
     *
     * @return the set
     */
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

    /**
     * Puts a single entry into the value set for the provided key
     *
     * @param key   the key
     * @param value the value
     */
    public void putOne(K key, V value) {
        Set<V> set = this.get(key);
        if (set == null) {
            set = new LinkedHashSet<>();
            this.put(key, set);
        }
        set.add(value);
    }

    /**
     * Reverses the provided map, putting all of its values as keys into this map,
     * and their respective keys as part of the value set
     *
     * @param map the map to copy
     */
    public void putAllReverse(Map<V, K> map) {
        for (Map.Entry<V, K> entry : map.entrySet()) {
            this.putOne(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Like a normal map put
     *
     * @param map the map to copy
     */
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
