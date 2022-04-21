package nessusTools.sync;

import nessusTools.util.*;
import org.apache.logging.log4j.*;

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

    private static final ReadWriteLock<Map<ReadWriteLock, Void>, Void>
             instances = new ReadWriteLock<>();

    private static final Var.Long counter = new Var.Long(1);

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
    private final Lock gcLock = new Lock();

    private final long id;

    private ReadWriteLock() {
        // static instances ReadWriteLock only
        Map map = new WeakHashMap<>();
        this.object = (O) map;
        this.view = (O) Collections.unmodifiableMap(map);
        map.put(this, null);
        this.id = 0;
    }

    public ReadWriteLock(O objectToLock) {
        this(objectToLock, objectToLock);
    }

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
    
    public final R read(Lambda1<O, R> lambda) {
        return (R) this.read(null, lambda);
    }

    public final R write(Lambda1<O, R> lambda) throws ConcurrentAccessException {
        return (R) this.write(null, lambda);
    }

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
        synchronized (this.waitingForRead) {
            this.waitingForRead.add(readLock.thread);
        }

        synchronized (readLock) {
            try {
                synchronized (addLock) {
                    synchronized (removeLock) {
                        readLocks.put(readLock.thread, readLock);
                    }
                }
                synchronized (this.waitingForRead) {
                    this.waitingForRead.remove(readLock.thread);
                }

                return lambda.call(view);

            } finally {
                readLock.active = false;
                readLock.thread = null;
                this.readThreads.mutated = true;
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
                    "A thread cannot grab a write lock until it has released all of its read locks!");

        } else {
            Thread currentThread = Thread.currentThread();
            synchronized (this.waitingForWrite) {
                this.waitingForWrite.add(currentThread);
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

    public final boolean holdsWriteLock() {
        return Thread.holdsLock(writeLock);
    }

    public final boolean holdsWriteLock(Thread thread) {
        synchronized (this.waitingForWrite) {
            return this.currentWriteThread != null
                    && Objects.equals(this.currentWriteThread, thread);
        }

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

    public final Thread getCurrentWriteThread() {
        return this.currentWriteThread;
    }

    /**
     * Returns whether the current thread is blocking the passed thread from obtaining
     * a read or write lock
     *
     * @param thread
     * @return
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

    public boolean isWaitingForReadLock(Thread thread) {
        synchronized (this.waitingForRead) {
            return this.waitingForRead.contains(thread);
        }
    }

    public boolean isWaitingForWriteLock(Thread thread) {
        synchronized (this.waitingForWrite) {
            return this.waitingForWrite.contains(thread);
        }
    }

    public boolean isWaitingForLock(Thread thread) {
        return this.isWaitingForReadLock(thread) ||
                this.isWaitingForWriteLock(thread);
    }

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


    // Does the current thread block the given thread from obtaining any locks?
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

    public final Set<Thread> getCurrentReadThreads() {
        return this.readThreads;
    }



    private void clearGarbageCollector() {
        this.garbageCollector = null;
    }

    private void startGarbageCollector() {
        this.garbageCollector = new Thread(() -> {
            try {
                Thread garbageCollector = Thread.currentThread();
                garbageCollector.setPriority(Thread.MIN_PRIORITY);
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

                    garbageCollector.setPriority(Thread.MAX_PRIORITY);
                    this.clear(inactive);
                    garbageCollector.setPriority(Thread.MIN_PRIORITY);

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
        }, "ReadWriteLock (" + this.id + ") garbage collector");

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
