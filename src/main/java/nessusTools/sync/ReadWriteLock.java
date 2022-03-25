package nessusTools.sync;

import org.apache.logging.log4j.*;
import org.jboss.jandex.*;

import javax.ejb.*;
import java.util.*;

/**
 * Simple class for synchronizing concurrent read access and exclusive write access to an object.
 * The Object is passed into the constructor, and should only be accessed via lambdas submitted to the
 * read() or write() methods.  The object will be provided as the argument to these lambdas.
 *
 * It is entirely up to the implementation to honor read and write rules. This class simply helps to
 * synchronize those operations.  However, the implementation may choose to submit both the object AND
 * an unmodifiable view of the object to the ReadWriteLock constructor, where the original object
 * will be sent to write lambdas and the unmodifiable view will be sent read lambdas.  If only one
 * argument is submitted, the same object is sent to both.  Alternatively, you may pass a single
 * Map, Set, List, or Collection to the respective static methods which create an unmodifiable view
 * of the original that is used for reading in a new ReadWriteLock instance (the original, of course,
 * will be used for writing).
 *
 * Mechanics:
 *
 * Multiple threads can read at one time.  Only one thread can write at a time, and no other
 * threads can read while the write thread holds the lock.  Note that the write thread will still be
 * able to run read lambdas with the read() method while holding the write lock.
 *
 * More importantly, a thread with only a read lock must release all read locks BEFORE it requests
 * a write lock.  Otherwise it could lead to a deadlock if multiple threads are doing this
 * simultaneously, but an ConcurrentAccessException will be thrown to prevent this.
 *
 * Type Parameters:
 *  * O : Object type, of the object needing synchronized access and passed into the constructor.  This will also
 *  *          be the object and type submitted to the read and write lambdas
 *  *
 *  * R : Return type from the most commonly used lambda.  Typically, this is the type being held in the list/map/set
 *  *          being synchronized. You can return null and ignore the return value if it is not needed.  If you need a
 *  *          different return type under varying situations, this can be specified on a per-invocation basis using
 *  *          read/write(Class&lt;T&gt; returnType, Callable&lt;O, T&gt; lambda)
 *
 */
public class ReadWriteLock<O, R> {
    private static Logger logger = LogManager.getLogger(ReadWriteLock.class);

    public static <R, K, V> ReadWriteLock<Map<K, V>, R> forMap(Map<K, V> map) {
        Map view = Collections.unmodifiableMap(map);
        return new ReadWriteLock<>(map, view);
    }

    public static <R, T> ReadWriteLock<Set<T>, R> forSet(Set<T> set) {
        Set<T> view = Collections.unmodifiableSet(set);
        return new ReadWriteLock<>(set, view);
    }

    public static <R, T> ReadWriteLock<List<T>, R> forList(List<T> list) {
        List<T> view = Collections.unmodifiableList(list);
        return new ReadWriteLock<>(list, view);
    }

    public static <R, T> ReadWriteLock<Collection<T>, R> forCollection(Collection<T> collection) {
        Collection<T> view = Collections.unmodifiableCollection(collection);
        return new ReadWriteLock<>(collection, view);
    }

    private final O object;
    private final O view;

    private final Map<Thread, Lock> readLocks = new WeakHashMap<>();
    private final Lock writeLock = new Lock();
    private Thread currentWriteThread = null;
    private Thread garbageCollector = null;
    private final Lock addLock = new Lock();
    private final Lock removeLock = new Lock();
    private final Lock gcLock = new Lock();

    public ReadWriteLock(O objectToLock) {
        this.object = objectToLock;
        this.view = objectToLock;
    }

    public ReadWriteLock(O objectToLock, O unmodifiableView) {
        this.object = objectToLock;
        this.view = unmodifiableView;
    }

    private class Lock {
        private Thread thread = Thread.currentThread();
        private boolean active = true;
        public boolean equals(Object o) {
            return o == this;
        }
    }

    public final boolean holdsWriteLock() {
        return Thread.holdsLock(writeLock);
    }

    public final boolean holdsWriteLock(Thread thread) {
        return Thread.holdsLock(writeLock);
    }

    public final boolean holdsReadLock() {
        synchronized (removeLock) {
            Thread current = Thread.currentThread();
            Lock lock = this.readLocks.get(current);
            if (lock == null) {
                return false;
            }

            return lock.active && Thread.holdsLock(lock);
        }
    }

    public final boolean holdsReadLock(Thread thread) {
        if (thread == null) {
            return false;
        }

        synchronized (removeLock) {
            Lock lock = this.readLocks.get(thread);
            if (lock == null) {
                return false;
            }

            return lock.active;
        }
    }

    public final boolean holdsLock() {
        return this.holdsWriteLock() || this.holdsReadLock();
    }
    
    public final R read(Lambda1<O, R> lambda) {
        return (R) this.read(null, lambda);
    }

    public final R write(Lambda1<O, R> lambda) throws ConcurrentAccessException {
        return (R) this.write(null, lambda);
    }

