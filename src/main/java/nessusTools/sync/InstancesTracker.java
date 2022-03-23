package nessusTools.sync;

import org.apache.logging.log4j.*;

import java.util.*;

/* Synchronizes and maps unique instances to a unique "key" (typically a String).
 * Instances are held as weak references via a WeakHashMap which is synchronized via ReadWriteLock.
 *
 *
 * NOTE: The implementation of this class inverts keys and value.
 * It is a little counter-intuitive that the instance is the key of the actual WeakHashMap and the key is
 * the value.  But this is needed for the "Weak" reference to work correctly, and allow garbage collection
 * of unused instances.
 *
 * In a sense, both the keys and values "keys" for each other, but only the instance is a weak reference
 * that can be garbage-collected, which is why it must occupy the formal 'key' position in the WeakHashMap.
 * However, for users of InstancesTracker, the instances are considered to be the "values", and the accessor
 * strong references (typically strings) are considered to be the "keys".  The strongly-referenced "keys" may have
 * more than one key per instance, but each key may refer to only one instance, like a normal map.
 *
 * The "put" method is ONLY for adding additional keys to a pre-existing instance because an instance should
 * not be constructed without first obtaining a CreateLock for the initial key. There are several methods for this.
 * 1) The getOrConstruct method.  A default construct lambda should be provided with the InstancesTracker
 *      constructor.  The construct lambda will be called with appropriate locks by the getOrConstruct method.
 *      Note that the construct lambda MUST return a non-null value of instance type I or a
 *      NullReturnFromConstructLambda runtime exception will be thrown (no need to be declared or caught)
 *
 * 2) The constructWith method allows you to pass a key and a custom constructor lambda which will be called
 *      with the appropriate locks.  Like with getOrConstruct, it must return a non-null value of type I
 *
 * 3) The write method allows you to pass a write lambda which will be called with a full write lock on the
 *      entire instances map.
 *
 * Type parameters:
 *
 * K -- Key -- the string or other object used to access the instances.  There is a strong reference to these values
 * I -- Instances -- the type of the instances being tracked.  These are weak references so may be garbage
 * collected if no strong references remain to the instance, in which case the strong reference to the key
 * will also be destroyed here (though there may be strong references elsewhere that prevent it from being
 * garbage-collected)
 */
public class InstancesTracker<K, I> {
    private static final Logger logger = LogManager.getLogger(InstancesTracker.class);

    private final Class<K> keyClass;
    private final Class<I> instClass;
    private final Lambda<K, I> construct;

    private final ReadWriteLock<Map<I, Set<K>>, I> instances
            = ReadWriteLock.forMap(new WeakHashMap());

    private final ReadWriteLock<Map<K, CreateLock>, CreateLock> underConstruction
            = ReadWriteLock.forMap(new LinkedHashMap());

    //lazily constructed unmodifiable views of the keys for each instance
    private final ReadWriteLock<Map<I, Set<K>>, Set<K>> keySetViews
            = ReadWriteLock.forMap(new WeakHashMap());

    // eagerly contructed (using Set.union(set1, set2)) unmodifiable view of the entire set of keys
    private final ReadWriteLock<Set<K>, Set<K>> keySet;
    private final Set<K> keySetImmutable;

    private final boolean keysComparable;


    public InstancesTracker(Class<K> keyType,
                            Class<I> instancesClass,
                            Lambda<K, I> constructionLambda) {
        this.keyClass = keyType;
        this.instClass = instancesClass;

        this.construct = constructionLambda;

        this.keysComparable = Comparable.class.isAssignableFrom(keyType);

        if (this.keysComparable) {
            this.keySet = ReadWriteLock.forSet(new TreeSet());
        } else {
            this.keySet = ReadWriteLock.forSet(new LinkedHashSet());
        }

        this.keySetImmutable = keySet.read(keySet -> keySet);

    }

    public Set<K> keySet() {
        return this.keySetImmutable;
    }

    public I get(K key) {
        return this.instances.read(instances -> {
            for (Map.Entry<I, Set<K>> entry : instances.entrySet()) {
                if (entry.getValue().contains(key)) {
                    return entry.getKey();
                }
            }

            CreateLock lock = underConstruction.read(underConstruction ->
                    underConstruction.get(key));

            if (lock == null) {
                return null;
            }
            synchronized (lock) {
                return lock.finishedInstance;
            }
        });
    }

    public I getOrConstruct(K key)
            throws InstancesTrackerException {
        return getOrConstruct(key, this.construct, false);
    }

