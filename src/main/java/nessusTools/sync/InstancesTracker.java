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

    // eagerly contructed (using Set.union(set1, set2)) unmodifiable view of the entire set of keys
    private final ReadWriteLock<Set<K>, Set<K>> keySet;
    private final Set<K> keySetImmutable;

    private final boolean keysComparable;

    public InstancesTracker(Class<K> keyType,
                            Class<I> instancesType,
                            Lambda1<K, I> constructionLambda) {

        this.keyType = keyType;
        this.instType = instancesType;
        this.construct = constructionLambda;

        this.keysComparable = Comparable.class.isAssignableFrom(keyType);

        if (this.keysComparable) {
            this.keySet = ReadWriteLock.forSet(new TreeSet());
        } else {
            this.keySet = ReadWriteLock.forSet(new LinkedHashSet());
        }

        this.keySetImmutable = keySet.read(keySet -> keySet);

        trackers.write(Void.class, trackers -> trackers.put(this, null));
    }


    public Set<K> keySet() {
        return this.keySetImmutable;
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
        synchronized (lock) {
            return lock.finishedInstance;
        }

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
                CreateLock lock = underConstruction.get(key);

                if (myLock != null) {
                    if (myLock.override) {
                        if (lock == null) {
                            return putWithLock(key, instance, instances);

                        } else {
                            // if both locks override, then it's just a race... *last* one wins
                            CreateLock altLock = new CreateLock(key, k -> instance, false);
                            return lock.overrideInstance(altLock);
                        }

                    } else if (lock == null) {
                        // put in underConstruction queue so it can still be overridden ...
                        CreateLock altLock = new CreateLock(key, k -> instance, false);
                        altLock.startOfChain = true;
                        underConstruction.put(key, altLock);
                        return null;

                    } else if (lock.override) {
                        //we will return the instance provided, even though this isn't actually displacing it
                        return lock.run();

                    } else {
                        CreateLock altLock = new CreateLock(key, k -> instance, false);
                        return lock.overrideInstance(altLock);
                    }
                }

                // ...else, this invocation is NOT coming from within a createLock
                // so we can grab all the create locks

                for (CreateLock l : underConstruction.values()) {
                    synchronized (lock) {
                        if (lock.run() == instance) {
                            return putWithLock(key, instance, instances);
                        }
                    }
                }

                throw new UnrecognizedInstance(this, instance);

            });
        });
    }

    private CreateLock getCurrentThreadLock() {
        return this.threadLocks.read(threadLocks -> threadLocks.get(Thread.currentThread()));
    }



    /**
     * Private method only intended to be called with the following write locks
     * 3) the CreateLock made for this key/thread
     * 1) the instances map, which is then passed to this method.
     * 2) the underConstruction map
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

        if (this.keySet.write(Boolean.class, ks -> ks.add(key))) {
            if (displacedInstance != null) {
                logger.warn(
                        "Unexpected lack of key in master keySet when a displaced instance was found: "
                        + key.toString());
            }
        }
        // master keyset may have outdated entries due to garbage collection of instances!!

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

        // Executor may be called with read lock or write lock, depending
        // on the value of overwriteIfFound
        Lambda1<Map<I, Set<K>>, Object> executor = (instances) -> {

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

            return underConstruction.write(underConstruction -> {
                CreateLock l = underConstruction.get(key);
                if (l == null) {
                    l = new CreateLock(key, construct, overwriteIfFound);
                    l.startOfChain = true;
                    underConstruction.put(key, l);
                }
                return l;
            });
        };

        Object iol = overwriteIfFound ?
                        instances.write(executor) :
                        instances.read(executor);



        if (!overwriteIfFound && this.instType.isInstance(iol)) {
            return (I) iol;
        }
        lock = (CreateLock) iol;

        synchronized (lock) {

            I instance = lock.run();
            if (instance == null) {
                throw new NullInstanceReturned(this, key);
            }
            return instance;
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
        RETURNED,
        FINALIZING,
        FINALIZED
    }

    private class CreateLock {
        private boolean startOfChain = false;

        private final K key;
        private final Lambda1<K, I> construct;
        private final boolean override;

        private I finishedInstance = null;
        private Boolean failedConstruction = null;
        private Throwable exception = null;
        private CreateLock overriddenBy = null;
        private Thread runThread = null;
        private Thread overriddingThread = null;

        /** for other locks to check on the stage of construction
         * The critical different between checkpointLock and the CreateLock itself is that
         * a CreateLock *must* be locked from OUTSIDE of the write lock on instances and
         * underConstruction maps, and cannot lock other createLocks as this would cause deadlock.
         *
         *
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
         *  Only call with a lock on 'this' (createLock).  instances and underConstruction lock not needed
          */
        private I run() throws CreateLockException {
            if (this.stage.ordinal() > Stage.CONSTRUCT.ordinal()) {
                if (this.overriddenBy != null) {
                    I instance = this.overriddenBy.run();
                    if (instance != null) {
                        return instance;
                    }
                }

                return this.finishedInstance;
            }

            boolean waitingForInstance = true;

            synchronized (this.checkpointLock) {
                if (this.stage == Stage.IDLE) {
                    this.stage = Stage.PRE_CONSTRUCT;
                    waitingForInstance = false;

                    this.runThread = Thread.currentThread();
                    InstancesTracker.this.threadLocks.write(tl -> tl.put(this.runThread, this));

                } else {
                    this.waitForInstance();
                }
                this.checkpointLock.notifyAll();
            }

            if (waitingForInstance) {
                return this.run();
            }



            try {
                boolean delegate = true;
                synchronized (this.checkpointLock) {
                    this.stage = Stage.CONSTRUCT;

                    if (this.overriddenBy == null || this.override) {
                        this.finishedInstance = construct.call(this.key);
                        this.failedConstruction = this.finishedInstance == null;

                        if (this.overriddenBy == null) {
                            delegate = false;
                        }
                    }

                }

                if (delegate && this.overriddenBy.run() == null) {
                    synchronized(this.checkpointLock) {
                        this.finishedInstance = construct.call(this.key);
                        this.failedConstruction = this.finishedInstance == null;
                    }
                }


            } catch (Throwable e) {
                this.exception = e;
                this.failedConstruction = true;
                throw e;

            } finally {
                synchronized (this.checkpointLock) {
                    this.stage = Stage.POST_CONSTRUCT;
                    InstancesTracker.this.threadLocks.write(tl -> tl.remove(this.runThread));
                    this.checkpointLock.notifyAll();
                }
            }

            try {
                return this.run();

            } finally {
                synchronized (this.checkpointLock) {
                    this.stage = Stage.RETURNED;
                    this.checkpointLock.notifyAll();
                }
            }
        }

        // ONLY CALL WITH CHECKPOINT LOCK.  Will yield the checkpoint lock using object.wait()
        private void waitForInstance() throws CreateLockException {
            if (this.stage == Stage.CONSTRUCT
                    && this.runThread.equals(Thread.currentThread())) {

                throw new CreateLockException(InstancesTracker.this, this.key);

            }

            //Means construct is still running
            while (this.stage.ordinal() <= Stage.CONSTRUCT.ordinal()) {
                try {
                    this.checkpointLock.wait();

                } catch (InterruptedException e) {
                    logger.error(e);
                    CreateLockException cle = new CreateLockException(InstancesTracker.this, this.key);
                    cle.addSuppressed(e);
                    throw cle;
                }
            }
        }

        /**
         * Synchronizes the moving of a newly constructed instance from the "underConstruction" map to the
         * instances map.  This should only be called while holding locks on 'this' (createLock, instances,
         * and underConstruction)
         */
        private boolean finished(Map<I, Set<K>> instances,
                                Map<K, CreateLock> underConstruction) {

            if (!markAsFinalizing()) {
                return false;
            }

            try {
                if (this.overriddenBy != null) {
                    this.overriddenBy.addInstanceNoKey(instances);
                }

                I instance = this.run();
                InstancesTracker.this.putWithLock(this.key, instance, instances);
                underConstruction.remove(this.key);

            } catch (Throwable e) {
                logger.error(e);
                return false;
            }

            return markAsFinalized();
        }


        //for putting unused instances into the map, but with an empty keySet
        private void addInstanceNoKey(Map<I, Set<K>> instances) {
            if (this.finishedInstance != null
                    && !instances.containsKey(this.finishedInstance)) {

                instances.put(this.finishedInstance, InstancesTracker.this.makeKeySet());
            }

            if (this.overriddenBy != null) {
                this.overriddenBy.addInstanceNoKey(instances);
            }
        }

        private boolean markAsFinalizing() {
            synchronized (this.checkpointLock) {
                if (this.stage.ordinal() < Stage.RETURNED.ordinal()) {
                    return false;
                }

                if (this.stage == Stage.RETURNED) {
                    this.stage = Stage.FINALIZING;
                }

                // if greater than RETURNED.ordinal() then leave
                if (this.overriddenBy != null) {
                    return this.overriddenBy.markAsFinalizing();

                } else {
                    return true;
                }

            }
        }

        private boolean markAsFinalized() {
            synchronized (this.checkpointLock) {
                if (this.stage.ordinal() < Stage.FINALIZING.ordinal()) {
                    return false;
                }

                this.stage = Stage.FINALIZED;

                if (this.overriddenBy != null) {
                    return this.overriddenBy.markAsFinalized();

                } else {
                    return true;
                }
            }
        }


        private I overrideInstance(CreateLock otherLock) {
            //typically this would be called from a different thread than the thread that created the lock
            if (otherLock == this
                    || otherLock.startOfChain
                    || (this.override && !otherLock.override)) {

                throw new CreateLockException(InstancesTracker.this, this.key);
            }

            boolean idle;
            boolean postConstruct;
            boolean overridden;
            boolean currentThread;

            synchronized (this.checkpointLock) {
                idle = this.stage == Stage.IDLE;
                currentThread = this.runThread != null
                                    && this.runThread.equals(Thread.currentThread());
                overridden = this.overriddenBy != null;
                postConstruct = stage.ordinal() >= Stage.POST_CONSTRUCT.ordinal();

                if (overridden) {
                    if (!(this.override || otherLock.override)
                            && this.overriddenBy.override) {

                        // splice otherLock in between, and use the current
                        // overriddenBy lock in place of "otherLock"
                        CreateLock overridding = this.overriddenBy;
                        this.overriddenBy = overridding;
                    }

                } else {
                    if (this.override) {
                        if (postConstruct || currentThread) {
                            this.overriddenBy = otherLock;
                            return this.finishedInstance;
                        }

                    } else {
                        if (idle || postConstruct || currentThread) {
                            this.overriddenBy = otherLock;
                            return this.finishedInstance;
                        }
                    }
                }
            }

            I displaced;

            if (this.override) {
                if (!idle && !postConstruct && currentThread) {
                    if (!overridden) {
                        // compiler says this is unreachable ... leaving in case of refactor
                        synchronized (this.checkpointLock) {
                            if (this.overriddenBy == null) {
                                this.overriddenBy = otherLock;
                                return null;
                            }
                        }
                    }
                    // fall through...

                } else if (idle || overridden || postConstruct) {
                    displaced = this.run();

                    synchronized (checkpointLock) {
                        if (this.overriddenBy == null) {
                            this.overriddenBy = otherLock;
                            if (displaced != null) {
                                return displaced;
                                
                            } else {
                                return this.finishedInstance;
                            }

                        }
                    }
                }

                return this.overriddenBy.overrideInstance(otherLock);

            } else {
                if (!idle && !postConstruct && !currentThread) {
                    displaced = this.run();
                    if (displaced == null) {
                        // probably unnecessary????
                        displaced = this.finishedInstance;
                    }
                    
                } else {
                    displaced = this.finishedInstance; // most likely null???
                }
                
                if (!overridden) {
                    synchronized (this.checkpointLock) {
                        if (this.overriddenBy == null) {
                            this.overriddenBy = otherLock;
                            return displaced;
                        }
                    }
                }

                return this.overriddenBy.overrideInstance(otherLock);
            }
        }

        // end of CreateLock inner class
    }


    //
    private Integer[] finalizeCreateLocks() {
        Set<CreateLock> finishedLocks = new LinkedHashSet<>();

        Integer[] counts = this.underConstruction.read(Integer[].class, underConstruction -> {
            int t = underConstruction.size();
            int c = 0;
            for (CreateLock lock : underConstruction.values()) {
                if (lock.stage.ordinal() >= Stage.POST_CONSTRUCT.ordinal()) {
                    finishedLocks.add(lock);
                    if (++c >= 200) break;
                }
            }
            return new Integer[] { t, c, null };
        });

        if (!(counts[1] > 0)) {
            counts[2] = 0;
            return counts;
        }

        counts[2] = this.instances.write(Integer.class, instances ->
            this.underConstruction.write(Integer.class, underConstruction -> {
                int failed = 0;
                for (CreateLock lock : finishedLocks) {
                    if (lock.stage.ordinal() >= Stage.RETURNED.ordinal()) {
                        if (!lock.finished(instances, underConstruction)) {
                            logger.error(lock);
                            failed++;
                        }
                    } else {
                        failed++;
                    }

                }
                return failed;
            }));
        return counts;
    }

    //tracker of trackers
    private static ReadWriteLock<Map<InstancesTracker, Void>, Map<InstancesTracker, Void>>
            trackers = ReadWriteLock.forMap(new WeakHashMap<>());


    private static Thread finalizer = new Thread(() -> {
        //trackers.constructWith(trackers, k -> trackers);

        int loops = 1;
        while (true) {
            try {
                Thread.yield();

                Map<InstancesTracker, Void>
                        t = trackers.read(trackers -> new WeakHashMap<>(trackers));

                Thread.yield();

                int counts[] = { 0, 0, 0 };

                long start = System.nanoTime();

                for (InstancesTracker tracker : t.keySet()) {
                    Integer[] c = tracker.finalizeCreateLocks();
                    counts[0] += c[0];
                    counts[1] += c[1];
                    counts[2] += c[2];
                    Thread.yield();
                }

                double time = (double)(System.nanoTime() - start) / 1000000000;

                logger.info("Finalizing time: " + time + " \t Trackers: " + t.size()
                        + " \t Counts: [" + counts[0] + ","
                        + counts[1] + "," + counts[2] + "] \t Loop number: "
                        + (loops++));

                // int divisor = 50 + (counts[0] + counts[1] + counts[2]) / 2;

                Thread.sleep(5000);


            } catch (Throwable e) {
                logger.error(e);
            }
        }
    });

    static {
        finalizer.start();
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
