package tenapull.sync;

import tenapull.util.*;
import org.apache.logging.log4j.*;

import java.lang.ref.*;
import java.util.*;

/**
 * A thin wrapper for InstancesTracker, which wraps the key with weak references
 * to the key.
 *
 * @param <K> the key type
 * @param <I> the instances type
 */
public class WeakInstancesTracker<K, I> {
    private static final Logger logger = LogManager.getLogger(WeakInstancesTracker.class);

    private static final ReadWriteLock<Map<WeakInstancesTracker, Void>,
                                        Map<WeakInstancesTracker, Void>>
            trackers = ReadWriteLock.forMap(new WeakHashMap<>());

    private static final Object gcMonitor = new Object();


    private final Class<K> keyType;
    private final InstancesTracker<WeakInstancesTracker<K, I>.WeakRef, I> tracker;
    //private final boolean keysComparable;
    private final ReadWriteLock<Map<WeakRef, Void>, Map<WeakRef, Void>>
            toDiscard = ReadWriteLock.forMap(new WeakHashMap<>());

    /**
     * Instantiates a new Weak instances tracker.
     *
     * @param keyType         the key type
     * @param instanceType    the instance type
     * @param constructLambda the construct lambda
     */
    public WeakInstancesTracker(Class<K> keyType,
                                Class<I> instanceType,
                                Lambda1<K, I> constructLambda) {
        this(new Type(keyType), new Type(instanceType), constructLambda);
    }

    /**
     * Instantiates a new Weak instances tracker.
     *
     * @param keyType         the key type
     * @param instanceType    the instance type
     * @param constructLambda the construct lambda
     */
    public WeakInstancesTracker(Type<K> keyType,
                                Type<I> instanceType,
                                Lambda1<K, I> constructLambda) {

        this.keyType = keyType.getType();
        //this.keysComparable = Comparable.class.isAssignableFrom(keyType);

        Type<WeakRef> weakKeyType = new Type(WeakRef.class, keyType);

        Lambda1<WeakRef, I> construct = wr -> {
            K key = wr.get();
            if (key != null) {
                return constructLambda.call(key);
            } else {
                throw new IllegalStateException(
                        "WeakRef was garbage collected while instance was under construction.  "
                    + "The caller must maintain a strong reference to the key while the instance is being constructed");
            }
        };

        this.tracker = new InstancesTracker(weakKeyType, instanceType, construct);
        trackers.write(Void.class, trackers -> trackers.put(this, null));
    }

    /**
     * A wrapper for WeakReference that also implement the Instances.KeyFinalizer interface,
     * so that a key is held as a strong reference until it has been finalized
     */
    public class WeakRef implements InstancesTracker.KeyFinalizer<WeakRef, I> {
        /**
         * The weak reference
         */
        WeakReference<K> ref;
        /**
         * The temporary strong reference that is set to null after the key is finalized
         */
        K tempStrongRef;

        private WeakRef(K referent) {
            this.ref = new WeakReference<K>(referent);
            this.tempStrongRef = referent;
        }

        /**
         * Get the key which this references, or null if it has been GC'd
         *
         * @return the key
         */
        public K get() {
            return this.ref.get();
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (!this.getClass().equals(o.getClass())) {
                return false;
            }

            WeakRef other = (WeakRef) o;
            K mine = this.get();
            K theirs = other.get();

            if (mine == null || theirs == null) {
                return false;
            }
            return Objects.equals(mine, theirs);

        }

        @Override
        public void finalizeKey(I instance) {
            this.tempStrongRef = null;
        }
    }

    private WeakRef make(K key) {
        /*if (keysComparable) {
            return new WeakRefComparable(key);

        } else {*/
            return new WeakRef(key);
        //}
    }

    /**
     * Returns the instance associated with the provided key
     *
     * @param key the key
     * @return the instance
     */
    public I get(K key) {
        return tracker.get(make(key));
    }

    /**
     * Wraps InstancesTracker.getOrConstruct
     *
     * @param key the key
     * @return the instance
     */
    public I getOrConstruct(K key) {
        return tracker.getOrConstruct(make(key));
    }

