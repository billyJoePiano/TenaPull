package nessusTools.util;

import java.util.*;

public class RecursiveMap<K> implements Map<K, RecursiveMap<K>> {

    private final RecursiveMap<K> origin;
    private final K key;

    private final Map<K, RecursiveMap<K>> map = new LinkedHashMap<>();
    private final Map<K, RecursiveMap<K>> view = Collections.unmodifiableMap(this.map);
    private final Set<K> parents;
    private final Set<K> parentsView;

    private int maxSizeToPrint = 10; // for toString() method ... if the keyset is larger than this size, it just prints the size

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

    public Set<K> getParents() {
        return this.parentsView;
    }

    public K getKey() {
        return this.key;
    }

    public RecursiveMap<K> getOrigin() {
        return this.origin;
    }

    public boolean isOrigin() {
        return this.origin == this;
    }

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

    public boolean containsUpstream(K key) {
        if (this.origin == this) return false;
        return this.containsDownstream(key, this.key);
    }

    public boolean containsUpstream(K startingPoint, K searchingFor) {
        return this.containsDownstream(searchingFor, startingPoint);
    }

    public boolean containsDownstream(K key) {
        Set<K> alreadyChecked = new LinkedHashSet<>();
        if (this != this.origin) {
            alreadyChecked.add(this.key);
        }
        return this.containsDownstream(key, alreadyChecked);
    }

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

    public boolean containsUpOrDownstream(K key) {
        return this.containsDownstream(key) || this.containsUpstream(key);
    }

    public int getMaxSizeToPrint() {
        return this.maxSizeToPrint;
    }

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