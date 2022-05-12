package nessusTools.sync;

import nessusTools.util.*;
import org.apache.logging.log4j.*;

import javax.ejb.*;
import java.util.*;

/**
 * Simple class for synchronizing concurrent read access and exclusive write access to an object.
 * The Object is passed into the constructor, and should only be accessed via lambdas submitted to the
 * read() or write() methods.  The object will be provided as the argument to these lambdas.
 * <p>
 * It is entirely up to the implementation to honor read and write rules. This class simply helps to
 * synchronize those operations.  However, the implementation may choose to submit both the object AND
 * an unmodifiable view of the object to the ReadWriteLock constructor, where the original object
 * will be sent to write lambdas and the unmodifiable view will be sent read lambdas.  If only one
 * argument is submitted, the same object is sent to both.  Alternatively, you may pass a single
 * Map, Set, List, or Collection to the respective static methods which create an unmodifiable view
 * of the original that is used for reading in a new ReadWriteLock instance (the original, of course,
 * will be used for writing).
 * <p>
 * Mechanics:
 * <p>
 * Multiple threads can read at one time.  Only one thread can write at a time, and no other
 * threads can read while the write thread holds the lock.  Note that the write thread will still be
 * able to run read lambdas with the read() method while holding the write lock.
 * <p>
 * More importantly, a thread with only a read lock must release all read locks BEFORE it requests
 * a write lock.  Otherwise it could lead to a deadlock if multiple threads are doing this
 * simultaneously, but an ConcurrentAccessException will be thrown to prevent this.
 * <p>
 * Type Parameters:
 * * O : Object type, of the object needing synchronized access and passed into the constructor.  This will also
 * *          be the object and type submitted to the read and write lambdas
 * *
 * * R : Return type from the most commonly used lambda.  Typically, this is the type being held in the list/map/set
 * *          being synchronized. You can return null and ignore the return value if it is not needed.  If you need a
 * *          different return type under varying situations, this can be specified on a per-invocation basis using
 * *          read/write(Class&lt;T&gt; returnType, Callable&lt;O, T&gt; lambda)
 *
 * @param <O> the object for which read/write access needs to be synchronized
 * @param <R> the standard return value from the read/write lambdas.  Other values types can also
 *           be returned with the overloaded methods
 */
public class ReadWriteLock<O, R> {
    private static Logger logger = LogManager.getLogger(ReadWriteLock.class);

    /**
     * Creates a ReadWriteLock with the given map for writing, and an
     * unmodifiable view of the map for reading
     *
     * @param <K> the key type for the map
     * @param <V> the value type for the map
     * @param <R> the standard return type
     * @param map the map
     * @return the read write lock
     */
    public static <K, V, R> ReadWriteLock<Map<K, V>, R> forMap(Map<K, V> map) {
        Map view = Collections.unmodifiableMap(map);
        return new ReadWriteLock<>(map, view);
    }

    /**
     * Creates a ReadWriteLock with the given set for writing, and an
     * unmodifiable view of the set for reading
     *
     * @param <T> the set type param
     * @param <R> the standard return type
     * @param set the set
     * @return the read write lock
     */
    public static <T, R> ReadWriteLock<Set<T>, R> forSet(Set<T> set) {
        Set<T> view = Collections.unmodifiableSet(set);
        return new ReadWriteLock<>(set, view);
    }

    /**
     * Creates a ReadWriteLock with the given list for writing, and an
     * unmodifiable view of the list for reading
     *
     * @param <T>  the list type param
     * @param <R> the standard return type
     * @param list the list
     * @return the read write lock
     */
    public static <T, R> ReadWriteLock<List<T>, R> forList(List<T> list) {
        List<T> view = Collections.unmodifiableList(list);
        return new ReadWriteLock<>(list, view);
    }

    /**
     * Creates a ReadWriteLock with the given collection for writing, and an
     * unmodifiable view of the collection for reading
     *
     * @param <T> the collection type param
     * @param <R> the standard return type
     * @param collection the collection
     * @return the read write lock
     */
    public static <R, T> ReadWriteLock<Collection<T>, R> forCollection(Collection<T> collection) {
        Collection<T> view = Collections.unmodifiableCollection(collection);
        return new ReadWriteLock<>(collection, view);
    }