    public I remove(K key) {
        return this.instances.write(instances -> {
            Set<K> keySet;
            for (Map.Entry<I, Set<K>> entry : instances.entrySet()) {
                keySet = entry.getValue();
                if (keySet.contains(key)) {
                    keySet.remove(key);
                    return entry.getKey();
                }
            }
            return null;
        });
    }


    public Set<K> getKeysFor(I instance)
            throws UnrecognizedInstance {

        return instances.read(Set.class,
                instances -> keySetViews.write(views -> {

            Set<K> view = views.get(instance);
            if (view != null) {
                return view;
            }

            Set<K> orig = instances.get(instance);
            if (orig == null) {
                throw new UnrecognizedInstance(this, instance);
            }

            view = Collections.unmodifiableSet(orig);
            views.put(instance, view);
            return view;
        }));
    }


    // First removes the key if it already exists, and then gains a construct
    // lock with it before running the custom one-time construction lambda
    public I constructWith(K key, Lambda<K, I> customLambda)
            throws InstancesTrackerException, IllegalArgumentException {
        if (customLambda == null) {
            throw new IllegalArgumentException(
                    "instancesTracker.constructWith(key, callableWithArg) cannot have a null callableWithArg");
        }

        return getOrConstruct(key, customLambda, true);
    }

    public I read(K key, Lambda<I, I> readLambda) {
        return this.instances.read(instances -> {
            return readLambda.call(this.get(key));
        });
    }

    public I put(K key, I instance)
            throws UnrecognizedInstance {

        return instances.write(instances -> {
            if (!instances.containsKey(instance)) {
                throw new UnrecognizedInstance(this, key, instance);
            }

            return underConstruction.write(instClass,
                    underConstruction -> putWithLock(key, instance, instances));
        });
    }


    /**
     * Private method only intended to be called with the following write locks
     * 1) the instances map, which is then passed to this method.
     * 2) the underConstruction map
     * 3) the CreateLock made for this key
     *
     * Each caller to this method should obtain those locks and any other operations on the
     * underConstruction map needed
     *
     * @param key
     * @param instance
     * @param instances
     * @return displaced instance
     */
    private I putWithLock(K key, I instance, Map<I, Set<K>> instances) {
        if (key == null || instance == null) {
            return null;
        }

        boolean foundInstance = false;
        I displacedInstance = null;

        for (Map.Entry<I, Set<K>> entry : instances.entrySet()) {
            I otherInstance = entry.getKey();
            if (instance == otherInstance) {
                foundInstance = true;
                entry.getValue().add(key);
                continue;
                // keep iterating to look for another matching 'key'
            }

            Set<K> otherKeys = entry.getValue();
            if (otherKeys.contains(key)) {
                otherKeys.remove(key);
                displacedInstance = otherInstance;
            }
        }

        if (!foundInstance) {
            Set<K> keySet;
            if (this.keysComparable) {
                keySet = new TreeSet<>();

            } else {
                keySet = new LinkedHashSet<>();
            }

            keySet.add(key);
            instances.put(instance, keySet);

            if (this.keySet.write(Boolean.class, ks -> ks.add(key))) {
                if (displacedInstance != null) {
                    logger.warn(
                            "Unexpected lack of key in master keySet when a displaced instance was found: "
                            + key.toString());
                }
            } /* else {
                // master keyset may have outdated entries due to garbage collection of instances!!

                if (displacedInstance == null) {
                    logger.warn(
                            "Unexpected presence of key in master keySet when no displaced instance was found: "
                            + key.toString());
                }
            } */
        }

        return displacedInstance;
    }

