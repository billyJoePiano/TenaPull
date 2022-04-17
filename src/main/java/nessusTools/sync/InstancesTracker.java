package nessusTools.sync;

import nessusTools.util.*;
import org.apache.logging.log4j.*;

import javax.validation.constraints.*;
import java.lang.ref.*;
import java.util.*;

/*
 * TODO see other TODOs , also triple-check logic behind createLock.waitForStage and checkForDeadlock
 * especially as it relates to knowing which threads are running with which locks and checking
 * against which are waiting for read/write locks on instance/underConstruction, etc to determine
 * if deadlock conditions have been reached
 */

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
 *      EDIT: 4/13 removed this requirement to allow searching while holding a CreateLock in ObjectLookupDao
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

    private static final WeakReference UNDER_CONSTRUCTION_PLACEHOLDER = new WeakReference(null);

    private final Class<K> keyType;
    private final Class<I> instType;
    private final Lambda1<K, I> construct;

    private final ReadWriteLock<Map<I, Set<K>>, Object> instances
            = ReadWriteLock.forMap(new WeakHashMap());

    private final ReadWriteLock<Map<K, CreateLock>, CreateLock> underConstruction
            = ReadWriteLock.forMap(new LinkedHashMap());

    private final ReadWriteLock<Map<Thread, CreateLock>, CreateLock> threadLocks
            = ReadWriteLock.forMap(new WeakHashMap<>());

    //lazily constructed unmodifiable views of the keys for each instance
    private final ReadWriteLock<Map<I, Set<K>>, Set<K>> keySetViews
            = ReadWriteLock.forMap(new WeakHashMap());


    private final KeySet keySet = new KeySet();

    private final boolean keysComparable;

    public InstancesTracker(Class<K> keyType,
                            Class<I> instancesType,
                            Lambda1<K, I> constructionLambda) {

        this.keyType = keyType;
        this.instType = instancesType;
        this.construct = constructionLambda;

        this.keysComparable = Comparable.class.isAssignableFrom(keyType);
    }


    public Set<K> keySet() {
        return this.keySet;
    }

    public int size() {
        return instances.read(Integer.class, instances ->
            underConstruction.read(Integer.class, underConstruction -> {
                Set<I> counter = new LinkedHashSet<>(instances.keySet());
                for (CreateLock lock : underConstruction.values()) {
                    I instance = lock.run();
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

        return lock.run();
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
                for (CreateLock lock : underConstruction.values()) {
                    I instance = lock.run();
                    if (accepted.contains(instance) || rejected.contains(instance)){
                        continue;
                    }
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
                return lock.run();

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
            for (CreateLock lock : underConstruction.values()) {
                if (Objects.equals(instance, lock.run())) {
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
                for (CreateLock lock : underConstruction.values()) {
                    I instance = lock.run();
                    if (instance != null) {
                        all.add(instance);
                    }
                }
                return null;
            });

            return all;
        });
    }

    // TODO a remove(K key) method to remove a mapping

    public I put(K key, I instance)
            throws UnrecognizedInstance {

        if (key == null) {
            throw new NullKeyArgument(this);

        } else if (instance == null) {
            throw new NullInstanceArgument(this, key);
        }

        CreateLock myLock = this.getCurrentThreadLock();

        return instances.write(instType, instances -> {
            if (instances.containsKey(instance)) {
                return putWithLock(key, instance, instances);
            }

            return underConstruction.write(instType, underConstruction -> {
                CreateLock otherLock = underConstruction.get(key);

                if (myLock == null) {
                    // this invocation is NOT coming from within a createLock construct lambda
                    for (CreateLock l : underConstruction.values()) {
                        if (otherLock.run() == instance) {
                            return putWithLock(key, instance, instances);
                        }
                    }

                    throw new UnrecognizedInstance(this, instance);
                }


                // ... else this invocation IS coming from within a createLock construct lambda
                if (myLock.override && otherLock == null) {
                    /**
                     *  put directly into the instances map only when this is
                     *  an overriding lock and there is no create lock for
                     *  this key.  Non-overriding locks should provide an
                     *  opportunity to be overridden by other threads before
                     *  their construction is finalized
                     */

                    return putWithLock(key, instance, instances);
                }

                this.keySet.keysMap.write(map -> map.put(key, UNDER_CONSTRUCTION_PLACEHOLDER));
                // ... else put in underConstruction queue ...
                CreateLock altLock
                        = new CreateLock(key, null, myLock.override);

                // don't need to provide construct lambda, because we will just take a "shortcut"...
                altLock.finishedInstance = instance;
                altLock.stage = Stage.RETURNED;

                if (otherLock == null) {
                    // If this block is reached, myLock.override must be false
                    altLock.startOfChain = true;
                    underConstruction.put(key, altLock);
                    return null;

                } else if (otherLock.override && !myLock.override) {
                    //need to splice in altLock as the new 'startOfChain'

                    synchronized (otherLock.checkpointLock) {
                        altLock.startOfChain = true;
                        otherLock.startOfChain = false;

                        if (key != otherLock.key) {
                            underConstruction.remove(otherLock.key);
                            // remove old key to avoid any issues with the key mutating and .equals() behavior
                        }

                        underConstruction.put(key, altLock);
                    }

                    return altLock.overrideWith(otherLock, false);

                }

                return otherLock.overrideWith(altLock, false);
            });
        });
    }

    private CreateLock getCurrentThreadLock() {
        return this.threadLocks.read(threadLocks -> threadLocks.get(Thread.currentThread()));
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

        if (lock != null) {
            throw new CreateLockException(this, lock.key);
        }

        if (overwriteIfFound) {
            return this.overwrite(key, construct);
        } else {
            return this.notOverwrite(key, construct);
        }
    }

    private I notOverwrite(K key, Lambda1<K, I> construct) {
        Var.Bool isNewLock = new Var.Bool(); // indicates whether the startOfChain is a newly create lock (on the first iteration only)
        Var<CreateLock> startOfChain = new Var();
        Var<CreateLock> backupPlan = new Var();
        I instance;

        // two iterations ... if the first time failed to produce an instances AND a new lock was not created
        // from the given construct lambda, then we attempt a second time, using the construct lambda as 'plan b'
        while (true) {
            instance = this.instances.read(this.instType, instances -> {
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

                underConstruction.write(underConstruction -> {
                    CreateLock l = underConstruction.get(key);
                    if (l == null) {
                        l = new CreateLock(key, construct, false);
                        l.startOfChain = true;

                        if (startOfChain.value == null) {
                            startOfChain.value = l;
                            isNewLock.value = true;

                        } else {
                            // means the old startOfChain was removed and returned null, thus the key is not in instances?
                            startOfChain.value = l;
                            backupPlan.value = l;
                        }

                        underConstruction.put(key, l);

                    } else if (startOfChain.value != null) {
                        backupPlan.value = new CreateLock(key, construct, false);
                        l.overrideWith(backupPlan.value, true);

                    } else {
                        startOfChain.value = l;
                    }
                    return null;
                });
                return null;
            });

            if (backupPlan.value == null) {
                synchronized (startOfChain.value) {
                    instance = startOfChain.value.run();
                    if (instance != null || isNewLock.value) {
                        return instance;
                    }
                }

            } else {
                synchronized (backupPlan.value) {
                    return startOfChain.value.run();
                }
            }
        }
    }

    private I overwrite(K key,
                      Lambda1<K, I> construct) {

        Var<CreateLock> startOfChain = new Var();
        CreateLock overridingLock = new CreateLock(key, construct, true);

        this.instances.write(instances -> {
            return underConstruction.write(underConstruction -> {
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

                CreateLock l = underConstruction.get(key);

                if (l == null) {
                    overridingLock.startOfChain = true;
                    underConstruction.put(key, overridingLock);

                } else {
                    startOfChain.value = l;
                    underConstruction.remove(key);
                    underConstruction.put(key, l);
                    l.overrideWith(overridingLock, true);
                }
                return null;
            });
        });


        if (startOfChain.value != null) {
            synchronized (overridingLock) {
                overridingLock.run();
            }
            synchronized (startOfChain.value) {
                return startOfChain.value.run();
            }

        } else {
            synchronized (overridingLock) {
                return overridingLock.run();
            }
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

    //used by createLock.waitForStage(stage)
    //private static int TIMEOUT_NO_LOCK = 0;
    //private static int TIMEOUT_WITH_LOCK = 10;

    private class CreateLock {
        private boolean startOfChain = false;

        private final K key;
        private final Lambda1<K, I> construct;
        private final boolean override;

        private I finishedInstance = null;
        //private Boolean failedConstruction = null;
        //private Throwable exception = null;
        private CreateLock overriddenBy = null;
        private Thread runThread = null;
        //private Thread overriddingThread = null;


        /**
         * For other locks to check on the stage of construction of this lock.
         *
         * The critical different between checkpointLock and the CreateLock itself is that:
         * 1) A CreateLock *must* be locked from OUTSIDE of a lock on the outer 'instances' and
         *      'underConstruction' maps, though it may grab these locks from WITHIN its lock, via
         *      the construct lambda calling instancesTracker.get/put() or similar operations.
         *      Conversely, the checkpoint lock CAN be grabbed from within a lock on instances,
         *      underConstruction or another createLock.
         *
         * 2) A thread holding a createLock cannot grab a lock on another createLock as this could
         *      cause deadlock.  It can however grab the checkpointLock of another createLock to check
         *      on its stage of construction.
         *
         * Note that multiple checkpoint locks should NOT be grabbed at once, or it
         * may cause deadlock.  Even when recursing through a chain of "overriddenBy", a createLock
         * further down the chain may be running construct() on a different thread which may attempt
         * an instancesTracker.get/put() type of operation, which would then try grabbing the
         * checkpoint lock higher up the same chain... thus leading to deadlock between the two
         * threads.
         */
        private final Object checkpointLock = new Object();
        private Stage stage = Stage.IDLE;




        private CreateLock(final K forKey,
                            final Lambda1<K, I> construct,
                            final boolean override) {
            this.key = forKey;
            this.construct = construct;
            this.override = override;
        }

        /**
         *  Should only be called with a lock on 'this' (createLock), OR from outside
         *  the scope of any createLock AFTER the primary run() thread has already been
         *  invoked/finished from its primary run thread.  A lock on instances and
         *  underConstruction is not needed.
          */
        private I run() throws CreateLockException {
            boolean alreadyRan = true;
            boolean waitForPostConstruct = false;

            //check stage
            synchronized (this.checkpointLock) {
                if (this.stage == Stage.IDLE) {
                    if (Thread.holdsLock(this)) {
                        this.stage = Stage.PRE_CONSTRUCT;
                        alreadyRan = false;

                        this.runThread = Thread.currentThread();
                        InstancesTracker.this.threadLocks.write(tl -> tl.put(this.runThread, this));

                        this.checkpointLock.notifyAll();

                    } else { // IDLE but not the thread holding the createLock
                        waitForPostConstruct = true;
                        if (this.override || this.overriddenBy == null) {
                            try {
                                this.checkpointLock.wait(500);

                            } catch (InterruptedException e) {
                                CreateLockException cle
                                        = new CreateLockException(InstancesTracker.this, this.key);
                                cle.addSuppressed(e);
                                throw cle;
                            }

                            if (this.stage == Stage.IDLE) {
                                throw new CreateLockException(InstancesTracker.this, this.key);
                            }
                        }
                    }

                } else { //not in the idle stage
                    waitForPostConstruct = this.stage.ordinal() < Stage.POST_CONSTRUCT.ordinal();
                }
            }

            if (alreadyRan) {
                if (waitForPostConstruct) {
                    if (!this.waitForStage(Stage.POST_CONSTRUCT)) {
                        return this.finishedInstance; //almost certain this will be null?
                    }
                }

                synchronized (this.checkpointLock) {
                    if (this.overriddenBy == null) {
                        return this.finishedInstance;
                    }
                }

                return this.overriddenBy.run();
            }

            boolean callConstruct;
            synchronized (this.checkpointLock) {
                this.stage = Stage.CONSTRUCT;
                callConstruct = this.override || this.overriddenBy == null;
            }

            try {
                if (callConstruct || this.overriddenBy.run() == null) {
                    try {
                        this.finishedInstance = construct.call(this.key);

                    } catch (Throwable e) {
                        logger.error("Error while constructing instance of type "
                                + InstancesTracker.this.instType);
                        logger.error(e);
                        throw e;
                    }
                }

                synchronized (this.checkpointLock) {
                    //this.failedConstruction = this.finishedInstance == null;
                    this.stage = Stage.POST_CONSTRUCT;
                    this.checkpointLock.notifyAll();
                }

                return this.run();

            } finally {
                synchronized (this.checkpointLock) {
                    InstancesTracker.this.threadLocks.write(tl -> tl.remove(this.runThread));
                    this.runThread = null;
                    this.stage = Stage.RETURNED;
                    this.checkpointLock.notifyAll();
                    this.notifyAll();
                }
                notifyFinalizer();
            }
        }


        /**
         * Waits for a lock to reach the given stage, but checks for deadlock
         * cycles.  If the current thread is found to be part of such a deadlock
         * cycle, it will return *without* waiting for the given stage.
         *
         * IMPORTANT: Do NOT call with any checkpoint locks.
         */
        private boolean waitForStage(Stage stage) {
            assert !Thread.holdsLock(this.checkpointLock);

            Thread currentThread = Thread.currentThread();
            CreateLock currentLock
                    = InstancesTracker.this.threadLocks.read(tl -> tl.get(currentThread));

            synchronized (this) {
                if (this.stage.ordinal() >= stage.ordinal()) {
                    return true;

                } else if (this.runThread != null
                        && Objects.equals(this.runThread, currentThread)) {

                    return false;
                }
            }


            if (currentLock != null) {
                assert Thread.holdsLock(currentLock);

                synchronized (currentLock.checkpointLock) {
                    assert currentLock.stage == Stage.CONSTRUCT;
                    // || currentLock.stage == Stage.FINALIZING;//???

                    assert Objects.equals(currentLock.runThread, currentThread);
                    if (currentLock == this) {

                        return this.stage.ordinal() >= stage.ordinal();
                    }

                }
            }

            while (true) {
                Set<Thread> runThreads = new LinkedHashSet<>();
                CreateLock next;

                synchronized (this) {
                    if (this.stage.ordinal() >= stage.ordinal()) {
                        return true;

                    } else if (this.runThread != null
                            && Objects.equals(this.runThread, currentThread)) {

                        return false;
                    }
                    next = this.overriddenBy;
                }

                while (next != null) {
                    synchronized (next.checkpointLock) {
                        if (next.runThread != null) {
                            runThreads.add(this.runThread);
                        }
                        next = next.overriddenBy;
                    }
                }

                RecursiveMap<Thread> blocks = ReadWriteLock.getThreadBlockingMap();
                Set<Thread> circular = blocks.getCircularKeys();
                if (circular.contains(currentThread)) {
                    return this.stage.ordinal() >= stage.ordinal();
                }

                for (Thread thread : runThreads) {
                    if (circular.contains(thread)
                            || blocks.containsDownstream(currentThread, thread)) {

                        return this.stage.ordinal() >= stage.ordinal();
                    }
                }

                try {
                    synchronized (this.checkpointLock) {
                        if (this.stage.ordinal() >= stage.ordinal()) {
                            return true;
                        }

                        this.checkpointLock.wait(500);

                        if (this.stage.ordinal() >= stage.ordinal()) {
                            return true;
                        }
                    }

                } catch (InterruptedException e) {
                    logger.error(e);
                    synchronized (this.checkpointLock) {
                        return this.stage.ordinal() >= stage.ordinal();
                    }
                }
            }
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

        private I overrideWith(CreateLock otherLock, boolean skipRun) {
            //typically this would be called from a different thread than the thread that created the lock
            assert !(otherLock == this
                    || otherLock.startOfChain
                    || (this.override && !otherLock.override));


            boolean origSkipRun = skipRun;
            // ^^^ to pass on the original argument recursively, since skipRun may change below

            synchronized (this.checkpointLock) {
                if (this.overriddenBy != null) {
                    if (!this.override) {
                        skipRun = true; //effectively a "delegateRun" IF origSkipRun is false
                                        // ... delegate to the overridding create lock

                        if (this.overriddenBy.override && !otherLock.override) {

                            // splice otherLock in between, and use the current
                            // overriddenBy lock in place of "otherLock"
                            CreateLock overridding = this.overriddenBy;
                            this.overriddenBy = otherLock;
                            otherLock = overridding;
                        }
                    }

                } else if (this.stage.ordinal() >= Stage.POST_CONSTRUCT.ordinal() //already constructed
                            || skipRun                              // hasn't started yet, but this thread indicates it will start after returning
                            || (this.runThread != null              // is the current thread
                                && Objects.equals(this.runThread, // ...meaning this is happening due to an invocation from within a construct lambda
                                                Thread.currentThread()))) {

                        this.overriddenBy = otherLock;
                        return this.finishedInstance; //the displaced instance
                }
            }

            I displaced;

            if (skipRun) {
                displaced = this.finishedInstance;

            } else {
                displaced = this.run();
            }

            synchronized (this.checkpointLock) {
                if (this.overriddenBy == null) {
                    this.overriddenBy = otherLock;
                    if (displaced != null) {
                        return displaced;

                    } else {
                        return this.finishedInstance;
                    }

                }
            }

            I priorityDisplaced = this.overriddenBy.overrideWith(otherLock, origSkipRun);
            if (priorityDisplaced != null) {
                return priorityDisplaced;

            } else if (displaced != null) {
                return displaced;

            } else {
                synchronized (this.checkpointLock) {
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
                instance = this.run();

            } catch (Throwable e) {
                logger.error(e);
                clearRunThread(current);
                return false;
            }

            synchronized (this.checkpointLock) {
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

            } else if (instance == null) {
                // remove from keySet if necessary
                InstancesTracker.this.keySet.keysMap.write(map -> {
                    WeakReference<I> ref = map.get(this.key);
                    if (ref == UNDER_CONSTRUCTION_PLACEHOLDER
                            || ref == null
                            || ref.get() == null) {

                        map.remove(this.key);
                    }
                    return null;
                });
            }

            synchronized (InstancesTracker.this.comparable) {
                InstancesTracker.this.comparable.notificationCount -= count;
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
            synchronized (this.checkpointLock) {
                wait = this.stage.ordinal() < Stage.RETURNED.ordinal();
                if (!wait) {
                    if (this.stage == Stage.RETURNED) {
                        this.stage = Stage.FINALIZING;
                        this.checkpointLock.notifyAll();
                    }
                    overriddenBy = this.overriddenBy;
                }
            }

            if (wait) {
                this.waitForStage(Stage.RETURNED);
                boolean success;
                synchronized (this.checkpointLock) {
                    success = this.stage.ordinal() >= Stage.RETURNED.ordinal();
                }
                if (success) {
                    return this.markAsFinalizing(currentThread);
                } else {
                    if (overriddenBy != null) {
                        overriddenBy.clearRunThread(currentThread);
                    }
                    this.clearRunThread(currentThread);
                    return false;
                }
            }

            while (true) {
                if (overriddenBy != null && !overriddenBy.markAsFinalizing(currentThread)) {
                    overriddenBy.clearRunThread(currentThread);
                    this.clearRunThread(currentThread);
                    return false;
                }

                synchronized (this.checkpointLock) {
                    if (this.overriddenBy == overriddenBy) {
                        this.runThread = currentThread;
                        return true;
                    }
                }
                overriddenBy.clearRunThread(currentThread);
                synchronized (this.checkpointLock) {
                    overriddenBy = this.overriddenBy;
                }
            }
        }

        private void clearRunThread(Thread currentThread) {
            CreateLock overriddenBy;
            synchronized (this.checkpointLock) {
                if (this.runThread == currentThread) {
                    //use instance equality to guarantee it came from this call to finalizeConstruction()
                    this.runThread = null;
                }
                overriddenBy = this.overriddenBy;
            }

            while (overriddenBy != null) {
                overriddenBy.clearRunThread(currentThread);
                synchronized (this.checkpointLock) {
                    if (overriddenBy == this.overriddenBy) {
                        return;
                    }
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

            synchronized (this.checkpointLock) {
                if (this.stage == Stage.FINALIZING
                        && this.finishedInstance != null
                        && this.finishedInstance != instanceWithKey
                        && !instances.containsKey(this.finishedInstance)) {

                    instances.put(this.finishedInstance,
                            InstancesTracker.this.makeKeySet());
                }
                overriddenBy = this.overriddenBy;
            }

            while (overriddenBy != null) {
                overriddenBy.checkForInstancesWithoutKey(instances, instanceWithKey);

                // check for (unlikely but possible) situation where another thread
                // spliced a new non-overriding createLock in between the old overriddenBy
                // createLock and this lock
                synchronized (this.checkpointLock) {
                    if (overriddenBy != this.overriddenBy) {
                        overriddenBy = this.overriddenBy;

                    } else {
                        overriddenBy = null;
                    }
                }
            }
        }

        private int markAsFinalized(Thread currentThread) {
            boolean wait;
            CreateLock overriddenBy = null;
            synchronized (this.checkpointLock) {
                wait = this.stage.ordinal() < Stage.FINALIZING.ordinal();
                if (!wait) {
                    overriddenBy = this.overriddenBy;
                    if (this.stage != Stage.FINALIZED) {
                        this.stage = Stage.FINALIZED;
                        this.checkpointLock.notifyAll();
                    }
                }
            }

            if (wait) {
                if (!this.markAsFinalizing(currentThread)) {
                    return -1;
                }
                synchronized (this.checkpointLock) {
                    if (this.stage.ordinal() < Stage.FINALIZING.ordinal()) {
                        return -1;
                    }
                }

                return this.markAsFinalized(currentThread);
            }

            while (true) {
                int count = 0;
                if (overriddenBy != null) {
                    count = overriddenBy.markAsFinalized(currentThread);
                    if (count == -1) {
                        return -1;
                    }
                }
                synchronized (this.checkpointLock) {
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

    /*
    //tracker of trackers
    private static ReadWriteLock<Map<InstancesTracker, Void>, Map<InstancesTracker, Void>>
            trackers = ReadWriteLock.forMap(new WeakHashMap<>());
     */
    private final TrackerComparable comparable = new TrackerComparable(this);

    private static ReadWriteLock<Map<TrackerComparable, Object>, List<TrackerComparable>>
            finalizerMonitor = ReadWriteLock.forMap(new WeakHashMap<>());

    private void notifyFinalizer() {
        finalizerMonitor.write(comparables -> {
            synchronized (this.comparable) {
                this.comparable.notificationCount++;
            }
            boolean justAdded =
                    comparables.put(this.comparable, UNDER_CONSTRUCTION_PLACEHOLDER) == null;

            if (justAdded) {
                synchronized (finalizerMonitor) {
                    finalizerMonitor.notifyAll();
                }
            }
            return null;
        });
    }

    private static final Thread finalizer = new Thread(() -> {
        int loops = 0;
        Thread finalizer = Thread.currentThread();
        while (true) {
            try {
                loops++;
                List<TrackerComparable> needsFinalizing;
                while(true) {
                    finalizer.setPriority(Thread.MIN_PRIORITY);

                    needsFinalizing = finalizerMonitor.write(comparables -> {
                            finalizer.setPriority(Thread.MAX_PRIORITY);

                            List nf = new ArrayList(comparables.keySet()); //automatically sorts
                            Collections.sort(nf);
                            comparables.clear();
                            return nf;
                        });

                    synchronized (finalizerMonitor) {
                        finalizer.setPriority(Thread.MIN_PRIORITY);

                        if (needsFinalizing.size() > 0) {
                            break;
                        }
                        finalizerMonitor.wait(600000); //double-check every 10 minutes if not woken up
                    }
                }

                long start = System.nanoTime();
                Iterator<TrackerComparable> iterator = needsFinalizing.iterator();
                while(iterator.hasNext()) {
                    TrackerComparable comparable = iterator.next();
                    if (comparable == null) { //shouldn't happen, but just in case...
                        continue;
                    }
                    InstancesTracker tracker = comparable.ref.get();
                    if (tracker == null) {
                        iterator.remove();
                        continue;
                    }
                    if (!tracker.finalizeCreateLocks()) {
                        finalizerMonitor.write(comparables -> {
                            comparables.put(comparable, UNDER_CONSTRUCTION_PLACEHOLDER);
                            return null;
                        });
                    }
                }

                double time = (double)(System.nanoTime() - start) / 1000000000;

                logger.info("InstanceTracker FINALIZING THREAD"
                        + "\nFinalizing time: " + time
                        + "\nTrackers: " + needsFinalizing.size()
                        + "\nLoop count: " + loops);


            } catch (Throwable e) {
                logger.error("Error in finalizer thread");
                logger.error(e);
            }
        }
    });

    static {
        finalizer.run();
    }

    private boolean finalizeCreateLocks() {
        Thread current = Thread.currentThread();
        int oldPriority = current.getPriority();

        try {
            current.setPriority(Thread.MIN_PRIORITY);
            Set<CreateLock> finishedLocks = new LinkedHashSet<>();

            this.underConstruction.read(Void.class, underConstruction -> {
                current.setPriority(Thread.MAX_PRIORITY);
                for (CreateLock lock : underConstruction.values()) {
                    if (lock.stage.ordinal() >= Stage.POST_CONSTRUCT.ordinal()) {
                        finishedLocks.add(lock);
                    }
                }
                return null;
            });

            current.setPriority(Thread.MIN_PRIORITY);
            if (finishedLocks.size() <= 0) {
                return true;
            }

            return this.instances.write(Boolean.class, instances -> {
                current.setPriority(Thread.MAX_PRIORITY);
                return this.underConstruction.write(Boolean.class, underConstruction -> {
                    boolean failed = false;
                    for (CreateLock lock : finishedLocks) {
                        synchronized (lock.checkpointLock) {
                            if (lock.stage != Stage.RETURNED) {
                                continue;
                            }
                        }

                        if (!lock.finalizeConstruction(instances, underConstruction)) {
                            logger.error("Error while finalizing:\t" + lock);
                            return false;
                        }
                    }
                    return true;
                });
            });

        } catch (Throwable e) {
            logger.error("Error in finalizeCreateLocks for InstancesTracker<"
                    + this.keyType + ", " + this.instType + ">");
            logger.error(e);
            return false;

        } finally {
            current.setPriority(oldPriority);
        }
    }



    private static int counter = 0;
    private static class TrackerComparable implements Comparable<TrackerComparable> {
        private final int id = counter++;
        private int notificationCount = 0;
        private final WeakReference<InstancesTracker> ref;

        private TrackerComparable(InstancesTracker tracker) {
            this.ref = new WeakReference<>(tracker);
        }

        public int compareTo(TrackerComparable other) {
            int myCount;
            int otherCount;

            synchronized (this) {
                myCount = this.notificationCount;
            }

            synchronized (other) {
                otherCount = other.notificationCount;
            }

            if (myCount == otherCount) {
                return this.id < other.id ? -1 : 1;

            } else {
                return myCount > otherCount ? -1 : 1;
            }
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
}