    private static final Var.Long counter = new Var.Long(1);

    private static final ReadWriteLock<Map<ReadWriteLock, Void>, Void>
             instances = new ReadWriteLock<>();

    private static final ReadWriteLock<Map<Thread, Void>, Boolean>
            disruptableThreads = forMap(new WeakHashMap<>());

    private final O object;
    private final O view;

    private final Map<Thread, Lock> readLocks = new WeakHashMap<>();
    private final Lock writeLock = new Lock();

    private final Set<Thread> waitingForRead = new LinkedHashSet<>();
    private final Set<Thread> waitingForWrite = new LinkedHashSet<>();

    private final ReadThreadsSet readThreads = new ReadThreadsSet();
    private Thread currentWriteThread = null;

    private Thread garbageCollector = null;
    private final Lock addLock = new Lock();
    private final Lock removeLock = new Lock();
    //private final Lock gcLock = new Lock();

    private final long id;

    /**
     * Private constructor used only to construct the first instance of ReadWriteLock, which synchronizes
     * access to the static instances map.  This is a WeakHashMap holding weak references to all instances
     * of ReadWriteLock which have been constructed and not GC'd
     */
    private ReadWriteLock() {
        // static instances ReadWriteLock only
        Map map = new WeakHashMap<>();
        this.object = (O) map;
        this.view = (O) Collections.unmodifiableMap(map);
        map.put(this, null);
        this.id = 0;
    }

    /**
     * Instantiates a new Read write lock that synchronizes access to the given object.
     *
     * @param objectToLock the object to lock
     */
    public ReadWriteLock(O objectToLock) {
        this(objectToLock, objectToLock);
    }

    /**
     * Instantiates a new Read write lock that synchronizes access to the given object,
     * and the given unmodifiable view of this object
     *
     * @param objectToLock     the object to lock
     * @param unmodifiableView the unmodifiable view of the object to lock
     */
    public ReadWriteLock(O objectToLock, O unmodifiableView) {
        this.object = objectToLock;
        this.view = unmodifiableView;
        synchronized (counter) {
            this.id = counter.value++;
        }
        instances.write(instances -> instances.put(this, null));
    }

    private class Lock {
        private Thread thread = Thread.currentThread();
        private boolean active = true;
        public boolean equals(Object o) {
            return o == this;
        }
    }

    private final Lock getCurrentLock() {
        if (Thread.holdsLock(writeLock)) {
            return writeLock;
        }
        synchronized (removeLock) {
            Thread current = Thread.currentThread();
            Lock lock = this.readLocks.get(current);
            if (lock == null || !(lock.active && Thread.holdsLock(lock))) {
                return null;
            }

            return lock;
        }
    }

    /**
     * Grants read access to the provided lambda, allowing it to return the
     * standard return type
     *
     * @param lambda the lambda
     * @return the value returned by the lambda
     */
    public final R read(Lambda1<O, R> lambda) {
        return (R) this.read(null, lambda);
    }

    /**
     * Grants write access to the provided lambda, allowing it to return the standard
     * return type
     *
     * @param lambda the lambda
     * @return the value returned by the lambda
     * @throws ConcurrentAccessException if the invoking thread already holds a read lock
     * for this ReadWriteLock instance.  It must release the read lock before requesting a
     * write lock
     */
    public final R write(Lambda1<O, R> lambda) throws ConcurrentAccessException {
        return (R) this.write(null, lambda);
    }