    /**
     * Obtains a write lock on the instances map, which allows the passed lambda to call the "put" lambda
     * passed to it.
     *
     * Note that this method will block all read access to the instance map AND underConstruction CreateLock map
     * while it is executing, so is generally a less-preferred approach than getOrConstruct or constructWith.
     * However, it may be necessary in certain circumstances depending on external synchronization needs.
     *
     * Also note that every call to "put" lambda will first check for a construct lock on the given key,
     * and if found will wait for that lock to release before overriding the instance it created.  The
     * overridden instance will be returned by the put lambda, and the other thread will return this
     * thread's instance once it is able to gain a lock on underConstruction map again.
     *
     * @param writeLambda Callback lambda's argument is another lambda to 'put' a key-value pair passed
     * via a Map.Entry. Return value of put is another instance displaced by this put, or null if no
     * matching key was found. If either key or value are null, nothing will happen and null will be returned.
     * The passed writeLambda may return an instance &gt;I&lt; or null.  Typically, this would be the new
     * instance added or the one displaced.
     *
     * NOTE: Put lambda will throw an IllegalAccessError if it is called outside the scope of the write lock.
     *
     * @return whatever value &gt;I&lt; (including null) returned by writeLambda
     */
    public I write(Lambda<Lambda2<K, I, I>, I> writeLambda)
            throws NullPointerException {

        Boolean[] unlocked = new Boolean[]{ Boolean.FALSE };

        Map<I, Set<K>>[] instances = new Map[] { null };
        Map<K, CreateLock>[] underConstruction = new Map[] { null };

        Lambda2<K, I, I>
                put = (key, instance) -> {

            // put lambda, passed to the writeLambda

            if (unlocked[0]) {
                throw new IllegalAccessError(
                        "Cannot call instanceTracker's put(mapEntry<K, I>) lambda after the write lock has been released");
            }

            if (instance == null || key == null) {
                return null;
            }

            CreateLock lock = underConstruction[0].get(key);
            boolean newLock = lock == null;
            if (newLock) {
                lock = new CreateLock(key);
                // no need to put into underConstruction, since we hold its lock the entire time
            }

            I displacedInstance;

            synchronized (lock) {
                 displacedInstance = putWithLock(key, instance, instances[0]);

                if (!newLock) {
                    underConstruction[0].remove(key);
                }

                if (lock.finishedInstance != null) {
                    if (displacedInstance != null) {
                        String same = displacedInstance == lock.finishedInstance
                                ? " THE SAME " : " NOT THE SAME ";

                        logger.warn(
                                "Unexpected displaced instance found in instances map during"
                                        + "instancesTracker.write(writeLambda(put) -> put(mapEntry) ...) for key '"
                                        + key.toString()
                                        + "' while a CreateLock for this key already held a finishedInstance.  "
                                        + "The displaced instances are " + same + " instance.");
                    }
                    displacedInstance = lock.finishedInstance;
                }

                // always return the lock's finished instance, since it would be replacing
                // the other instance anyways
                lock.finishedInstance = instance;
            }

            return displacedInstance;
        };


        try {
            // unlike getOrConstruct (below, not above...) this method holds the
            // lock on underConstruction the entire time, in order to override the
            // lock.finishedInstance in other threads' removeFromUnderConstruction calls

            return this.instances.write(insts -> this.underConstruction.write(instClass,
                ucMap -> {
                    instances[0] = insts;
                    underConstruction[0] = ucMap;
                    return writeLambda.call(put);
                }));


        } finally {
            unlocked[0] = Boolean.TRUE;
            instances[0] = null;
            underConstruction[0] = null;
        }
    }

    private I getOrConstruct(K key,
                             Lambda<K, I> constructLambda,
                             boolean overwriteIfFound)
            throws InstancesTrackerException {

        if (key == null) {
            throw new NullArgumentAsKey(this);
        }
        Boolean[] newlyConstructed = new Boolean[] { Boolean.FALSE };

        // Executor may be called with read lock or write lock, depending
        // on the value of overwriteIfFound
        Object[] lk = new Object[] { null };

        Lambda<Map<I, Set<K>>, I> executor = (instances) -> {

            for (Map.Entry<I, Set<K>> entry : instances.entrySet()) {
                if (entry.getValue().contains(key)) {
                    I instance = entry.getKey();
                    if (instance == null) {
                        break;
                        //since this is a WeakHashMap, is it possible the key
                        // was garbage collected while iterating?!?!?

                    } else if (overwriteIfFound) {
                        // will only be invoked on write lock
                        instances.remove(instance);
                        break;

                    } else {
                        return instance;
                    }

                }
            }

            CreateLock lock;

            synchronized (lock = underConstruction.write(underConstruction -> {
                CreateLock l = underConstruction.get(key);
                if (l == null) {
                    l = new CreateLock(key);
                    underConstruction.put(key, l);
                }

                return l;

            })) {
                // this block is synchronized on the CreateLock fetched/constructed above
                lk[0] = lock;
                if (lock.finishedInstance == null && !lock.failedConstruction) {
                    newlyConstructed[0] = Boolean.TRUE;
                    try {
                        lock.finishedInstance = constructLambda.call(key);

                    } finally {
                        lock.failedConstruction = lock.finishedInstance == null;
                    }
                }
                return lock.finishedInstance;
            }
        };

        I instance = null;

        try {
            if (overwriteIfFound) {
                instance = instances.write(executor);

            } else {
                instance = instances.read(executor);
            }

        } finally {
            if (newlyConstructed[0]) {
                removeFromUnderConstruction(key, instance, (CreateLock) lk[0]);
            }
        }

        if (instance == null) {
            throw new NullReturnFromConstructLambda(this, key);
        }

        CreateLock lock = (CreateLock) lk[0];
        if (lock != null) {
            synchronized (lock) {
                if (lock.finishedInstance != null) {
                    instance = lock.finishedInstance;
                    // in case this was overriden by instancesTracker.write(writeLambda(put) -> put(mapEntry))

                } else if (lock.failedConstruction) {
                    // ???
                    lock.finishedInstance = instance;
                    logger.warn("Unexpected success in constructing instance that failed previously");

                } else {
                    logger.warn("Unexpected null found in createLock.finishedInstance that didn't fail");
                }
            }
        }

        return instance;
    }