    /**
     * Wraps InstancesTracker.getOrConstructWith
     *
     * @param key    the key
     * @param lambda the construct lambda
     * @return the instance
     */
    public I getOrConstructWith(K key, Lambda1<K, I> lambda) {
        return tracker.getOrConstructWith(make(key), wr -> {
            K ref = wr.get();

            if (ref == null) {
                ref = wr.tempStrongRef;
            }

            return lambda.call(ref);
        });
    }

    /**
     * Wraps InstancesTracker.put
     *
     * @param key      the key
     * @param instance the instance to put
     * @return any displaced instance
     */
    public I put(K key, I instance) {
        return tracker.put(make(key), instance);
    }

    /**
     * Wraps InstancesTracker.constructWith
     *
     * @param key          the key
     * @param customLambda the custom construct lambda
     * @return the instance associated with the key at the time this returns
     */
    public I constructWith(K key, Lambda1<K, I> customLambda) {
        return tracker.constructWith(make(key), wr -> customLambda.call(key));
    }

    /**
     * Wraps InstancesTracker.remove
     *
     * @param key the key
     * @return the instance that was previously associated with the key
     */
    public I remove (K key) {
        return tracker.remove(make(key));
    }

    /**
     * Wraps InstancesTracker.getKeysFor
     *
     * @param instance the instance
     * @return the keys that previously pointed to the provided instance
     */
    public Set<K> getKeysFor(I instance) {
        return unwrapKeySet(tracker.getKeysFor(instance));
    }

    /**
     * A view of the keyset for this WeakInstancesTracker.  Wraps InstancesTracker.keySet
     *
     * @return the set
     */
    public Set<K> keySet() {
        return this.unwrapKeySet(tracker.keySet());
    }

    /**
     * Wraps InstancesTracker.get
     *
     * @param filter the filter
     * @return the list
     */
    public List<I> get(Lambda1<I, Boolean> filter) {
        return this.tracker.get(filter, 0);
    }

    /**
     * Wraps InstancesTracker.get
     *
     * @param filter the filter
     * @param limit  the limit
     * @return the list
     */
    public List<I> get(Lambda1<I, Boolean> filter, int limit) {
        return this.tracker.get(filter, limit);
    }

    /**
     * Wraps InstancesTracker.getInstances
     *
     * @return the instances
     */
    public Set<I> getInstances() {
        return this.tracker.getInstances();
    }

    private Set<K> unwrapKeySet(Set wrKeys) {
        Set<K> keySet;
        /*if (this.keysComparable) {
            keySet = new TreeSet<>();

        } else {*/
        keySet = new LinkedHashSet<>();
        //}
        List<WeakRef> discard = new LinkedList<>();

        for (Object wro : wrKeys) {
            WeakRef wr = (WeakRef) wro;
            K key = wr.get();
            if (key != null) {
                keySet.add(key);
            } else {
                discard.add(wr);
            }
        }

        if (discard.size() > 0) {
            synchronized (gcMonitor) {
                this.toDiscard.write(toDiscard -> {
                    for (WeakRef wr : discard) {
                        toDiscard.put(wr, null);
                    }
                    return null;
                });
                garbageCollector.interrupt();
            }
        }

        return keySet;
    }

    private static Thread garbageCollector = new Thread(() -> {
        Thread garbageCollector = Thread.currentThread();

        long lastGcMonitorValue = 0;

        while(true) try {
            synchronized (gcMonitor) {
                garbageCollector.setPriority(Thread.MIN_PRIORITY);
                try {
                    gcMonitor.wait();
                } catch (InterruptedException e) { }
            }

            Thread.sleep(10000);

            Map<WeakInstancesTracker, Void>
                    trackers = WeakInstancesTracker.trackers.read(WeakHashMap::new);

            synchronized (gcMonitor) {
                garbageCollector.setPriority(Thread.MAX_PRIORITY);

                for (WeakInstancesTracker tracker : trackers.keySet()) {
                    tracker.discard();
                }
                Thread.interrupted(); //clear interrupt status
            }
        } catch (Exception e) {
            logger.error(e);
        }
    });

    static {
        garbageCollector.setName("WeakInstancesTracker.garbageCollector");
        garbageCollector.setDaemon(true);
        garbageCollector.start();
    }

    private void discard() {
        this.toDiscard.write(discard -> {
            for (WeakRef wr : discard.keySet()) {
                this.tracker.remove(wr);
            }
            discard.clear();
            return null;
        });
    }
}