    /**
     * Grants read access to the provided lambda, allowing it to return any
     * type
     *
     * @param <T>        the type parameter
     * @param returnType the return type
     * @param lambda     the lambda
     * @return the value returned by the lambda
     */
    public final <T> T read(Class<T> returnType, Lambda1<O, T> lambda) {
        Lock readLock = this.getCurrentLock();
        if (readLock != null) {
            //could also be the write lock, but that's ok
            // a write lock can also grab a read lock
            // ... just not the other way around

            synchronized (readLock) {
                return lambda.call(this.view);
            }
        }

        readLock = new Lock();
        boolean checkDisruptable;
        synchronized (this.waitingForRead) {
            checkDisruptable = this.waitingForRead.add(readLock.thread);
        }

        if (checkDisruptable) {
            this.checkDisruptable(false);
        }


        try {
            synchronized (readLock) {
                synchronized (addLock) {
                    synchronized (removeLock) {
                        readLocks.put(readLock.thread, readLock);
                    }
                }
                synchronized (this.waitingForRead) {
                    this.waitingForRead.remove(readLock.thread);
                }

                try {
                    return lambda.call(view);

                } finally {
                    readLock.active = false;
                    readLock.thread = null;
                    this.readThreads.mutated = true;
                }
            }

        } finally {
            synchronized (removeLock) {
                readLocks.remove(readLock.thread, readLock);
            }
        }

    }