    public final <T> T read(Class<T> returnType, Lambda1<O, T> lambda) {
        if (this.holdsLock()) {
            return lambda.call(this.view);
        }

        Lock readLock = new Lock();
        synchronized (readLock) {
            try {
                synchronized (addLock) {
                    synchronized (removeLock) {
                        readLocks.put(readLock.thread, readLock);
                    }
                }

                return lambda.call(view);

            } finally {
                readLock.active = false;
                readLock.thread = null;
                // don't hold up the current thread with removing the read lock from the map
                // setting readLock.thread to null will allow for garbage collection of the entry
                // once the thread dies (or maybe even sooner?) since readLock is a WeakHashMap ????

                synchronized (this.gcLock) {
                    if (this.garbageCollector == null) {
                        startGarbageCollector();
                    }
                }
            }
        }
    }

    public final <T> T write(Class<T> returnType, Lambda1<O, T> lambda)
            throws ConcurrentAccessException {

        if (this.holdsWriteLock()) {
            synchronized (writeLock) {
                return lambda.call(this.object);
            }
        } else if (this.holdsReadLock()) {
            throw new ConcurrentAccessException(
                    "A thread cannot grab a write lock until it has released all its read locks!");

        } else {
            synchronized (addLock) {
                waitForWriteLock();
                synchronized (writeLock) {
                    this.currentWriteThread = Thread.currentThread();
                    try {
                        return lambda.call(this.object);

                    } finally {
                        this.currentWriteThread = null;
                    }
                }
            }
        }
    }


    /**
     * Iterates over readLocks and waits for all of them to release
     *
     * IMPORTANT: ONLY INVOKE WHILE HOLDING addLock, AND ONLY ON THE FIRST PASS THROUGH
     * WRITE LOCK FOR THIS THREAD.
     */
    private void waitForWriteLock() throws ConcurrentAccessException {
        // iterate over the existing read locks and wait for all of them to be released
        while (true) {
            int size;

            synchronized (removeLock) {
                // this.readLocks list cannot be modified externally once inside this code block.
                // Since both addLock and removeLock are held by this thread, we can
                // guarantee it will not change during iteration.  However, it is still
                // possible that the state of individual readLocks within the list may change
                // which is why we wait for a lock on each individually before inspecting
                boolean allInactive = true;

                for (Lock lock : this.readLocks.values()) {
                    synchronized (lock) {
                        if (lock.active) {
                            allInactive = false;
                            // In theory the below break should be unreachable ...
                            // All "read" threads set lock.active = false before releasing
                            // their lock
                            break;
                        }
                    }
                }
                if (allInactive) {
                    if (readLocks.size() > 0) {
                        this.readLocks.clear();
                        // Makes the GC's job easier, since this is basically doing the same thing
                        // by iterating over the locks and checking if they are active.
                        // Also negates the need to start GC if it's not currently running ...
                        // It would be started the next time a read lock is created
                        // and finished, and wouldn't be needed until that time anyways.
                    }
                    return;
                }
            }
        }
    }

    private void clearGarbageCollector() {
        this.garbageCollector = null;
    }

    private void startGarbageCollector() {
        this.garbageCollector = new Thread(() -> {
            try {
                int zeroLocksCounter = 0;
                Map<Thread, Lock> inactive = new WeakHashMap<>();
                while (true) {
                    List<Map.Entry<Thread, Lock>> entries;

                    synchronized (removeLock) {
                        entries = new ArrayList(this.readLocks.entrySet());
                    }

                    boolean zeroLocks = true;
                    int size = entries.size();

                    for (Map.Entry<Thread, Lock> entry : entries) {
                        // can use an iterator since this is a copy
                        Lock lock = entry.getValue();
                        if (lock == null) continue;
                        zeroLocks = false;

                        if (!lock.active) {
                            inactive.put(entry.getKey(), lock);
                        }
                    }

                    entries = null; //allows garbage collection of thread in the map entries ???

                    if (zeroLocks) {
                        if (++zeroLocksCounter >= 5) {
                            // discontinue the GC thread if there are no
                            // new readLocks created in the last 5+ seconds
                            break;

                        }

                    } else {
                        zeroLocksCounter = 0;
                    }

                    this.clear(inactive);

                    // size is used as divisor for sleep time
                    // the more locks are present, the more active the GC should be
                    if (size <= 0) {
                        size = 1;
                    } else {
                        size++;
                    }

                    try {
                        Thread.sleep(1000 / size);
                    } catch (Throwable e) { }
                }

            } finally {
                synchronized (removeLock) {
                    synchronized (gcLock)   {
                        this.clearGarbageCollector();
                        if (this.readLocks.size() > 0) {
                            startGarbageCollector();
                        }
                    }
                }
            }
        });

        this.garbageCollector.start();
    }

    private void clear(Map<Thread, Lock> inactive) {
        if (inactive.size() <= 0) return;

        // CLEAR all the read locks identified as inactive!
        synchronized (removeLock) {
            if (this.readLocks.size() > 0) {
                // ^^^ in case a write lock cleared it for the GC :-)
                // means we can just skip this...
                return;
            }

            for (Map.Entry<Thread, Lock> entry : inactive.entrySet()) {
                Lock lock = entry.getValue();
                if (lock != null) {
                    synchronized (lock) {
                        if(!lock.active) {
                            this.readLocks.remove(entry.getKey());
                        } else {
                            //WTF!?
                            logger.error(
                                    "UNEXPECTED condition in garbage collecting thread: " +
                                            "A readLock that was marked as inactive is now active again!!??");
                        }
                    }
                } else {
                    this.readLocks.remove(entry.getKey());
                }
            }

        }
        inactive.clear();
    }
}
