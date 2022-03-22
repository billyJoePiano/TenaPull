package nessusTools.sync;

import java.util.*;

import static nessusTools.sync.CallableWithArg.NothingThrown;

/* Synchronizes and maps unique instances to a unique "key" (typically a String).
 * Instances are held as weak references via a WeakHashMap which is synchronized via ReadWriteLock.
 *
 *
 * NOTE: In the implementation of this class is more like a bi-map, where both keys and values are unique.
 * It is a little counter-intuitive that the instance is the key of the actual WeakHashMap and the key is
 * the value.  But this is needed for the "Weak" reference to work correctly, and allow garbage collection
 * of unused instances.
 *
 * In a sense, both the keys and values are unique "keys" for each other, but only the
 * instance is a weak reference that can be garbage-collected, which is why it must
 * occupy the formal 'key' position in the WeakHashMap.  However, for users of InstancesTracker,
 * the instances are considered to be the "values", and the accessor strong references (typically strings)
 * are considered to be the "keys"
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
    private final ReadWriteLock<Map<I, K>, I, NothingThrown>
            instances = new ReadWriteLock<>(new WeakHashMap<>());

    private ReadWriteLock<Map<K, CreateLock>, CreateLock, NothingThrown>
            underConstruction = new ReadWriteLock(new LinkedHashMap<>());

    private final CallableWithArg<K, I, NothingThrown> construct;
    //private static final CallableWithArg<Object, Object, ReadWriteLock.NothingThrown> doNothing = arg -> null;

    public InstancesTracker(CallableWithArg<K, I, NothingThrown> constructionLambda) {
        this.construct = constructionLambda;
    }

    public I getOrConstruct(K key)
            throws InstancesTrackerException {
        return getOrConstruct(key, this.construct, false);
    }

    public K getKeyFor(I value) {
        K[] placeholder = (K[]) new Object[] { null };
        instances.read(instances -> {
            // may only return type of I from this lambda
            placeholder[0] = instances.get(value);
            return null;
        });
        return placeholder[0];
    }

    // First removes the key if it already exists, and then gains a construct
    // lock with it before running the custom one-time construction lambda
    public I constructWith(K key, CallableWithArg<K, I, NothingThrown> customLambda)
            throws InstancesTrackerException, IllegalArgumentException {
        if (customLambda == null) {
            throw new IllegalArgumentException(
                    "instancesTracker.constructWith(key, callableWithArg) cannot have a null callableWithArg");
        }

        return getOrConstruct(key, customLambda, true);
    }

    public I get(K key) {
        return this.instances.read(instances -> {
            for (Map.Entry<I, K> entry : instances.entrySet()) {
                if (Objects.equals(entry.getValue(), key)) {
                    return entry.getKey();
                }
            }
            return null;
        });
    }

    public I read(K key, CallableWithArg<I, I, NothingThrown> readLambda) {
        return this.instances.read(instances -> {
            return readLambda.run(this.get(key));
        });
    }

    // callback lambda's argument is another lambda to 'put' a key-value pair in the map
    // This put lambda will throw an exception if it is called outside the scope of the write lock.
    // Return value of put is another instance displaced by this put, or null if no matching key was found.
    // If either key or value are null, nothing will happen and null will be returned.
    // The passed writeLambda must return an instance <I> or null.  Typically, this would be the new
    // instance added.
    public Object write(CallableWithArg<CallableWithArg<Map.Entry<K, I>, I, NothingThrown>, I, NothingThrown>
            writeLambda)
        throws NullPointerException {

        return this.instances.write(instances -> {
            Boolean[] unlocked = new Boolean[] { Boolean.FALSE };
            CallableWithArg<Map.Entry<K, I>, I, NothingThrown>
                    put = mapEntry -> {

                // put lambda, passed to the writeLambda
                if (unlocked[0]) {
                    throw new IllegalAccessError(
                            "Cannot call instanceTracker's put(mapEntry<K, V>) lambda after the write lock has been release");
                }

                if (mapEntry == null) {
                    return null;
                }

                I instance = mapEntry.getValue();
                K key = mapEntry.getKey();
                if (instance == null || key == null) {
                    return null;
                }

                for (Map.Entry<I, K> entry : instances.entrySet()) {
                    I otherInstance = entry.getKey();
                    if (instance == otherInstance) {
                        continue;
                        // keep iterating to look for another matching 'key'
                    }

                    K otherKey = entry.getValue();
                    if (Objects.equals(key, otherKey)) {
                        instances.remove(otherInstance);
                        instances.put(instance, key);
                        return otherInstance;
                    }
                }

                instances.put(instance, key);
                return null;
            };

            I returnVal = writeLambda.run(put);
            unlocked[0] = Boolean.TRUE;
            return returnVal;
        });
    }

    private I getOrConstruct(K key,
                             CallableWithArg<K, I, NothingThrown> constructLambda,
                             boolean overwriteIfFound)
            throws InstancesTrackerException {

        if (key == null) {
            throw new NullArgumentAsKey(this);
        }
        Boolean[] newlyConstructed = new Boolean[] { Boolean.FALSE };

        // Executor may be called with read lock or write lock, depending
        // on the value of overwriteIfFound
        CallableWithArg<Map<I, K>, I, NothingThrown> executor;
        executor = instances -> {

            for (Map.Entry<I, K> entry : instances.entrySet()) {
                if (Objects.equals(entry.getValue(), key)) {
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
                if (lock.finishedInstance == null && !lock.failedConstruction) {
                    newlyConstructed[0] = Boolean.TRUE;
                    try {
                        lock.finishedInstance = constructLambda.run(key);

                    } finally {
                        lock.failedConstruction = lock.finishedInstance == null;
                    }
                }
                return lock.finishedInstance;
            }

        };

        I returnVal = null;

        try {
            if (overwriteIfFound) {
                returnVal = instances.write(executor);

            } else {
                returnVal = instances.read(executor);
            }

        } finally {
            if (newlyConstructed[0]) {
                removeFromUnderConstruction(key, returnVal);
            }
        }

        if (returnVal == null) {
            throw new NullReturnFromConstructLambda(this, key);
        }

        return returnVal;
    }

    /*
     * Delegates and synchronizes the moving of a newly constructed instance from the
     * "underConstruction" map to the instances map.  If the instance is null, no lock is needed
     * on the instances map, otherwise it will require a write lock on the instances map.
     * In both cases, a write lock is needed on the "underConstruction" list.
     *
     * To prevent unnecessary blocking of the current thread while waiting for these locks,
     * the execution is delegated to a new thread
     */
    private void removeFromUnderConstruction(K key, I instance) {
        Map<I, K>[] instancesPlaceholder = new Map[] { null }; // instances map will be put here...
        Runnable writeToInstances = () -> instancesPlaceholder[0].put(instance, key);
        //only used if instance != null , but needs to be declared in this scope so it is accessible to
        // removeFromUnderConstruction lambda (below)

        CallableWithArg<Map<K, CreateLock>, CreateLock, NothingThrown>
                removeFromUnderConstruction = (underConstruction) -> {

            CreateLock lock = underConstruction.get(key);
            if (lock == null) {
                throw new IllegalStateException(
                        "Unexpected state: No CreateLock for outstanding key "
                        + key.toString());
            }
            synchronized (lock) {
                try {
                    if (instance != null) {
                        // instancesPlaceholder array will be populated with
                        // the instances map by a sub-call of the executor
                        writeToInstances.run();
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
            executor = () -> {
                instances.write(instances -> {
                    instancesPlaceholder[0] = instances;
                    underConstruction.write(removeFromUnderConstruction);
                    return null;
                });
            };

        } else {
            executor = () -> underConstruction.write(removeFromUnderConstruction);
        }

        // Don't block the current thread waiting for another lock on underConstruction and (maybe) instances
        (new Thread(executor)).start();
    }

    public CallableWithArg<K, I, NothingThrown> getConstruct() {
        return this.construct;
    }



    private class CreateLock {
        final K key;
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
}