    /**
     * Grants write access to the provided lambda, allowing it to return any
     * type
     *
     * @param <T>        the type parameter
     * @param returnType the return type
     * @param lambda     the lambda
     * @return the value returned by the lambda
     * @throws ConcurrentAccessException if the invoking thread already holds a read lock
     * for this ReadWriteLock instance.  It must release the read lock before requesting a
     * write lock
     */
    public final <T> T write(Class<T> returnType, Lambda1<O, T> lambda)
            throws ConcurrentAccessException {

        if (this.holdsWriteLock()) {
            synchronized (writeLock) {
                return lambda.call(this.object);
            }
        } else if (this.holdsReadLock()) {
            throw new ConcurrentAccessException(
                    "A thread cannot grab a write lock until it has released all of its read locks!");

        } else {
            Thread currentThread = Thread.currentThread();
            boolean checkDisruptable;
            synchronized (this.waitingForWrite) {
                checkDisruptable = this.waitingForWrite.add(currentThread);
            }

            if (checkDisruptable) {
                checkDisruptable(true);
            }

            synchronized (addLock) {
                this.currentWriteThread = currentThread;
                waitForWriteLock();
                synchronized (writeLock) {
                    synchronized (this.waitingForWrite) {
                        this.waitingForWrite.remove(currentThread);
                    }

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
            List<Lock> readLocksCopy;

            synchronized (removeLock) {
                readLocksCopy = new ArrayList(this.readLocks.values());
            }
            boolean allInactive = true;

            for (Lock lock : readLocksCopy) {
                synchronized (lock) {
                    if (lock.active) {
                        // In theory this should be unreachable ...
                        // All "read" threads set lock.active = false before releasing
                        // their lock
                        allInactive = false;
                        break;
                    }
                }
            }


            if (allInactive) {
                synchronized (removeLock) {
                    if (readLocks.size() > 0) {
                        this.readLocks.clear();
                        // Makes the GC's job easier, since this is basically doing the same thing
                        // by iterating over the locks and checking if they are active.
                        // Also negates the need to start GC if it's not currently running ...
                        // It would be started the next time a read lock is created
                        // and finished, and wouldn't be needed until that time anyways.
                    }
                }
                return;
            }
        }
    }

    /* TODO ???
    public void waitLock()
            throws InterruptedException, IllegalMonitorStateException {

        Lock currentLock = this.getCurrentLock();
        if (currentLock == null) {
            throw new IllegalMonitorStateException();
        }

    }

    public void waitLock(long timeoutMillis)
            throws InterruptedException, IllegalMonitorStateException {

        Lock currentLock = this.getCurrentLock();
        if (currentLock == null) {
            throw new IllegalMonitorStateException();
        }

    }

    public void notifyWriteLock() {

    }

    public void notifyLock(Thread thread) {

    }
     */

    /**
     * Find out if the current thread holds the write lock on this ReadWriteLock
     *
     * @return true if the current thread holds the write lock, false if not
     */
    public final boolean holdsWriteLock() {
        return Thread.holdsLock(writeLock);
    }

    /**
     * Find out if the provided thread holds the write lock on this ReadWriteLock
     *
     * @param thread the thread to check
     * @return true if the provided thread holds the write lock, false if not
     */
    public final boolean holdsWriteLock(Thread thread) {
        synchronized (this.waitingForWrite) {
            return this.currentWriteThread != null
                    && Objects.equals(this.currentWriteThread, thread);
        }

    }

    /**
     * Find out if the current thread holds a read lock on this ReadWriteLock
     *
     * @return true if the current thread holds a read lock, false if not
     */
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

    /**
     * Find out if the provided thread holds a read lock on this ReadWriteLock
     *
     * @param thread the thread to check
     * @return true if the provided thread holds a read lock, false if not
     */
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

    /**
     * Find out if the current thread holds any lock (read or write) on this ReadWriteLock
     *
     * @return true if the current thread holds a lock, false if not
     */
    public final boolean holdsLock() {
        return this.holdsWriteLock() || this.holdsReadLock();
    }

    /**
     * Returns the thread that currently holds the write lock on this ReadWriteLock, if any
     *
     * @return that thread that currently holds the write lock, or null if none does
     */
    public final Thread getCurrentWriteThread() {
        return this.currentWriteThread;
    }

    /**
     * Returns whether the current thread is blocking the provided thread from obtaining
     * a read or write lock from this ReadWriteLock instance
     *
     * @param thread the thread that may or may not be blocked
     * @return boolean whether this thread is blocking it
     */
    public final boolean isCurrentBlocking(Thread thread) {
        return this.isCurrentBlocking(Thread.currentThread(), thread);
    }

    private final boolean isCurrentBlocking(Thread current, Thread thread) {
        if (Objects.equals(thread, current)) {
            return false;
        }

        if (this.isWaitingForReadLock(thread)) {
            return this.holdsWriteLock();
        }

        if (this.isWaitingForReadLock(thread)) {
            return this.holdsLock();

        }

        return false;
    }

    /**
     * Returns whether the provided thread is waiting for a read lock
     *
     * @param thread the thread to check
     * @return whether the thread is waiting for a read lock
     */
    public boolean isWaitingForReadLock(Thread thread) {
        synchronized (this.waitingForRead) {
            return this.waitingForRead.contains(thread);
        }
    }

    /**
     * Returns whether the provided thread is waiting for a write lock
     *
     * @param thread the thread to check
     * @return whether the thread is waiting for a write lock
     */
    public boolean isWaitingForWriteLock(Thread thread) {
        synchronized (this.waitingForWrite) {
            return this.waitingForWrite.contains(thread);
        }
    }

    /**
     * Returns whether the provided thread is waiting for any lock (read or write)
     *
     * @param thread the thread to check
     * @return whether the thread is waiting for a lock
     */
    public boolean isWaitingForLock(Thread thread) {
        return this.isWaitingForReadLock(thread) ||
                this.isWaitingForWriteLock(thread);
    }

    /**
     * Gets the set of all threads which are blocked by the provided thread
     * within the context of this ReadWriteLock
     *
     * @param thread the thread that may be blocking other threads
     * @return the threads blocked by the provided thread
     */
    public Set<Thread> getThreadsBlockedBy(Thread thread) {
        Set<Thread> blocked = new LinkedHashSet<>();
        getThreadsBlockedBy(thread, blocked);
        return blocked;
    }

    // *immediately* blocked by
    private void getThreadsBlockedBy(Thread thread, Set<Thread> blocked) {
        boolean holdsWrite;
        boolean holdsRead;
        synchronized (this.removeLock) {
            holdsWrite = this.currentWriteThread != null
                    && Objects.equals(thread, this.currentWriteThread);

            if (holdsWrite) {
                holdsRead = false;

            } else {
                Lock lock = this.readLocks.get(thread);
                holdsRead = lock != null && lock.active;
            }
        }

        if (holdsWrite || holdsRead) {
            synchronized (this.waitingForWrite) {
                blocked.addAll(waitingForWrite);

                if (holdsWrite) {
                    // a thread may both hold the addLock AND be waiting for full write lock at the same time
                    // ... it is not blocking itself, however
                    blocked.remove(thread);
                }
            }
        }

        if (holdsWrite) {
            synchronized (this.waitingForRead) {
                blocked.addAll(this.waitingForRead);
            }
        }
    }

    // puts thread blocking relationships into a RecursiveMap
    private void putThreadBlocks(RecursiveMap<Thread> threadMap) {
        Set<Thread> readLocks = new LinkedHashSet<>();
        Thread writeThread;
        synchronized (this.removeLock) {
            for (Map.Entry<Thread, Lock> entry : this.readLocks.entrySet()) {
                if (entry.getValue().active) {
                    readLocks.add(entry.getKey());
                }
            }
            writeThread = this.currentWriteThread;
        }

        if (writeThread != null) {
            synchronized (this.waitingForRead) {
                for (Thread waiting : this.waitingForRead) {
                    threadMap.putChild(writeThread, waiting);
                }
            }
            synchronized (this.waitingForWrite) {
                for (Thread waiting : this.waitingForWrite) {
                    if (!Objects.equals(writeThread, waiting)) {
                        // a thread may both hold the addLock AND be waiting for full write lock at the same time
                        // ... it is not blocking itself, however
                        threadMap.putChild(writeThread, waiting);
                    }
                    for (Thread readLock : readLocks) {
                        threadMap.putChild(readLock, waiting);
                    }
                }
            }

        } else {
            synchronized (this.waitingForWrite) {
                for (Thread waiting : this.waitingForWrite) {
                    for (Thread readLock : readLocks) {
                        threadMap.putChild(readLock, waiting);
                    }
                }
            }
        }
    }

    /**
     * Register the provided thread as a "disruptable" thread, meaning that it may
     * be denied read/write access if doing so would lead to a circular blocking
     * pattern that would result in deadlock
     *
     * @param disruptableThread the disruptable thread
     */
    public static void registerAsDisruptable(Thread disruptableThread) {
        disruptableThreads.write(dtMap -> {
            dtMap.put(disruptableThread, null);
            return true;
        });
    }
    
    private void checkDisruptable(boolean write) throws ThreadDisruption {
        if (this == disruptableThreads || this == instances) return;
        
        Thread current = Thread.currentThread();
        if (disruptableThreads.read(dtMap -> !dtMap.containsKey(current))) {
            return;
        }
        if (!InstancesTracker.getThreadBlockingMap().getCircularKeys().contains(current)) {
            return;
        }

        if (write) synchronized (this.waitingForWrite) {
            this.waitingForWrite.remove(current);

        } else synchronized (this.waitingForRead) {
            this.waitingForRead.remove(current);
        }

        throw new ThreadDisruption();
    }

    /**
     * Error (NOT Exception) thrown when a disruptable thread would create a
     * deadlock if allowed the request read/write access.  Because this is NOT
     * an exception, it should pass through almost all try ... catch statements and unwind
     * the thread back to its origin.  Similar to ThreadDeath, except that the thread may
     * catch this and continue on to its next operation without dying
     */
    public static class ThreadDisruption extends Error {
        private ThreadDisruption() { }
    }

    /**
     * Gets the recursive map of which threads are blocking which other threads
     * from obtaining locks, across all instances of ReadWriteLock
     *
     * @return the thread blocking map
     */
    public static RecursiveMap<Thread> getThreadBlockingMap() {
        RecursiveMap<Thread> threadMap = new RecursiveMap<>();

        instances.read(instances -> {
            for (ReadWriteLock rwl : instances.keySet()) {
                rwl.putThreadBlocks(threadMap);
            }
            return null;
        });
        return threadMap;
    }


    /**
     * Determine whether the current thread is blocking the provided thread
     * from obtaining any locks (read or write) in any instance of ReadWriteLock
     *
     * @param thread the thread that may be blocked
     * @return whether the current thread is blocking the provided thread
     */
    public static boolean isBlockingAnyLock(Thread thread) {
        return instances.read(Boolean.class, instances -> {
            Thread current = Thread.currentThread();
            for (ReadWriteLock rwl : instances.keySet()) {
                if (rwl.isCurrentBlocking(thread, current)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Determine whether the provided thread is waiting for a read lock in
     * any instance of ReadWriteLock
     *
     * @param thread the thread to check
     * @return true if the provided thread is waiting for any read lock
     */
    public static boolean isWaitingForAnyReadLock(Thread thread) {
        return instances.read(Boolean.class, instances -> {
            for (ReadWriteLock rwl : instances.keySet()) {
                if (rwl.isWaitingForReadLock(thread)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Determine whether the provided thread is waiting for a write lock in
     * any instance of ReadWriteLock
     *
     * @param thread the thread to check
     * @return true if the provided thread is waiting for any write lock
     */
    public static boolean isWaitingForAnyWriteLock(Thread thread) {
        return instances.read(Boolean.class, instances -> {
            for (ReadWriteLock rwl : instances.keySet()) {
                if (rwl.isWaitingForWriteLock(thread)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Determine whether the provided thread is waiting for a lock (read or write) in
     * any instance of ReadWriteLock
     *
     * @param thread the thread to check
     * @return true if the provided thread is waiting for any lock (read or write)
     */
    public static boolean isWaitingForAnyLock(Thread thread) {
        return instances.read(Boolean.class, instances -> {
            for (ReadWriteLock rwl : instances.keySet()) {
                if (rwl.isWaitingForLock(thread)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Gets an unmodifiable view of the set of threads currently holding a read lock
     * on this ReadWriteLock
     *
     * @return the current read threads
     */
    public final Set<Thread> getCurrentReadThreads() {
        return this.readThreads;
    }

    private class ReadThreadsSet implements Set<Thread> {
        private Set<Thread> keySet = ReadWriteLock.this.readLocks.keySet();
        private boolean mutated = false;

        private <T> T clearInactive(Class<T> returnType, Lambda0<T> returnGetter) {
            while (true) {
                synchronized (ReadWriteLock.this.removeLock) {
                    if (!this.mutated) {
                        return returnGetter.call();
                    }
                    this.mutated = false;

                    ReadWriteLock.this.readLocks.entrySet().removeIf(
                            entry -> !entry.getValue().active);
                }
            }
        }

        @Override
        public int size() {
            return clearInactive(Integer.class, () -> keySet.size());
        }

        @Override
        public boolean isEmpty() {
            return clearInactive(Boolean.class, () -> keySet.isEmpty());
        }

        @Override
        public boolean contains(Object o) {
            return clearInactive(Boolean.class, () -> keySet.contains(o));
        }

        @Override
        public Iterator<Thread> iterator() {
            return new ReadThreadsIterator();
        }

        @Override
        public Object[] toArray() {
            return clearInactive(Object[].class, () -> keySet.toArray());
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return (T[]) clearInactive(Object[].class, () -> keySet.toArray(a));
        }

        @Override
        public boolean add(Thread thread) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return clearInactive(Boolean.class, () -> keySet.containsAll(c));
        }

        @Override
        public boolean addAll(Collection<? extends Thread> c) {
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
     * A special iterator to handle dynamically changing state of read lock threads,
     * for the ReadThreadsSet
     */
    public class ReadThreadsIterator implements Iterator<Thread> {
        private final Map<Thread, Void> alreadyUsed = new WeakHashMap();
        private final ReadThreadsSet rtSet = ReadWriteLock.this.readThreads;
        private final Map<Thread, Lock> map = ReadWriteLock.this.readLocks;

        private Thread next;
        private boolean done = false;

        private ReadThreadsIterator() { }

        @Override
        public boolean hasNext() {
            if (done) {
                return false;

            } else if (this.next != null) {
                return true;
            }

            return rtSet.clearInactive(Boolean.class, () -> {
                for (Map.Entry<Thread, Lock> entry : this.map.entrySet()) {
                    Thread thread = entry.getKey();
                    if (!alreadyUsed.containsKey(thread) && entry.getValue().active) {
                        this.next = thread;
                        alreadyUsed.put(thread, null);
                        return true;
                    }
                }
                done = true;
                return false;
            });
        }

        @Override
        public Thread next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Thread next = this.next;
            this.next = null;
            return next;
        }
    }
}
