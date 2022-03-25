package nessusTools.sync;

import org.apache.logging.log4j.*;

import java.util.*;

/**
 * Synchronizes and maps unique instances to a unique "key" (typically a String).
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

    private final Class<K> keyType;
    private final Class<I> instClass;
    private final Lambda1<K, I> construct;
    private final Lambda2<K, Lambda1<K, Void>, I> construct2;

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
                            Lambda1<K, I> constructionLambda) {
        this(keyType, instancesClass, constructionLambda, null);
    }

    public InstancesTracker(Class<K> keyType,
                            Class<I> instancesClass,
                            Lambda2<K, Lambda1<K, Void>, I> constructionLambda) {
        this(keyType, instancesClass, null, constructionLambda);
    }

    private InstancesTracker(Class<K> keyType,
                            Class<I> instancesClass,
                            Lambda1<K, I> construct,
                            Lambda2<K, Lambda1<K, Void>, I> construct2) {
        if (keyType == null || instancesClass == null
                || (construct == null && construct2 == null)) {
            throw new NullPointerException();
        }

        this.keyType = keyType;
        this.instClass = instancesClass;
        this.construct = construct;
        this.construct2 = construct2;

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
        return getOrMakeCreateLock(key, this.construct, this.construct2, false);
    }

    // First removes the key if it already exists, and then gains a construct
    // lock with it before running the custom one-time construction lambda
    public I constructWith(K key, Lambda1<K, I> customLambda)
            throws InstancesTrackerException, NullPointerException {
        if (customLambda == null) {
            throw new NullPointerException();
        }

        return getOrMakeCreateLock(key, customLambda, null, true);
    }

    public I constructWith(K key, Lambda2<K, Lambda1<K, Void>, I> customLambda)
            throws InstancesTrackerException, NullPointerException {
        if (customLambda == null) {
            throw new NullPointerException();
        }

        return getOrMakeCreateLock(key, null, customLambda, true);
    }

    public I getOrConstructWith(K key, Lambda1<K, I> customLambda)
            throws InstancesTrackerException, NullPointerException {

        if (customLambda == null) {
            throw new NullPointerException();
        }

        return getOrMakeCreateLock(key, customLambda, null, false);
    }

    public I getOrConstructWith(K key, Lambda2<K, Lambda1<K, Void>, I> customLambda)
            throws InstancesTrackerException, NullPointerException {

        if (customLambda == null) {
            throw new NullPointerException();
        }

        return getOrMakeCreateLock(key, null, customLambda, false);
    }

    // uses the default construction lambda for key K
    public I construct(K key) throws InstancesTrackerException {
        if (key == null) {
            return null;
        }
        return this.getOrMakeCreateLock(key, this.construct, this.construct2, true);
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

    public I read(K key, Lambda1<I, I> readLambda) {
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

    private I getOrMakeCreateLock(K key,
                                  Lambda1<K, I> construct,
                                  Lambda2<K, Lambda1<K, Void>, I> construct2,
                                  boolean overwriteIfFound)
            throws InstancesTrackerException {

        if (key == null) {
            throw new NullArgumentAsKey(this);
        }

        List<CreateLock> lockPlaceholder = new ArrayList<>(1);

        // Executor may be called with read lock or write lock, depending
        // on the value of overwriteIfFound
        Lambda1<Map<I, Set<K>>, I> executor = (instances) -> {

            for (Map.Entry<I, Set<K>> entry : instances.entrySet()) {
                if (entry.getValue().contains(key)) {
                    I instance = entry.getKey();
                    if (overwriteIfFound) {
                        // will only be invoked on write lock
                        entry.getValue().remove(key);
                        break;

                    } else if (instance == null) {
                        break;
                        //since this is a WeakHashMap, is it possible the key
                        // was garbage collected while iterating?!?!?

                    } else {
                        return instance;
                    }
                }
            }

            lockPlaceholder.add(underConstruction.write(underConstruction -> {
                CreateLock l = underConstruction.get(key);
                if (l == null) {
                    l = new CreateLock(key, construct, construct2, overwriteIfFound);
                    underConstruction.put(key, l);
                }
                return l;
            }));
            return null;
        };

        I instance;

        if (overwriteIfFound) {
            instance = instances.write(executor);

        } else {
            instance = instances.read(executor);
        }


        if (instance != null) {
            return instance;
        }

        if (lockPlaceholder.size() <= 0) {
            throw new AssertionError("Non-existent createLock but no instance to return.");
        }

        CreateLock lock = lockPlaceholder.get(0);

        if (lock == null) {
            throw new AssertionError("Non-existent createLock but no instance to return.");
        }

        instance = lock.softConstruct();

        if (instance == null) {
            throw new NullReturnFromConstructLambda(this, key);
        }

        return instance;
    }

    public Lambda1<K, I> getConstruct() {
        return this.construct;
    }

    public Lambda2<K, Lambda1<K, Void>, I> getConstruct2() {
        return this.construct2;
    }



    private class CreateLock {
        private final K key;
        private final Lambda1<K, I> construct;
        private final Lambda2<K, Lambda1<K, Void>, I> construct2;
        private final boolean override;

        private Object innerLock = new Object();
        private I finishedInstance = null;
        private Boolean failedConstruction = null;
        private boolean removedFromUnderConstruction = false;
        private Set<K> keySet = null;


        private CreateLock(final K forKey,
                            final Lambda1<K, I> construct,
                            final Lambda2<K, Lambda1<K, Void>, I> construct2,
                            final boolean override) {
            this.key = forKey;
            this.construct = construct;
            this.construct2 = construct2;
            this.override = override;
        }

        // checks for another instance on the lock first
        private I softConstruct() {
            if (this.failedConstruction != null) {
                //quick and dirty check.  Don't grab the lock unless we need to
                synchronized (this) {
                    if (this.finishedInstance != null) {
                        return this.finishedInstance;
                    }
                }
            }

            boolean runFinished = true;
            I instance = null;

            try {
                synchronized (this) {
                    //double-check, because we released the lock briefly
                    if (this.failedConstruction != null && this.finishedInstance != null) {
                        runFinished = false;
                        return this.finishedInstance;

                    } else {
                        ConstructResult<I> result = runConstruct(this.construct);
                        instance = result.instance;
                        runFinished = result.runFinished;
                    }
                }
            } finally {
                // This needs to be outside the above synchronized block to prevent deadlock
                // Reason: The lambda that creates CreateLocks grabs the create lock *while*
                // holding the locks on both instances and constructionUnderway
                // Also, The 'put' lambda passed in write() does this
                if (runFinished) {
                    I inst = instance;
                    instances.write(instances -> {
                        underConstruction.write(underConstruction -> {
                            synchronized (this) {
                                this.finished(inst, instances, underConstruction);
                            }
                            return null;
                        });
                        return null;
                    });
                }
            }

            synchronized (this) {
                if (this.finishedInstance == null && instance != null) {
                    this.finishedInstance = instance;
                }
                return this.finishedInstance;
            }
        }

        /**
         *  Only call with a lock on 'this' (createLock).  instances and underConstruction lock not needed
          */
        private ConstructResult<I> runConstruct(Lambda1<K, I> construct) {
            //Arg is so this can also be called by write(put -> put(k, i))
            I instance = null;
            boolean runFinished = true;
            Boolean[] allowAltKeys = new Boolean[] { Boolean.TRUE };

            if (construct == null) {
                Lambda1<K, Void> setAltKey = altKey -> {
                    if (!allowAltKeys[0]) {
                        throw new IllegalAccessError(
                                "Cannot call construct2 lambdas addKey(K) lambda after the write lock has been released");
                    }

                    if (altKey == null) {
                        throw new NullArgumentAsKey(InstancesTracker.this);
                    }
                    //DO NOT GRAB LOCK ON underConstruction
                    // COULD LEAD TO DEADLOCK
                    //That will be handled by finished()

                    if (this.keySet == null) {
                        this.makeKeySet();
                    }

                    this.keySet.add(altKey);
                    return null;
                };

                construct = key -> {
                    return this.construct2.call(this.key, setAltKey);
                };
            }

            try {
                instance = construct.call(key);

            } finally {
                allowAltKeys[0] = Boolean.FALSE;
                if (this.failedConstruction == null) {
                    this.failedConstruction = instance == null;
                    this.finishedInstance = instance;

                } else if (this.finishedInstance == instance
                        || instance == null) {
                    runFinished = false;

                } else {
                    // means this thread put an instance here
                    // further down the call stack
                    logger.warn("Thread put a *different* finishedInstance in createLock further down the callstack");
                    I i = instance;
                    instances.write(instances ->
                            InstancesTracker.this.putWithLock(this.key, i, instances));
                }
            }
            return new ConstructResult<I>(instance, runFinished);
        }

        /**
         * Synchronizes the moving of a newly constructed instance from the "underConstruction" map to the
         * instances map.  This should only be called while holding locks on instances, underConstruction,
         * and 'this' (createLock)
         */
        private void finished(  I instance,
                                Map<I, Set<K>> instances,
                                Map<K, CreateLock> underConstruction) {
            if (!this.removedFromUnderConstruction) {
                underConstruction.remove(key);
                this.removedFromUnderConstruction = true;
                if (this.finishedInstance != null) {
                    InstancesTracker.this.putWithLock(key, this.finishedInstance, instances);
                }
            }

            if (this.keySet != null) {
                checkAltKeys(instance, instances, underConstruction);
            }

            if (instance != this.finishedInstance &&
                    instance != null) {
                /* if the finishedInstance changed, it means that either
                 *   1) Another thread (or this thread) overrode it with a call to write(writeLambda)
                 *   2) This thread placed it first somewhere down the call stack
                 * We will respect the other thread's/call's precedence,
                 * but still place instance in the instances map with an empty keySet
                 *
                 * The caller may choose to override the other thread/call (e.g. write())
                 * after this method returns.
                 */
                if (!instances.containsKey(instance)) {
                    if (keySet == null) {
                        makeKeySet();
                    }

                    instances.put(instance, this.keySet);
                }
            }
        }

        private void makeKeySet() {
            if (InstancesTracker.this.keysComparable) {
                this.keySet = new TreeSet<>();

            } else {
                this.keySet = new LinkedHashSet<>();
            }
        }


        // only call with full write lock on instances, underConstruction, and createLock
        private void checkAltKeys(I instance,
                                  Map<I, Set<K>> instances,
                                  Map<K, CreateLock> underConstruction) {
            Set<K> actual = instances.get(instance);

            for (K alt : this.keySet) {
                boolean skip = false;
                for (Map.Entry<K, CreateLock> entry
                        : underConstruction.entrySet()) {
                    K otherKey = entry.getKey();
                    CreateLock lock = entry.getValue();
                    synchronized (lock) {
                        if (otherKey.equals(alt)
                                || (lock.keySet != null && lock.keySet.contains(alt))) {
                            skip = true;
                            if (lock.finishedInstance == null || this.override) {
                                lock.finishedInstance = instance;
                                lock.failedConstruction = Boolean.FALSE;
                                lock.finished(instance, instances, underConstruction);
                            }
                            break;
                        }
                    }
                }

                if (skip) continue;

                for (Map.Entry<I, Set<K>> entry: instances.entrySet()) {
                    Set<K> otherKeySet = entry.getValue();
                     if (otherKeySet.contains(alt)) {
                        if (override) {
                            otherKeySet.remove(alt);
                        } else {
                            skip = true;
                        }
                        break;
                    }
                }

                if (!skip) {
                    actual.add(alt);
                }
            }
            // add to master keyset ... may have some redundant, but it's a set so doesn't matter
            InstancesTracker.this.keySet.write(masterKs -> {
                masterKs.addAll(actual);
                return null;
            });
        }

        // end of CreateLock inner class
    }

    /**
     * Obtains a write lock on the instances map, which allows the passed lambda to call the "put" lambda
     * passed to it.
     *
     * Note that this method will block all read access to the instance map AND underConstruction&lt;K, CreateLock&gt;
     * map while it is executing.  It is a more direct way to put an instance into the instances map,
     * because it avoids the hassle of making and syncing a create lock.  However, the put lambda
     * will still check for a create lock for the given key, and grab that lock if it exists.  If a different
     * finishedInstance is found in this lock, that instance will be overridden and returned by the put lambda.
     * If the overriden instance was created by another thead, the other thread will end up returning this thread's
     * instance once it is able to gain a lock on the instances & underConstruction map again.
     *
     * Depending on the circumstance, write() may be a less-preferred approach than getOrConstruct or constructWith,
     * because those only grab the full write lock for the instances map (and underConstruction map) *after*
     * construction is finished.  However, depending on external synchronization needs, this may be neccessary
     * under certain circumstances.
     *
     * NOTE: DO NOT CALL WRITE FROM WITHIN THE DEFAULT CONSTRUCT LAMBDA, OR IN ANY CONSTRUCT LAMBDA WHICH DOES
     * NOT ALREADY HAVE A PRE-EXISTING WRITE LOCK.  IT COULD CAUSE DEADLOCK.
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
    public I write(Lambda1<Lambda2<K, I, I>, I> writeLambda)
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
            I displacedInstance;
            if (lock == null) {
                displacedInstance = putWithLock(key, instance, instances[0]);
                // no need to make a CreateLock, since we hold a full write lock on both maps,
                // plus there is no chance of a conflicting construction further down the call stack
                // since putWithLock is entirely under the control of this class

            } else synchronized (lock) {
                ConstructResult<I> result = lock.runConstruct(k -> instance);

                if (lock.finishedInstance != instance) {
                    displacedInstance = lock.finishedInstance;
                    lock.finishedInstance = instance;
                } else {
                    displacedInstance = null;
                }

                if (result.runFinished) {
                    lock.finished(instance, instances[0], underConstruction[0]);
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



    private static class ConstructResult<I> {
        private final I instance;
        private final boolean runFinished;

        ConstructResult(I instance, boolean runFinished) {
            this.instance = instance;
            this.runFinished = runFinished;
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
