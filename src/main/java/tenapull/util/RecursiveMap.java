package tenapull.util;

import java.util.*;

/**
 * A recursive map is a map of maps that point to each other.  It is essentially a way to
 * map keys as nodes that each point to other keys (via that keys' map).  The origin map
 * is the only map which does not represent a key, and also contains all keys that have
 * been added to any of the recursive maps that originated from it.
 *
 * The primary use-case for this in TenaPull is to determine where thread deadlocks may
 * be happening, by finding circular references between threads blocking each other.  In this
 * scenario, each thread represents a key, and its value is a recursive map of all the threads it is
 * blocking from obtaining a lock (or actually... the recursive maps representing each of those threads,
 * with the threads themselves as the keys, etc...)
 *
 *
 * @param <K> the key type for the recursive map.  The value will always be another recursive map
 *           representing that key (and containing the keys it points to)
 */
public class RecursiveMap<K> implements Map<K, RecursiveMap<K>> {

    private final RecursiveMap<K> origin;
    private final K key;

    private final Map<K, RecursiveMap<K>> map = new LinkedHashMap<>();
    private final Map<K, RecursiveMap<K>> view = Collections.unmodifiableMap(this.map);
    private final Set<K> parents;
    private final Set<K> parentsView;

    private int maxSizeToPrint = 10; // for toString() method ... if the keyset is larger than this size, it just prints the size

    /**
     * Instantiates a new Recursive map.
     */
    public RecursiveMap() {
        this.key = null;
        this.origin = this;
        this.parents = null;
        this.parentsView = null;
    }

