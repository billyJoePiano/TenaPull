package nessusTools.sync;


import nessusTools.util.*;
import org.apache.logging.log4j.*;

import java.lang.ref.*;
import java.util.*;

/*
 * TODO see other TODOs , also triple-check logic behind createLock.waitForStage and checkForDeadlock
 * especially as it relates to knowing which threads are running with which locks and checking
 * against which are waiting for read/write locks on instance/underConstruction, etc to determine
 * if deadlock conditions have been reached
 */

/**
 * Synchronizes and maps unique instances to a unique "key" (typically a String or Number).
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
 * 1) getOrConstruct(K key).  A default construct lambda should be provided with the InstancesTracker
 *      constructor.  The construct lambda will be called with appropriate locks by the getOrConstruct method.
 *
 * 2) getOrConstructWith(K key, Lambda1/2) like getOrConstruct but with a custom construction lambda instead of the
 *      default lambda.
 *
 *
 * 3) constructWith(K key, Lambda1/2) Construct with a custom lambda, the returned instance will override any
 *      previously existing instance associated with the given key.
 *
 * Note that all construct lambda MUST return a non-null value of instance type I or a
 *      NullInstance runtime exception will be thrown (no need to be declared or caught)
 *      EDIT: 4/13 removed this requirement to allow searching while holding a CreateLock in MapLookupDao
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

    private static final WeakReference
            UNDER_CONSTRUCTION_PLACEHOLDER = new WeakReference(null);

    private final Class<K> keyType;
    private final Class<I> instType;
    private final Lambda1<K, I> construct;

    private final ReadWriteLock<Map<I, Set<K>>, Object>
            instances = ReadWriteLock.forMap(new WeakHashMap());

    private final ReadWriteLock<Map<K, CreateLock>, CreateLock>
            underConstruction = ReadWriteLock.forMap(new LinkedHashMap());

    private final ReadWriteLock<Map<Thread, CreateLock>, CreateLock>
            constructThreads = ReadWriteLock.forMap(new WeakHashMap<>());

    //lazily constructed unmodifiable views of the keys for each instance
    private final ReadWriteLock<Map<I, Set<K>>, Set<K>>
            keySetViews = ReadWriteLock.forMap(new WeakHashMap());


    private final KeySet keySet = new KeySet();

    private final boolean keysComparable;
    private final boolean keysFinalizable;
    private final KeyValueType type;

    private InstancesTracker<K, I> replacedWith;



    public InstancesTracker(Class<K> keyType,
                            Class<I> instancesType,
                            Lambda1<K, I> constructionLambda)
            throws NullPointerException {

        this(new Type(keyType), new Type(instancesType), constructionLambda);
    }

    public InstancesTracker(Type<K> keyType,
                            Type<I> instType,
                            Lambda1<K, I> constructionLambda)
            throws NullPointerException {

        this.keyType = keyType.getType();
        this.instType = instType.getType();
        this.construct = constructionLambda;

        this.keysComparable = Comparable.class.isAssignableFrom(this.keyType);
        this.keysFinalizable = KeyFinalizer.class.isAssignableFrom(this.keyType);

        this.type = new KeyValueType(keyType, instType);

        registerTracker(this);
    }


    public Set<K> keySet() {
        return this.keySet;
    }

    public int size() {
        return instances.read(Integer.class, instances ->
            underConstruction.read(Integer.class, underConstruction -> {
                Set<I> counter = new LinkedHashSet<>(instances.keySet());
                for (Map.Entry<K, CreateLock> entry : underConstruction.entrySet()) {
                    CreateLock lock = entry.getValue();
                    I instance = lock.run(entry.getKey());
                    if (instance != null) {
                        counter.add(instance);
                    }
                }
                return counter.size();
            })
        );
    }

    public I get(K key) {
        //iol = instance or lock
        Object iol = this.instances.read(instances -> {
            for (Map.Entry<I, Set<K>> entry : instances.entrySet()) {
                if (entry.getValue().contains(key)) {
                    return entry.getKey();
                }
            }

            return underConstruction.read(
                    underConstruction -> underConstruction.get(key));
        });

        if (iol == null) {
            return null;
        }

        if (instType.isInstance(iol)) {
            return (I) iol;
        }

        CreateLock lock = (CreateLock) iol;

        return lock.run(lock.key);
    }

    public List<I> get(Lambda1<I, Boolean> filter) {
        return this.get(filter, 0);
    }

    public List<I> get(Lambda1<I, Boolean> filter, int limit) {
        List<I> accepted = new LinkedList();
        List<I> rejected = new LinkedList();
        this.instances.read(instances -> {
            for(Map.Entry<I, Set<K>> entry : instances.entrySet()) {
                I instance = entry.getKey();
                if (entry.getValue().size() > 0) {
                    Boolean filterResult = filter.call(instance);
                    if (filterResult != null && filterResult) {
                        accepted.add(instance);
                        if (limit <= 0 || accepted.size() >= limit) {
                            return null;
                        }
                        continue;
                    }
                }
                rejected.add(instance);
            }

            this.underConstruction.read(underConstruction -> {
                for (Map.Entry<K, CreateLock> entry : underConstruction.entrySet()) {
                    CreateLock lock = entry.getValue();
                    I instance = lock.run(entry.getKey());

                    //instances comparison instead of list.contains() which requires .equals() method
                    if (instance == null) continue;
                    for (I other : accepted) {
                        if (instance == other) {
                            instance = null;
                            break;
                        }
                    }

                    if (instance == null) continue;
                    for (I other : rejected) {
                        if (instance == other) {
                            instance = null;
                            break;
                        }
                    }
                    if (instance == null) continue;

                    Boolean filterResult = filter.call(instance);
                    if (filterResult != null && filterResult) {
                        accepted.add(instance);
                        if (limit <= 0 || accepted.size() >= limit) {
                            return null;
                        }

                    } else {
                        rejected.add(instance);
                    }
                }
                return null;
            });
            return null;
        });
        return accepted;
    }


    public I getOrConstruct(K key)
            throws InstancesTrackerException {
        return getOrMakeCreateLock(key, this.construct,false);
    }

    // First removes the key if it already exists, and then gains a construct
    // lock with it before running the custom one-time construction lambda
    public I constructWith(K key, Lambda1<K, I> customLambda)
            throws InstancesTrackerException, NullPointerException {
        if (customLambda == null) {
            throw new NullPointerException();
        }

        return getOrMakeCreateLock(key, customLambda, true);
    }

    public I getOrConstructWith(K key, Lambda1<K, I> customLambda)
            throws InstancesTrackerException, NullPointerException {

        if (customLambda == null) {
            throw new NullPointerException();
        }

        return getOrMakeCreateLock(key, customLambda,  false);
    }

    // uses the default construction lambda for key K
    public I construct(K key) throws InstancesTrackerException {
        if (key == null) {
            return null;
        }
        return this.getOrMakeCreateLock(key, this.construct, true);
    }

    public I remove(K key) {
        return this.instances.write(instType, instances -> {
            Set<K> keySet;
            for (Map.Entry<I, Set<K>> entry : instances.entrySet()) {
                keySet = entry.getValue();
                if (keySet.contains(key)) {
                    keySet.remove(key);
                    this.keySet.keysMap.write(map -> map.remove(key));
                    return entry.getKey();
                }
            }


            CreateLock lock = this.underConstruction.write(underConstruction ->
                underConstruction.remove(key));

            if (lock != null) {
                return lock.run(key);

            } else {
                return null;
            }
        });
    }

    public Set<K> getKeysFor(I instance)
            throws UnrecognizedInstance {

        return instances.write(Set.class, instances -> {
            Set<K> keySet = getKeysForWithLock(instance, instances);

            return keySetViews.write(views -> {
                Set<K> view = views.get(instance);
                if (view != null) {
                    return view;
                }

                view = Collections.unmodifiableSet(keySet);
                views.put(instance, view);
                return view;
            });
        });
    }

    public Set<K> clearKeysFor(I instance)
            throws UnrecognizedInstance {

        return instances.write(Set.class, instances -> {
            Set<K> keySet = getKeysForWithLock(instance, instances);
            Set<K> copy = this.makeKeySet();
            copy.addAll(keySet);
            keySet.clear();
            return copy;
        });
    }

    // with write lock on instances ... this will obtain the lock on underConstruction
    private Set<K> getKeysForWithLock(I instance,
                               Map<I, Set<K>> instances)
            throws UnrecognizedInstance {

        // start by finalizingConstruction of all createLocks with references to this instance
        underConstruction.write(underConstruction -> {
            for (Map.Entry<K, CreateLock> entry : underConstruction.entrySet()) {
                CreateLock lock = entry.getValue();
                if (instance == lock.run(entry.getKey())) {
                    lock.finalizeConstruction(instances, underConstruction);
                }
            }
            return null;
        });

        Set<K> keySet = instances.get(instance);
        if (keySet == null) {
            throw new UnrecognizedInstance(this, instance);
        }
        return keySet;
    }

    public Set<I> getInstances() {
        return instances.read(Set.class, instances -> {
            Set<I> all = new LinkedHashSet(instances.keySet());
            this.underConstruction.read(underConstruction -> {
                for (Map.Entry<K, CreateLock> entry : underConstruction.entrySet()) {
                    I instance = entry.getValue().run(entry.getKey());
                    if (instance != null) {
                        all.add(instance);
                    }
                }
                return null;
            });

            return all;
        });
    }


    public I put(K key, I instance)
            throws UnrecognizedInstance {

        if (key == null) {
            throw new NullKeyArgument(this);

        } else if (instance == null) {
            throw new NullInstanceArgument(this, key);
        }

        CreateLock myLock = this.getCurrentThreadLock();

        return instances.write(instType, instances -> {
            for (I other: instances.keySet()) {
                if (instance == other) {
                    return putWithLock(key, instance, instances);
                }
            }

            return underConstruction.write(instType, underConstruction -> {
                CreateLock otherLock = underConstruction.get(key);

                if (myLock == null) {
                    // this invocation is NOT coming from within a createLock construct lambda
                    if (otherLock.run(key) == instance) {
                        I displaced = putWithLock(key, instance, instances);
                        underConstruction.remove(otherLock.key);
                        return displaced;
                    }

                    for (CreateLock l : underConstruction.values()) {
                        if (l.run(l.key) == instance) {
                            return putWithLock(key, instance, instances);
                        }
                    }

                    throw new UnrecognizedInstance(this, instance);
                }


                // ... else this invocation IS coming from within a createLock construct lambda
                if (myLock.forceOverwrite && otherLock == null) {
                    /**
                     *  put directly into the instances map only when this is
                     *  a forceOverwriting lock and there is no create lock for
                     *  this key.  Non-forceOverwriting locks should provide an
                     *  opportunity to be overridden by other threads before
                     *  their construction is finalized
                     */

                    return putWithLock(key, instance, instances);

                } else if (otherLock != null && otherLock.forceOverwrite && !myLock.forceOverwrite) {
                    //need to splice in altLock as the new 'startOfChain', and reset its key...
                    CreateLock altLock = new CreateLock(otherLock.key, null, false);
                    //canonical key instance will be from overwriting lock

                    // don't need to provide construct lambda, because we will just take a "shortcut"...
                    altLock.startOfChain = true;
                    altLock.finishedInstance = instance;
                    altLock.stage = Stage.RETURNED;

                    synchronized (otherLock) {
                        otherLock.startOfChain = false;
                        otherLock.overrides = altLock;

                        underConstruction.put(otherLock.key, altLock);
                    }

                    return altLock.overrideWith(otherLock, false, otherLock.key);
                }

                // ... else put in underConstruction queue ...
                CreateLock altLock
                        = new CreateLock(key, null, myLock.forceOverwrite);

                // don't need to provide construct lambda, because we will just take a "shortcut"...
                altLock.finishedInstance = instance;
                altLock.stage = Stage.RETURNED;

                if (otherLock != null) {
                    return otherLock.overrideWith(altLock, false, otherLock.key);
                }

                // If this point is reached, myLock.forceOverwrite must be false
                altLock.startOfChain = true;
                this.keySet.keysMap.write(map -> map.put(key, UNDER_CONSTRUCTION_PLACEHOLDER));
                underConstruction.put(key, altLock);
                return null;

            });
        });
    }

    private CreateLock getCurrentThreadLock() {
        return this.constructThreads.read(threadLocks -> threadLocks.get(Thread.currentThread()));
    }



    /**
     * Private method only intended to be called with the lock on the instances map
     * (and optionally the underConstruction map)
     *
     * Each caller to this method should obtain those locks and any other operations on the
     * underConstruction map needed
     *
     * @param key
     * @param instance
     * @param instances
     * @return displaced instance
     */
    private I putWithLock(K key, I instance,
                          Map<I, Set<K>> instances) {
        if (key == null) {
            throw new NullKeyArgument(this);

        } else if (instance == null) {
            throw new NullInstanceArgument(this, key);
        }

        this.keySet.keysMap.write(map ->
            map.put(key, new WeakReference<>(instance)));

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

        if (foundInstance) {
            return displacedInstance;
        }


        Set<K> keySet = makeKeySet();

        keySet.add(key);
        instances.put(instance, keySet);

        return displacedInstance;
    }

    private I getOrMakeCreateLock(K key,
                                  Lambda1<K, I> construct,
                                  boolean overwriteIfFound)
            throws InstancesTrackerException {

        if (key == null) {
            throw new NullKeyArgument(this);
        }

        CreateLock lock = this.getCurrentThreadLock();

        if (lock == null) {
            return overwriteIfFound
                    ? this.overwriteIfFound(key, construct)
                    : this.noOverwrite(key, construct);
        }


        throw new CreateLockException(this, lock.key);
        /*
        return instances.write(instType, instances -> {

        });
         */

    }

    private I noOverwrite(K key, Lambda1<K, I> construct) {
        Var<CreateLock> lock = new Var(); // the lock to grab constructLock on
        Var<CreateLock> startOfChain = new Var(); //the lock to call run() on... may or may not be the same as lock
        I instance;

        instance = this.instances.read(this.instType, instances -> {
            if (this.replacedWith != null) {
                throw new TrackerDiscontinuedException(this);
            }

            I inst = this.keySet.keysMap.write(this.instType, map -> {
                WeakReference<I> ref = map.get(key);
                if (ref != null) {
                    if (ref == UNDER_CONSTRUCTION_PLACEHOLDER) {
                        return null;
                    }

                    I i = ref.get();

                    if (i != null) {
                        return i;

                    } else {
                        map.remove(key);
                    }
                }

                map.put(key, UNDER_CONSTRUCTION_PLACEHOLDER);
                return null;
            });

            if (inst != null) {
                return inst;
            }

            if (underConstruction.write(
                    Boolean.class,
                    underConstruction -> {

                startOfChain.value = underConstruction.get(key);

                if (startOfChain.value != null) {
                    return true;
                }

                startOfChain.value = lock.value
                        = new CreateLock(key, construct, false);

                lock.value.startOfChain = true;
                underConstruction.put(key, lock.value);

                return false;

            })) {
                // returns true when this was an established lock, not a new one
                inst = startOfChain.value.run(startOfChain.value.key);
                if (inst == null) {
                    lock.value = new CreateLock(key, construct, false);
                    startOfChain.value.overrideWith(lock.value, true, startOfChain.value.key);
                }
            }

            return inst;
        });

        if (instance != null) {
            return instance;
        }

        synchronized (lock.value.constructLock) {
            return startOfChain.value.run(startOfChain.value.key);
        }
    }

    private I overwriteIfFound(K key,
                               Lambda1<K, I> construct) {

        Var<CreateLock> startOfChain = new Var();
        CreateLock overridingLock = new CreateLock(key, construct, true);

        this.instances.write(instances -> {
            return underConstruction.write(underConstruction -> {
                if (this.replacedWith != null) {
                    throw new TrackerDiscontinuedException(this);
                }

                this.keySet.keysMap.write(Void.class, map -> {
                    WeakReference<I> ref = map.get(key);

                    if (ref != null) {
                        map.remove(key);
                        if (ref != UNDER_CONSTRUCTION_PLACEHOLDER) {
                            I instance = ref.get();
                            if (instance != null) {
                                instances.get(instance).remove(key);
                            }
                        }
                    }

                    map.put(key, UNDER_CONSTRUCTION_PLACEHOLDER);
                    return null;
                });

                startOfChain.value = underConstruction.get(key);

                if (startOfChain.value == null) {
                    startOfChain.value = overridingLock;
                    overridingLock.startOfChain = true;
                    underConstruction.put(key, overridingLock);

                } else {
                    underConstruction.remove(startOfChain.value.key);
                    underConstruction.put(key, startOfChain.value);
                    startOfChain.value.overrideWith(overridingLock, true, key);
                }
                return null;
            });
        });

        synchronized (overridingLock.constructLock) {
            return startOfChain.value.run(startOfChain.value.key);
        }
    }

    public Lambda1<K, I> getConstruct() {
        return this.construct;
    }

    private Set<K> makeKeySet() {
        Set<K> keySet;
        if (InstancesTracker.this.keysComparable) {
            keySet = new TreeSet<>();

        } else {
            keySet = new LinkedHashSet<>();
        }
        return keySet;
    }

    private enum Stage {
        IDLE,
        PRE_CONSTRUCT,
        CONSTRUCT,
        POST_CONSTRUCT,
        RETURNED, // includes thrown exceptions
        FINALIZING,
        FINALIZED
    }

    public interface KeyFinalizer<K extends KeyFinalizer<K, I>, I> {
        public void finalizeKey(I instance);
    }


    static Var.Long lockIdCounter = new Var.Long(Long.MIN_VALUE);
    private static long getCreateLockId(InstancesTracker.CreateLock lock) {
        synchronized (lockIdCounter) {
            return lockIdCounter.value++;
        }
    }
    
    private static Set<InstancesTracker.CreateLock> locks;

    private class CreateLock implements Comparable<InstancesTracker<?, ?>.CreateLock> {
            //raw InstancesTracker type param for Comparable, so that CreateLocks can be compared BETWEEN trackers

        private final long id = getCreateLockId(this);
        private final K key;
        private final Lambda1<K, I> construct;
        private final boolean forceOverwrite;

        /**
         * constructLock is only for the thread calling the construct lambda.
         * The lock itself is grabbed to check or modify any mutuable property,
         * -- typically the stage or overridenBy properties.
         *
         * The critical different between the CreateLock itself and construct lock is that:
         * 1) The constructLock *must* be locked from OUTSIDE of a lock on the outer 'instances' and
         *      'underConstruction' maps, though it may grab these locks from WITHIN its lock, when
         *      the construct lambda calls instancesTracker.get/put() or similar operations.
         *      Conversely, the createLock lock CAN be grabbed from within a lock on instances,
         *      underConstruction or another createLock.
         *
         * 2) A thread holding a constructLock cannot grab a lock on another constructLock as this could
         *      cause deadlock.  It can however grab any other createLock to check on its stage of
         *      construction, add itself to the waiting set, etc.
         *
         * Note that multiple checkpoint locks should NOT be grabbed at once, or it
         * may cause deadlock.  Even when recursing through a chain of "overriddenBy", a createLock
         * further down the chain may be running construct() on a different thread which may attempt
         * an instancesTracker.get/put() type of operation, which would then try grabbing the
         * checkpoint lock higher up the same chain... thus leading to deadlock between the two
         * threads.
         */
        private final Object constructLock = new Object();

        private Stage stage = Stage.IDLE;
        private boolean startOfChain = false;
        private I finishedInstance = null;
        private CreateLock overrides = null;
        private CreateLock overriddenBy = null;
        private Thread constructThread = null;

        private CreateLock(final K forKey,
                            final Lambda1<K, I> construct,
                            final boolean forceOverwrite) {

            this.key = forKey;
            this.construct = construct;
            this.forceOverwrite = forceOverwrite;
        }

        /**
         * Used to sort priority of interrupts when there is a deadlock cycle between threads
         * Non-overriding locks will always be interrupted first, and earlier-created locks of
         * the same 'override' status will be interrupted first
         *
         * @param other Another createLock from any instanceTracker (non-parameterized)
         * @return Which create lock's thread should be interrupted first, in event of
         * a deadlock
         */
        public int compareTo(InstancesTracker<?, ?>.CreateLock other) {
            if (this.forceOverwrite && !other.forceOverwrite) {
                return 1;

            } else if (!this.forceOverwrite && other.forceOverwrite) {
                return -1;

            } else if (this.id < other.id) {
                return -1;

            } else {
                return 1;
            }
        }

        /**
         *  Should only be called with a lock on 'constructLock' (createLock), OR from outside
         *  the scope of any createLock AFTER the primary run() thread has already been
         *  invoked/finished from its primary run thread.  A lock on instances and
         *  underConstruction is not needed, and should be avoided if holding the
         *  constructLock because it could cause deadlock.
          */

        /*
        private I run() throws CreateLockException {
            return this.run(this.key);
        }
         */

        private I run(K key) throws CreateLockException {
            boolean runConstruct = false;
            boolean waitForPostConstruct = false;

            //check stage
            synchronized (this) {
                if (this.stage == Stage.IDLE
                        && Thread.holdsLock(this.constructLock)) {
                    
                    this.stage = Stage.PRE_CONSTRUCT;
                    runConstruct = true;

                    this.constructThread = Thread.currentThread();
                    InstancesTracker.this.constructThreads.write(tl -> tl.put(this.constructThread, this));

                    this.notifyAll();

                } else {
                    waitForPostConstruct = this.stage.ordinal() < Stage.POST_CONSTRUCT.ordinal();
                }
            }

            if (runConstruct) {
                return this.runConstruct(key);

            } else {
                return getResult(waitForPostConstruct, key);
            }


        }

        private I runConstruct(K key) {
            boolean callConstruct;
            synchronized (this) {
                this.stage = Stage.CONSTRUCT;
                callConstruct = this.forceOverwrite || this.overriddenBy == null;
            }

            try {
                if (callConstruct || this.overriddenBy.run(key) == null) {
                    try {
                        this.finishedInstance = construct.call(key);

                    } catch (Exception e) {
                        logger.error("Error while constructing instance of type "
                                + InstancesTracker.this.instType);
                        logger.error(e);
                        throw e;
                    }
                }

                synchronized (this) {
                    //this.failedConstruction = this.finishedInstance == null;
                    this.stage = Stage.POST_CONSTRUCT;
                    this.notifyAll();
                }

                return this.getResult(false, key);

            } finally {
                synchronized (this) {
                    InstancesTracker.this.constructThreads.write(tl -> tl.remove(this.constructThread));
                    this.constructThread = null;
                    this.stage = Stage.RETURNED;
                    this.notifyAll();
                    this.constructLock.notifyAll(); //probably not necessary?  do we ever call constructLock.wait ??
                }
                notifyFinalizer();
            }
        }

        private I getResult(boolean waitForPostConstruct, K key) {
            if (waitForPostConstruct) {
                this.waitForStage(Stage.POST_CONSTRUCT);
            }

            Thread currentThread = null;
            synchronized (this) {
                if (this.overriddenBy == null) {
                    return this.finishedInstance;
                }
            }

            I instance = this.overriddenBy.run(key);
            if (instance != null) {
                return instance;

            } else {
                return this.finishedInstance;
            }
        }


        /**
         * Waits for a lock to reach the given stage, but checks for deadlock
         * cycles.  If the current thread is found to be part of such a deadlock
         * cycle, it will return *without* waiting for the given stage.
         *
         * IMPORTANT: Do NOT call while holding any createLocks
         */
        private boolean waitForStage(Stage stage) {
            assert !Thread.holdsLock(this);

            Thread currentThread = Thread.currentThread();

            synchronized (this) {
                if (this.stage.ordinal() >= stage.ordinal()) {
                    return true;

                } else if (this.constructThread != null
                        && Objects.equals(this.constructThread, currentThread)) {

                    return false;
                }
            }

            try {
                while(true) {
                    setWaitingStatus(this);
                    synchronized (this) {
                        if (this.stage.ordinal() >= stage.ordinal()) {
                            break;
                        }
                        this.wait(1000);
                    }
                }

            } catch (InterruptedException e) {
                synchronized (this) {
                    return this.stage.ordinal() >= stage.ordinal();
                }
                // deadlockBreaker daemon automatically clears waiting status when it interrupts a thread
            }

            clearWaitingStatus();
            return true;
        }

        /**
         * ONLY CALL WITH LOCK ON instances and underConstruction, with at least one of the two
         *  as a write lock, to prevent race conditions between overriding and finalizing!!
         *
         * 4-16-22 -- 5 invocations total found:
         *  1 ) recursive call to self, to walk the chain of overrides
         * 2,3) TWO invocations in instancesTracker.put (different permutations
         *      of which lock overrides which, depending on override priority)
         *
         *  4 ) In notOverwrite, for situations where the "backup plan" is needed
         *      (i.e. the startOfChain.run() returned null)
         *
         *  5 ) In overwrite, for obvious reasons...
         *
         */

        private I overrideWith(CreateLock otherLock, boolean skipRun, K key) {
            //typically this would be called from a different thread than the thread that created the lock
            assert !(otherLock == this
                    || otherLock.startOfChain
                    || (this.forceOverwrite && !otherLock.forceOverwrite));


            boolean origSkipRun = skipRun;
            // ^^^ to pass on the original argument recursively, since skipRun may change below

            synchronized (this) {
                if (this.stage == Stage.FINALIZED) {
                    // ...shouldn't happen as long as there is a lock on instances and/or underConstruction
                    throw new AlreadyFinalized(InstancesTracker.this, this);
                }

                if (this.overriddenBy != null) {
                    if (!this.forceOverwrite) {
                        skipRun = true; //effectively a "delegateRun" IF origSkipRun is false
                                        // ... delegate to the overridding create lock

                        if (this.overriddenBy.forceOverwrite && !otherLock.forceOverwrite) {

                            // splice otherLock in between, and use the current
                            // overriddenBy lock in place of "otherLock"
                            otherLock.overrides = this;
                            CreateLock forceOverWriting = this.overriddenBy;
                            this.overriddenBy = otherLock;
                            otherLock = forceOverWriting;

                            //IMPORTANT, the "forceOverWriting" lock will NOT have its "overrides"
                            // property mutated, because grabbing it could lead to deadlock.
                            // THEREFORE, when traversing upstream, the initial "otherLock" (now this.overriddenBy)
                            // will be skipped by the forceOverWriting lock (initial "this.overriddenBy") overrides property
                            // Upstream traversal should ONLY be used to reach the top of the chain,
                            // and then any operations conducted by traversing should proceed in a downstream
                            // order
                        }
                    }

                } else if (this.stage.ordinal() >= Stage.POST_CONSTRUCT.ordinal() //already constructed
                            || skipRun                              // hasn't started yet, but this thread indicates it will start after returning
                            || (this.constructThread != null              // is the current thread
                                && Objects.equals(this.constructThread, // ...meaning this is happening due to an invocation from within a construct lambda
                                                Thread.currentThread()))) {

                        this.overriddenBy = otherLock;
                        otherLock.overrides = this;
                        return this.finishedInstance; //the displaced instance
                }
            }

            I displaced;

            if (skipRun) {
                displaced = this.finishedInstance;

            } else {
                displaced = this.run(key);
            }

            synchronized (this) {
                if (this.stage == Stage.FINALIZED) {
                    // ...shouldn't happen as long as there is a lock on instances and/or underConstruction
                    throw new AlreadyFinalized(InstancesTracker.this, this);
                }

                if (this.overriddenBy == null) {
                    this.overriddenBy = otherLock;
                    otherLock.overrides = this;
                    if (displaced != null) {
                        return displaced;

                    } else {
                        return this.finishedInstance;
                    }

                }
            }

            I priorityDisplaced = this.overriddenBy.overrideWith(otherLock, origSkipRun, key);
            if (priorityDisplaced != null) {
                return priorityDisplaced;

            } else if (displaced != null) {
                return displaced;

            } else {
                synchronized (this) {
                    return this.finishedInstance;
                }
            }
        }


        /**
         * Synchronizes the moving of a newly constructed instance from the "underConstruction" map to the
         * instances map.  This should only be called while holding locks on 'this' (createLock, instances,
         * and underConstruction)
         */
        private boolean finalizeConstruction(Map<I, Set<K>> instances,
                                             Map<K, CreateLock> underConstruction) {

            Thread current = Thread.currentThread();
            if (!markAsFinalizing(current)) {
                return false;
            }

            I instance;
            try {
                instance = this.run(this.key);

            } catch (Exception e) {
                logger.error(e);
                return false;
            }

            synchronized (this) {
                underConstruction.remove(this.key);
                if (instance != null) {
                    checkForInstancesWithoutKey(instances, instance);
                    InstancesTracker.this.putWithLock(this.key, instance, instances);

                }
            }

            int count = markAsFinalized(current);
            if (count == -1) {
                logger.warn("Failure to mark entire createLock chain as 'FINALIZED',"
                        + "reverting state of instances and underConstruction maps"
                        + "\nKey: " + key
                        + "\nInstance: " + instance);
                underConstruction.put(this.key, this);
                if (instance != null) {
                    instances.get(instance).remove(this.key);
                }
                return false;

            }

            if (instance == null) {
                // remove from keySet if necessary
                InstancesTracker.this.keySet.keysMap.write(map -> {
                    WeakReference<I> ref = map.get(this.key);
                    if (ref == UNDER_CONSTRUCTION_PLACEHOLDER
                            || (ref != null
                                && ref.get() == null)) {

                        map.remove(this.key);
                    }
                    return null;
                });
            }

            if (InstancesTracker.this.keysFinalizable) {
                KeyFinalizer<?, I> kf = (KeyFinalizer<?, I>) this.key;
                kf.finalizeKey(instance);
            }

            synchronized (InstancesTracker.this.comparable) {
                if (underConstruction.size() == 0) InstancesTracker.this.comparable.notificationCount = 0;
                else InstancesTracker.this.comparable.notificationCount -= count;
            }
            return true;
        }

        /**
         *
         *
         * @return
         */
        private boolean markAsFinalizing(Thread currentThread) {
            boolean wait;
            CreateLock overriddenBy = null;
            synchronized (this) {
                if (this.stage.ordinal() < Stage.RETURNED.ordinal()
                    || this.stage == Stage.FINALIZED) {

                    return false;
                }

                this.stage = Stage.FINALIZING;
                this.notifyAll();
            }

            while (true) {
                if (overriddenBy != null && !overriddenBy.markAsFinalizing(currentThread)) {
                    return false;
                }

                synchronized (this) {
                    if (this.overriddenBy == overriddenBy) {
                        this.constructThread = currentThread;
                        return true;
                    }
                }
                synchronized (this) {
                    overriddenBy = this.overriddenBy;
                }
            }
        }

        /**
         * Adds finishedInstance to the instances map if it is being overridden
         * by another instance returned from run(), and isn't already in the instances
         * map.  Recurses down the chain of overriding createLocks
         */
        private void checkForInstancesWithoutKey(Map<I, Set<K>> instances,
                                                 I instanceWithKey) {
            CreateLock overriddenBy;

            synchronized (this) {
                if (this.stage == Stage.FINALIZING
                        && this.finishedInstance != null
                        && this.finishedInstance != instanceWithKey) {

                    boolean found = false;
                    for (I other : instances.keySet()) {
                        if (other == this.finishedInstance) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        instances.put(this.finishedInstance,
                                InstancesTracker.this.makeKeySet());
                    }
                }
                overriddenBy = this.overriddenBy;
            }

            while (overriddenBy != null) {
                overriddenBy.checkForInstancesWithoutKey(instances, instanceWithKey);

                // check for (unlikely but possible) situation where another thread
                // spliced a new non-overriding createLock in between the old overriddenBy
                // createLock and this lock
                synchronized (this) {
                    if (overriddenBy != this.overriddenBy) {
                        overriddenBy = this.overriddenBy;

                    } else {
                        overriddenBy = null;
                    }
                }
            }
        }

        private int markAsFinalized(Thread currentThread) {
            CreateLock overriddenBy = null;
            synchronized (this) {
                if (this.stage.ordinal() < Stage.FINALIZING.ordinal()) {
                    return -1;
                }

                overriddenBy = this.overriddenBy;
                if (this.stage != Stage.FINALIZED) {
                    this.stage = Stage.FINALIZED;
                    this.notifyAll();
                }
            }

            while (true) {
                int count = 0;
                if (overriddenBy != null) {
                    count = overriddenBy.markAsFinalized(currentThread);
                    if (count == -1) {
                        return -1;
                    }
                }
                synchronized (this) {
                    if (this.overriddenBy == overriddenBy) {
                        return count + 1;
                    }

                    overriddenBy = this.overriddenBy;
                }
            }
        }

        // end of CreateLock inner class
    }

    public class KeySet implements Set<K> {
        private final ReadWriteLock<Map<K, WeakReference<I>>, Object>
                keysMap = ReadWriteLock.forMap(new WeakHashMap());

        private final ReadWriteLock<Map<I, Set<K>>, Object>
                instances = InstancesTracker.this.instances;

        @Override
        public int size() {
            return instances.read(Integer.class, map -> {
                int size = 0;
                for (Set<K> keySet : map.values()) {
                    size += keySet.size();
                }
                return size;
            });
        }

        @Override
        public boolean isEmpty() {
            return instances.read(Boolean.class, map -> {
                for (Set<K> keySet : map.values()) {
                    if (keySet.size() > 0) {
                        return false;
                    }
                }
                return true;
            });
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) return false;
            if (!InstancesTracker.this.keyType.isInstance(o)) {
                return false;
            }
            K other = (K) o;
            return keysMap.read(Boolean.class, map -> map.containsKey(other));
        }

        @Override
        public Iterator<K> iterator() {
            return null; //TODO
        }

        @Override
        public Object[] toArray() {
            return null; //TODO
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null; //TODO
        }

        @Override
        public boolean add(K k) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Tracker of trackers.  Used primarily for thread comparisons
     */
    private static final ReadWriteLock<
            Map<KeyValueType, WeakReference<InstancesTracker>>,
            InstancesTracker>
            trackers = ReadWriteLock.forMap(new WeakHashMap<>());

    private static <K, I> void registerTracker(InstancesTracker<K, I> tracker) {

        InstancesTracker<K, I> displaced = trackers.write(trackers -> {
            WeakReference<InstancesTracker> displacedRef
                    = trackers.put(tracker.type, new WeakReference<>(tracker));

            if (displacedRef == null) return null;
            InstancesTracker dspl = displacedRef.get();
            if (dspl != null) {
                trackers.put(new DiscontinuedTrackerPlaceholder(), displacedRef);
                //needed so that running create locks can be found & and interrupted if there are any deadlocks
            }
            return dspl;
        });

        if (displaced != null) displaced.discontinue(tracker);
    }

    private void discontinue(InstancesTracker<K, I> replacedWith) {
        this.instances.write(instances ->
                this.underConstruction.write(uc -> {
                    if (this.replacedWith != null) {
                        this.replacedWith.discontinue(replacedWith);

                    } else {
                        this.replacedWith = replacedWith;
                    }

                    for (CreateLock lock : uc.values()) {
                        lock.run(lock.key);
                    }

                    return null;
                })
        );
    }


    private static ReadWriteLock<Map<Thread, InstancesTracker.CreateLock>, InstancesTracker.CreateLock>
            waitingThreads = ReadWriteLock.forMap(new WeakHashMap<>());


    private static void setWaitingStatus(InstancesTracker.CreateLock lock) {
        InstancesTracker.CreateLock displaced;
        Thread current = Thread.currentThread();
        displaced = waitingThreads.write(wt -> wt.put(current, lock));

        synchronized (waitingThreads) {
            waitingThreads.notifyAll();
        }

        Thread constructThread;
        synchronized (lock) {
            constructThread = lock.constructThread;
        }


        /*
        // For concurrency debugging...
        if (displaced == null) {
            logger.info(lock + "\t" + current + "\t" + constructThread);

        } else if (displaced != lock) {
            logger.error("DISPLACED DIFFERENT LOCK!");
            logger.error(lock + "\t" + current + "\t" + constructThread);
            logger.error(displaced);
        }

         */

    }

    private static void clearWaitingStatus() {
        waitingThreads.write(wt -> wt.remove(Thread.currentThread()));
        Thread.interrupted();
    }

    public static RecursiveMap<Thread> getThreadBlockingMap() {
        return getThreadBlockingMap(ReadWriteLock.getThreadBlockingMap());
    }

    public static RecursiveMap<Thread> getThreadBlockingMap(RecursiveMap<Thread> preFilled) {
        //switch out waitingThreads and altWaitingThreads
        return waitingThreads.read(RecursiveMap.class, wt ->
                getThreadBlockingMapWithLock(preFilled, wt));
    }

    private static RecursiveMap<Thread> getThreadBlockingMapWithLock(
            RecursiveMap<Thread> threads,
            Map<Thread, InstancesTracker.CreateLock> waitingThreads) { //typically a copy -- 'altWaitingThreads'

        for (Map.Entry<Thread, InstancesTracker.CreateLock> entry
                : waitingThreads.entrySet()) {

            Thread thread = entry.getKey();
            InstancesTracker.CreateLock lock = entry.getValue();

            while (lock != null) {
                synchronized (lock) {
                    if (lock.overrides != null) {
                        //traverse upstream first, to the top of the chain
                        lock = lock.overrides;

                    } else {
                        if (lock.constructThread != null) {
                            threads.putChild(lock.constructThread, thread);
                        }
                        lock = lock.overriddenBy;
                    }
                }
            }
        }
        return threads;
    }

    private static Thread deadlockBreaker = new Thread(() -> {
        Thread deadlockBreaker = Thread.currentThread();
        deadlockBreaker.setPriority(Thread.MAX_PRIORITY);

        while(true) try {
            synchronized (waitingThreads) {
                while (waitingThreads.read(Boolean.class, wt -> wt.size() <= 0)) {
                    try {
                        waitingThreads.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }

            //switch out waitingThreads and altWaitingThreads
            Map<Thread, InstancesTracker.CreateLock>
                    wtCopy = waitingThreads.write(Map.class, wt -> new LinkedHashMap(wt));


            ReverseMap<Thread, InstancesTracker.CreateLock>
                    ctCopy = new ReverseMap<>(LinkedHashMap.class, TreeSet.class);

            Set<WeakReference<InstancesTracker>> trckrs = trackers.read(
                    Set.class, trackers -> new LinkedHashSet(trackers.values()));

            for (WeakReference<InstancesTracker> ref : trckrs) {
                InstancesTracker tracker = ref.get();
                if (tracker == null) {
                    continue;
                }
                tracker.constructThreads.read(ct -> {
                    Map<Thread, InstancesTracker.CreateLock>
                            ctCast = (Map<Thread, InstancesTracker.CreateLock>)ct;

                    ctCopy.putAllForward(ctCast);
                    return null;
                });
            }


            Set<Thread> deadlocks = new TreeSet<Thread>((thread1, thread2) -> {
                //Comparator ... sorts threads by priority to interrupt
                if (Objects.equals(thread1, thread2)) return 0;
                InstancesTracker.CreateLock lock1 = null;
                InstancesTracker.CreateLock lock2 = null;

                Set<InstancesTracker.CreateLock> locks1 = ctCopy.get(thread1);
                if (locks1 != null) {
                    for (InstancesTracker.CreateLock lock : locks1) {
                        lock1 = lock;
                        break;
                    }
                }

                Set<InstancesTracker.CreateLock> locks2 = ctCopy.get(thread2);
                if (locks2 != null) {
                    for (InstancesTracker.CreateLock lock : locks2) {
                        lock2 = lock;
                        break;
                    }
                }

                if (lock1 != null && lock2 != null) {
                    return lock1.compareTo(lock2);
                }

                if (lock1 == null) {
                    lock1 = wtCopy.get(thread1);
                }

                if (lock2 != null) {
                    lock2 = wtCopy.get(thread2);
                }

                if (lock1 != null && lock2 != null) {
                    return lock1.compareTo(lock2);

                } else if (lock2 != null) {
                    return -1;

                } else if (lock1 != null) {
                    return 1;
                }

                return thread1.getId() < thread2.getId() ? -1 : 1;
            });

            RecursiveMap<Thread> threads = getThreadBlockingMapWithLock(
                    ReadWriteLock.getThreadBlockingMap(), wtCopy);

            deadlocks.addAll(threads.getCircularKeys());



                for (Thread thread : deadlocks) {
                    InstancesTracker.CreateLock origLock = wtCopy.get(thread);
                    if (origLock == null) {
                        continue;
                    }

                    if (waitingThreads.write(Boolean.class, wt -> {
                        InstancesTracker.CreateLock lock = wt.get(thread);
                        if (lock == null || lock != origLock) return false;

                        logger.debug("INTERRUPTING WAITING THREAD, TO BREAK A DEADLOCK:" + thread + "\n"
                                + StackTracePrinter.makeStackTraceString(thread));

                        wt.remove(lock);
                        thread.interrupt();
                        return true;
                    })) {
                        break;
                    }
                }


        } catch (Exception e) {
            logger.error(e);
        }
    });

    static {
        deadlockBreaker.setName("InstancesTracker.deadlockBreaker");
        deadlockBreaker.setDaemon(true);
        deadlockBreaker.start();
    }


    private final TrackerNeedsFinalizing comparable = new TrackerNeedsFinalizing(this);

    private final static ReadWriteLock<Map<TrackerNeedsFinalizing, Object>, List<TrackerNeedsFinalizing>>
            finalizerMonitor = ReadWriteLock.forMap(new WeakHashMap<>());

    private void notifyFinalizer() {
        finalizerMonitor.write(comparables -> {
            synchronized (this.comparable) {
                this.comparable.notificationCount++;
            }
            comparables.put(this.comparable, UNDER_CONSTRUCTION_PLACEHOLDER);
            return null;
        });

        synchronized (finalizerMonitor) {
            finalizerMonitor.notifyAll();
        }
    }

    private static class FinalizerReport {
        private final int count;
        private final double time;
        private FinalizerReport(int count, long time) {
            this.count = count;
            this.time = time / BILLION;
        }

        public String toString() {
            return "{" + count + ", " + String.format("%.5f", time) + "}";
        }
    }

    public static final double BILLION = 1000000000; //for converting ns to seconds
    public static final long MILLION = 1000000; //for converting between ns and ms
    //private static final int REPORT_ITERATIONS = 1;
    //private static final int RESULT_SAMPLES = 1;

    private static final Thread finalizer = new Thread(() -> {
        int loops = 0;
        Thread finalizer = Thread.currentThread();

        //FinalizerReport[] reports = new FinalizerReport[REPORT_ITERATIONS];
        int index = 0;
        int totalCount = 0;
        long activeTime = 0;
        long startTime = System.nanoTime();

        while (true) {
            try {
                loops++;
                finalizer.setPriority(Thread.MIN_PRIORITY);
                int size = 0;
                while (true) {
                    synchronized (finalizerMonitor) {
                        size = finalizerMonitor.read(Integer.class, comparables -> {
                            int s = 0;
                            for (TrackerNeedsFinalizing comparable : comparables.keySet()) {
                                s += (comparable.countChange = comparable.notificationCount);
                            }
                            return s;
                        });
                        if (size > 0) {
                            break;
                        }
                        finalizerMonitor.wait(60000);
                    }
                }

                long inactiveTime = System.nanoTime() - startTime;
                // Time that will be allocated for finalizing should never exceed inactive time
                if (inactiveTime > BILLION) { // Maximum of one second
                    inactiveTime = (long) BILLION;

                } else if (inactiveTime < BILLION / 8) {
                    //EDIT: but with a minimum of 1/8 of a second;
                    inactiveTime = (long) BILLION / 8;
                }

                Thread.sleep(500);

                runFinalizer(inactiveTime);

            } catch (Exception e) {
                logger.error("Error in finalizer thread");
                logger.error(e);

            } catch (Throwable e) { // most likely ThreadDisruption error from ReadWriteLock
                logger.warn("Non-exception Throwable Error", e);
            }
        }
    });

    public static boolean runFinalizer(long maxTimeNs) {
        List<TrackerNeedsFinalizing> needsFinalizing = getTrackersThatNeedFinalizing();

        if (needsFinalizing.size() <= 0) return false;

        //long loopStart = System.nanoTime();

        Iterator<TrackerNeedsFinalizing> iterator = needsFinalizing.iterator();

        int finalizedCount = 0;
        int remainingTrackers = needsFinalizing.size();
        List<TrackerNeedsFinalizing> addBackToMonitor = new ArrayList<>(needsFinalizing.size());

        long timeoutAt = System.nanoTime() + maxTimeNs;
        List<InstancesTracker> revisit = null;

        while (true) {
            InstancesTracker tracker = null;
            if (iterator.hasNext()) {
                TrackerNeedsFinalizing comparable = iterator.next();
                if (comparable == null //shouldn't happen, but just in case...
                        || (tracker = comparable.ref.get()) == null
                        || tracker.replacedWith != null) {

                    remainingTrackers--;
                    iterator.remove();
                    continue;
                }

                boolean finalizeUnderway = tracker.finalizeUnderway;

                if (!finalizeUnderway) {
                    synchronized (tracker.returnedLocks) {
                        if (!(finalizeUnderway = tracker.finalizeUnderway)) {
                            tracker.finalizeUnderway = true;
                        }
                    }
                }

                if (finalizeUnderway) {
                    if (revisit == null) revisit = new LinkedList<>();
                    revisit.add(tracker);
                    continue;
                }

            } else  {
                if (revisit == null || revisit.size() <= 0) break;

                for (Iterator<InstancesTracker> revisitIterator = revisit.iterator();
                            revisitIterator.hasNext();) {

                    tracker = revisitIterator.next();
                    if (tracker.finalizeUnderway) {
                        tracker = null;
                        continue;
                    }
                    synchronized (tracker.returnedLocks) {
                        if (tracker.finalizeUnderway) {
                            tracker = null;
                            continue;
                        }
                        tracker.finalizeUnderway = true;
                    }
                    revisitIterator.remove();
                    break;
                }
                if (tracker == null) {
                    if (System.nanoTime() >= timeoutAt) return true;
                    synchronized (finalizerMonitor) {
                        long waitMs = (timeoutAt - System.nanoTime()) / 2 / MILLION;
                        if (waitMs < 2) return true;

                        try {
                            finalizerMonitor.wait(waitMs);

                        } catch (InterruptedException e) { }
                    }
                    if (System.nanoTime() >= timeoutAt) return true;
                    continue;
                }
            }

            long time = System.nanoTime();
            if (time >= timeoutAt) return true;
            long trackerTimeoutAt = (timeoutAt - time) / remainingTrackers + time;

            //logger.debug("Finalizing create locks <K, I> : " + tracker.type);
            FinalizeResult result = tracker.finalizeCreateLocks(trackerTimeoutAt);
            /*logger.info(tracker.type + "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
                    + result.count + "finalized  \t "
                    + tracker.comparable.notificationCount + " remaining  \t"
                    + result.instances + " instances");*/

            remainingTrackers--;

            finalizedCount += result.count;
            if (!result.success) {
                addBackToMonitor.add(tracker.comparable);
            }
        }

        //long endTime = System.nanoTime();

        finalizerMonitor.write(comparables -> {
            for (TrackerNeedsFinalizing comparable : addBackToMonitor) {
                comparables.put(comparable, UNDER_CONSTRUCTION_PLACEHOLDER);
            }
            return null;
        });

        return true;


        /*
        reports[index++] = new FinalizerReport(finalizedCount, endTime - loopStart);
        totalCount += finalizedCount;
        activeTime += (endTime - loopStart);

        if (index >= REPORT_ITERATIONS) {
            logger.debug(getFinalizerReportString(
                    reports, totalCount, startTime, activeTime, endTime));

            //reset
            index = 0;
            totalCount = 0;
            activeTime = 0;
            startTime = System.nanoTime();
        }
         */
    }

    private static List<TrackerNeedsFinalizing> getTrackersThatNeedFinalizing() {
        List<TrackerNeedsFinalizing> needsFinalizing = finalizerMonitor.write(comparables -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            List nf = new ArrayList(comparables.keySet());
            comparables.clear();
            return nf;
        });

        if (!Objects.equals(Thread.currentThread(), finalizer)) {
            return needsFinalizing;
        }

        int changedSize = 0;
        for (TrackerNeedsFinalizing comparable : needsFinalizing) {
            comparable.countChange = comparable.notificationCount - comparable.countChange;
            changedSize += comparable.countChange;
        }

        long addToLoopStart = 0;

        if (changedSize > 0) {
            long startSleep = System.nanoTime();
            try {
                Thread.sleep(changedSize);
            } catch (InterruptedException e) { }

            long endSleep = System.nanoTime();
            addToLoopStart = endSleep - startSleep;
        }

        Collections.sort(needsFinalizing);

        return needsFinalizing;
    }


    /*
    private static String getFinalizerReportString(FinalizerReport[] reports,
                                                   int totalCount,
                                                   long startTime,
                                                   long activeTime,
                                                   long endTime) {

        String avgCount = String.format("%.2f", (double)totalCount / REPORT_ITERATIONS);

        String totalActive = String.format("%.4f", activeTime / BILLION);
        String avgActive = String.format("%.6f", activeTime / (REPORT_ITERATIONS * BILLION));

        String totalTime = String.format("%.4f", (endTime - startTime) / BILLION);
        String avgTotal = String.format("%.6f", (endTime - startTime) / (REPORT_ITERATIONS * BILLION));

        String inactiveTime = String.format("%.4f", (endTime - startTime - activeTime) / BILLION);
        String avgInactive = String.format("%.6f", (endTime - startTime - activeTime) / (REPORT_ITERATIONS * BILLION));

        String str = "";
        for (int i = 0; i < RESULT_SAMPLES; i++) {
            int j = (int)Math.round(((double)(REPORT_ITERATIONS - 1) / RESULT_SAMPLES) * i);
            str += reports[j].toString();
        }


        return "\nFINALIZER REPORTS:"
                + "\n     Counts \t total: " + totalCount + " \t avg: " + avgCount
                + "\n  Active Time \t total: " + totalActive  + " \t avg: " + avgActive
                + "\nInactive Time \t total: " + inactiveTime + " \t avg: " + avgInactive
                + "\n   Total Time \t total: " + totalTime + " \t avg: " + avgTotal
                + "\n\n" + str;
    }
     */

    static {
        finalizer.setName("InstancesTracker.finalizer");
        finalizer.setDaemon(true);
        ReadWriteLock.registerAsDisruptable(finalizer);
        finalizer.start();
    }

    private static class FinalizeResult {
        final boolean success;
        final int count;
        final int instances;
        FinalizeResult(boolean success, int count, int instances) {
            this.success = success;
            this.count = count;
            this.instances = instances;
        }
    }

    private boolean finalizeUnderway = false;
    private final Var<List<CreateLock>> returnedLocks = new Var();
    private final Var.Int rlIndex = new Var.Int();

    private FinalizeResult finalizeCreateLocks(long timeoutAt) {
        synchronized (returnedLocks) {
            this.finalizeUnderway = true;
            Var.Int finalizedCount = new Var.Int();
            Var.Int instancesSize = new Var.Int();

            try {
                boolean newList = returnedLocks.value == null;

                while (true) {
                    if (newList) {
                        List<CreateLock> allLocks = underConstruction.read(List.class, uc -> new ArrayList(uc.values()));
                        returnedLocks.value = new ArrayList(allLocks.size());

                        for (CreateLock lock : allLocks) {
                            if (lock.stage.ordinal() >= Stage.RETURNED.ordinal()) {
                                returnedLocks.value.add(lock);
                            }
                        }
                        rlIndex.value = 0;
                    }


                    boolean completedList = this.instances.write(Boolean.class, instances -> {
                        try {
                            return this.underConstruction.write(Boolean.class, underConstruction -> {
                                for (; rlIndex.value < returnedLocks.value.size();
                                     rlIndex.value++) {

                                    CreateLock lock = returnedLocks.value.get(rlIndex.value);

                                    if (lock.finalizeConstruction(instances, underConstruction)) {
                                        finalizedCount.value++;
                                    }
                                    if (System.nanoTime() >= timeoutAt) return false;
                                }
                                return true;
                            });

                        } finally {
                            instancesSize.value = instances.size();
                        }
                    });

                    if (completedList) {
                        returnedLocks.value = null;
                    }

                    if (completedList && !newList) {
                        newList = true;

                    } else {
                        return new FinalizeResult(true, finalizedCount.value, instancesSize.value);
                    }
                }

            } catch (Exception e) {
                logger.error("Error in finalizeCreateLocks for InstancesTracker<"
                        + this.keyType.getSimpleName() + ", " + this.instType.getSimpleName() + ">", e);

                return new FinalizeResult(false, finalizedCount.value, instancesSize.value);

            } finally {
                this.finalizeUnderway = false;
                synchronized (finalizerMonitor) {
                    finalizerMonitor.notify();
                }
            }
        }
    }



    private static int counter = 0;
    private static class TrackerNeedsFinalizing implements Comparable<TrackerNeedsFinalizing> {
        private final int id = counter++;
        private int notificationCount = 0;
        private int countChange = 0;
        private final WeakReference<InstancesTracker> ref;

        private TrackerNeedsFinalizing(InstancesTracker tracker) {
            this.ref = new WeakReference<>(tracker);
        }

        public int compareTo(TrackerNeedsFinalizing other) {
            if (this.countChange == other.countChange) {
                return this.id < other.id ? -1 : 1;

            } else {
                return this.countChange < other.countChange ? -1 : 1; //prioritize trackers which are less active
            }
        }
    }

    private static class DiscontinuedTrackerPlaceholder
            extends KeyValueType<InstancesTracker, InstancesTracker.CreateLock> {

        private DiscontinuedTrackerPlaceholder() {
            super (InstancesTracker.class, InstancesTracker.CreateLock.class);
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
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
    public abstract static class NullInstance extends InstancesTrackerException {
        private final Object key;
        private NullInstance(InstancesTracker tracker, Object key, String modifier) {
            super(tracker, "Null instance " + modifier
                            + " InstancesTracker " + tracker.toString()
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

    public static class NullInstanceReturned extends NullInstance {
        private NullInstanceReturned(InstancesTracker tracker, Object key) {
            super (tracker, key, "returned to");
        }
    }

    public static class NullInstanceArgument extends NullInstance {
        private NullInstanceArgument(InstancesTracker tracker, Object key) {
            super (tracker, key, "passed to");
        }
    }

    public static class NullKeyArgument extends InstancesTrackerException {
        private NullKeyArgument(InstancesTracker tracker) {
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

    public static class CreateLockException extends InstancesTrackerException {
        private final Thread thread = Thread.currentThread();
        private final Object key;

        private CreateLockException(InstancesTracker tracker, Object key) {
            super(tracker, "Cannot call a construct method while already under a create lock.  Use instancesTracker.put instead");
            this.key = key;
        }

        public Thread getThread() {
            return this.thread;
        }

        public Object getKey() {
            return this.key;
        }
    }

    public static class AlreadyFinalized extends InstancesTrackerException {
        private final InstancesTracker.CreateLock lock;

        private AlreadyFinalized(InstancesTracker tracker, InstancesTracker.CreateLock lock) {
            super(tracker);
            this.lock = lock;
        }
    }

    public static class TrackerDiscontinuedException extends InstancesTrackerException {
        InstancesTracker newTracker;

        private TrackerDiscontinuedException(InstancesTracker tracker) {
            super(tracker);
            tracker = tracker.replacedWith;

            while (true) {
                if (tracker.replacedWith == null) {
                    break;
                }
            }
            this.newTracker = tracker;
        }

        public InstancesTracker getNewTracker() {
            return this.newTracker;
        }
    }
}