    /**
     * Synchronizes the moving of a newly constructed instance from the "underConstruction" map to the
     * instances map.  In all cases, a write lock is needed on the "underConstruction" list. However,
     * If the instance is null, no lock is needed on the instances map.  Otherwise it will
     * require a write lock on the instances map, which may be blocked if a write(writeLambda) method is
     * under way in another thread.  In this case, if the writeLambda places a different instance on
     * the same key, that instance will be returned instead
     *
     */
    private void removeFromUnderConstruction(K key, I instance, CreateLock lock) {
        Map<I, Set<K>>[] instances = new Map[] { null }; // instances map will be put here...
        //only used if instance != null , but needs to be declared in this scope so it is accessible to
        // removeFromUnderConstruction lambda (below)

        Lambda<Map<K, CreateLock>, CreateLock>
                removeFromUnderConstruction = (underConstruction) -> {

            synchronized (lock) {
                try {
                    if (instance != null && instance == lock.finishedInstance) {
                        // if the finishedInstance changed, it means that
                        // another thread overrode it with a call to write(writeLambda)
                        // in this case, don't bother writing it.
                        this.putWithLock(key, instance, instances[0]);
                    }
                } finally {
                    underConstruction.remove(key);
                }
            }

            return null;
        };

        Runnable executor;

        // only invoke a lock on instances map if it is needed
        if (instance != null) {
            this.instances.write(insts -> {
                instances[0] = insts;
                underConstruction.write(removeFromUnderConstruction);
                instances[0] = null;
                return null;
            });

        } else {
            underConstruction.write(removeFromUnderConstruction);
        }
    }

    public Lambda<K, I> getConstruct() {
        return this.construct;
    }



    private class CreateLock {
        private final K key;
        private I finishedInstance;
        private boolean failedConstruction = false;

        private CreateLock(K forKey) {
            this.key = forKey;
        }

    }

    public static abstract class InstancesTrackerException extends RuntimeException {
        private final InstancesTracker tracker;

        protected InstancesTrackerException(InstancesTracker tracker) {
            super();
            this.tracker = tracker;
        }

        protected InstancesTrackerException(InstancesTracker tracker, String msg) {
            super(msg);
            this.tracker = tracker;
        }

        public InstancesTracker getTracker() {
            return this.tracker;
        }
    }

    // Compiler won't let Throwables have type params ... womp womp
    // Can only be a static inner class when the outer class has type params
    public static class NullReturnFromConstructLambda extends InstancesTrackerException {
        private final Object key;
        private NullReturnFromConstructLambda(InstancesTracker tracker, Object key) {
            super(tracker, "Null return to InstancesTracker " + tracker.toString()
                            + " for key.toString() value: " + key.toString());

            this.key = key;
        }

        public Object getKey() {
            return this.key;
        }

        public <K> K getKey(Class<K> keyClass) throws ClassCastException {
            return (K) key;
        }
    }

    public static class NullArgumentAsKey extends InstancesTrackerException {
        private NullArgumentAsKey(InstancesTracker tracker) {
            super(tracker, "Null key passed to InstancesTracker " + tracker.toString());
        }
    }

    public static class UnrecognizedInstance extends InstancesTrackerException {
        private Object instance;
        private Object key;

        private UnrecognizedInstance(InstancesTracker tracker, Object key, Object instance) {
            super(tracker,
                    "The passed instance was not constructed under the lock of this instance tracker.\n"
                            + (key != null ? "     key:\t" + key.toString() + "\n" : "")
                            + "instance:\t" + instance.toString());
            this.key = key;
            this.instance = instance;
        }

        private UnrecognizedInstance(InstancesTracker tracker, Object instance) {
            this(tracker, null, instance);
        }

        public Object getInstance() {
            return instance;
        }
    }
}
