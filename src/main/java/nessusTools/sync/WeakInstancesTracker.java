package nessusTools.sync;

import nessusTools.util.*;
import org.apache.logging.log4j.*;

import javax.persistence.criteria.*;
import java.lang.ref.*;
import java.util.*;

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

    public WeakInstancesTracker(Class<K> keyType,
                                Class<I> instanceType,
                                Lambda1<K, I> constructLambda) {
        this(new Type(keyType), new Type(instanceType), constructLambda);
    }

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

    public class WeakRef implements InstancesTracker.KeyFinalizer<WeakRef, I> {
        WeakReference<K> ref;
        K tempStrongRef;

        private WeakRef(K referent) {
            this.ref = new WeakReference<K>(referent);
            this.tempStrongRef = referent;
        }

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

    public I get(K key) {
        return tracker.get(make(key));
    }

    public I getOrConstruct(K key) {
        return tracker.getOrConstruct(make(key));
    }

    public I getOrConstructWith(K key, Lambda1<K, I> lambda) {
        return tracker.getOrConstructWith(make(key), wr -> {
            K ref = wr.get();

            if (ref == null) {
                ref = wr.tempStrongRef;
            }

            return lambda.call(ref);
        });
    }

    public I put(K key, I instance) {
        return tracker.put(make(key), instance);
    }

    public I constructWith(K key, Lambda1<K, I> customLambda) {
        return tracker.constructWith(make(key), wr -> customLambda.call(key));
    }

    public I remove (K key) {
        return tracker.remove(make(key));
    }

    public Set<K> getKeysFor(I instance) {
        return unwrapKeySet(tracker.getKeysFor(instance));
    }

    public Set<K> keySet() {
        return this.unwrapKeySet(tracker.keySet());
    }

    public List<I> get(Lambda1<I, Boolean> filter) {
        return this.tracker.get(filter, 0);
    }

    public List<I> get(Lambda1<I, Boolean> filter, int limit) {
        return this.tracker.get(filter, limit);
    }

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
        } catch (Throwable e) {
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