    private RecursiveMap(K key, RecursiveMap<K> origin) {
        this.key = key;
        this.origin = origin;
        this.parents = new LinkedHashSet<>();
        this.parentsView = Collections.unmodifiableSet(this.parents);
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
    public RecursiveMap<K> get(Object key) {
        return map.get(key);
    }

    /**
     * Put recursive map.
     *
     * @param key the key
     * @return the recursive map
     */
    public RecursiveMap<K> put(K key) {
        RecursiveMap<K> child = this.origin.map.get(key);
        if (child == null) {
            child = new RecursiveMap<K>(key, this.origin);
            this.origin.map.put(key, child);
        }
        if (this != this.origin) {
            this.map.put(key, child);
            child.parents.add(this.key);
        }
        return child;
    }

    /**
     * Put child recursive map.
     *
     * @param parent the parent
     * @param child  the child
     * @return the recursive map
     */
    public RecursiveMap<K> putChild(K parent, K child) {
        return this.origin.put(parent).put(child);
    }

    @Override
    public RecursiveMap<K> put(K key, RecursiveMap<K> value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    // when called on the origin map, remove this key from every child map
    @Override
    public RecursiveMap<K> remove(Object key) {
        if (this == this.origin) {
            if (this.containsKey(key)) {
                for (RecursiveMap<K> child : this.values()) {
                    child.map.remove(key);
                }

                RecursiveMap<K> removed = this.get(key);
                removed.parents.clear();
                return removed;
            }
            return null;

        } else if (this.containsKey(key)) {
            this.get(key).parents.remove(this.key);
        }
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends RecursiveMap<K>> m) {
        throw new UnsupportedOperationException();
    }

    /**
     * Put all.
     *
     * @param keys the keys
     */
    public void putAll(Set<K> keys) {
        for (K key : keys) {
            this.put(key);
        }
    }

    @Override
    public void clear() {
        if (this == this.origin) {
            for (RecursiveMap<K> child : this.values()) {
                child.map.clear();
            }
        }
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<RecursiveMap<K>> values() {
        return map.values();
    }

    @Override
    public Set<Map.Entry<K, RecursiveMap<K>>> entrySet() {
        return this.view.entrySet();
    }

    /**
     * Gets parents.
     *
     * @return the parents
     */
    public Set<K> getParents() {
        return this.parentsView;
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    public K getKey() {
        return this.key;
    }

    /**
     * Gets origin.
     *
     * @return the origin
     */
    public RecursiveMap<K> getOrigin() {
        return this.origin;
    }

    /**
     * Is origin boolean.
     *
     * @return the boolean
     */
    public boolean isOrigin() {
        return this.origin == this;
    }

    /**
     * Gets circular keys.
     *
     * @return the circular keys
     */
    public Set<K> getCircularKeys() {
        Set<K> circular = new LinkedHashSet<>();
        Set<K> alreadyChecked = new LinkedHashSet<>();
        List<K> currentChain = new LinkedList();
        if (this != this.origin) {
            alreadyChecked.add(this.key);
            currentChain.add(this.key);
        }
        this.circularKeys(circular, alreadyChecked, currentChain);

        //filter circular set for only keys in this map, for child maps
        if (this != this.origin) {
            for (K key : circular) {
                if (!this.map.containsKey(key)) {
                    circular.remove(key);
                }
            }
        }

        return circular;
    }

    private void circularKeys(Set<K> circular,
                              Set<K> alreadyChecked,
                              List<K> currentChain) {

        for (RecursiveMap<K> next : this.map.values()) {
            if (next == this) {
                continue;
            }

            int index = currentChain.indexOf(next.key);
            if (index > -1) {
                for (int i = index; i < currentChain.size(); i++) {
                    // all keys starting at next.key and going to this point are circular
                    K key = currentChain.get(i);
                    circular.add(key);
                }
                continue; // if its in current chain, then it is definitely in 'alreadyChecked'
            }

            if (alreadyChecked.add(next.key)) {
                currentChain.add(next.key);
                next.circularKeys(circular, alreadyChecked, currentChain);
                currentChain.remove(next.key);
            }
        }
    }

    /**
     * Contains upstream boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean containsUpstream(K key) {
        if (this.origin == this) return false;
        return this.containsDownstream(key, this.key);
    }

    /**
     * Contains upstream boolean.
     *
     * @param startingPoint the starting point
     * @param searchingFor  the searching for
     * @return the boolean
     */
    public boolean containsUpstream(K startingPoint, K searchingFor) {
        return this.containsDownstream(searchingFor, startingPoint);
    }

    /**
     * Contains downstream boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean containsDownstream(K key) {
        Set<K> alreadyChecked = new LinkedHashSet<>();
        if (this != this.origin) {
            alreadyChecked.add(this.key);
        }
        return this.containsDownstream(key, alreadyChecked);
    }

    /**
     * Contains downstream boolean.
     *
     * @param startingPoint the starting point
     * @param searchingFor  the searching for
     * @return the boolean
     */
    public boolean containsDownstream(K startingPoint, K searchingFor) {
        RecursiveMap<K> upstreamMap = this.origin.get(startingPoint);
        if (upstreamMap == null) {
            return false;
        }
        if (!this.origin.containsKey(searchingFor)) {
            return false;
        }

        return upstreamMap.containsDownstream(searchingFor);
    }

    private boolean containsDownstream(K key, Set<K> alreadyChecked) {
        for (RecursiveMap<K> next : this.map.values()) {
            if (next == this) {
                continue;
            }

            if (Objects.equals(key, next.key) || next.containsKey(key)) {
                return true;
            }

            if (alreadyChecked.add(next.key)) {
                if (next.containsDownstream(key, alreadyChecked)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Contains up or downstream boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean containsUpOrDownstream(K key) {
        return this.containsDownstream(key) || this.containsUpstream(key);
    }

    /**
     * Gets max size to print.
     *
     * @return the max size to print
     */
    public int getMaxSizeToPrint() {
        return this.maxSizeToPrint;
    }

    /**
     * Sets max size to print.
     *
     * @param maxSizeToPrint the max size to print
     */
    public void setMaxSizeToPrint(int maxSizeToPrint) {
        this.maxSizeToPrint = maxSizeToPrint;
    }

    public String toString() {
        Set<K> keySet = this.map.keySet();
        int size = keySet.size();

        String keySetStr;

        if (size > this.maxSizeToPrint) {
            keySetStr = size + ": { <not listed>  }";
        } else {
            keySetStr = size + ": " + keySet.toString();
        }

        if (this.origin == this) {
            return "RecursiveMap (origin) " + keySetStr;

        } else {
            return "RecursiveMap[" + this.key + "] " + keySetStr;
        }
    }
}