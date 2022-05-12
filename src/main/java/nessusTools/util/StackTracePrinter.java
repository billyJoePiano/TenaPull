package nessusTools.util;

import nessusTools.sync.*;

import java.time.*;
import java.util.*;

/**
 * A debugging utility only (thus the use of println statements...)
 * for multi-thread concurrency issues.
 * Obtains the stack traces of all threads, grouping threads with the same
 * stack trace together, and showing any threads blocking other threads,
 * then determining if there are any circular blocks.
 */
public class StackTracePrinter extends Thread {
    private static boolean started = false;
    private static Var.Long counter = new Var.Long();

    private static long makeId() {
        synchronized (counter) {
            return ++counter.value;
        }
    }

    private final int delay;

    private StackTracePrinter(int delay) {
        super("StackTracePrinter #" + makeId());
        this.setDaemon(true);
        this.delay = delay;
        System.out.println(this + " constructed at " + LocalDateTime.now() + " (delay for " + delay + " ms)");
    }

    /**
     * Starts a stackTracePrinter thread with a delay of 0
     *
     * @return the name of the new thread
     */
    public static String startThread() {
        return startThread(0);
    }

    /**
     * Starts a stackTracePrinter thread with a delay of 0
     *
     * @param delay the number of millseconds to wait before performing the stack trace analysis and printing
     * @return the name of the new thread
     */
    public static String startThread(int delay) {
        Thread thread = new StackTracePrinter(delay);
        thread.start();
        return thread.toString();
    }

    @Override
    public void run() {
        System.out.println(this + " run at " + LocalDateTime.now() + " (delay for " + delay + " ms)");
        System.out.println("\t\t...actual run thread: " + Thread.currentThread());
        if (this.delay > 0) {
            try {
                Thread.sleep(this.delay);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        print();
    }

    /**
     * The main working method that obtains and analyzes the stack traces and thread
     * blocking patterns.  This may also be invoked by any thread from outside of the
     * StackTracePrinter class
     */
    public static void print() {
        System.err.println("STARTING STACK TRACE PRINTER ... " + Thread.currentThread());

        Map<String, StackTrace> stackTraces = new LinkedHashMap<>();
        LocalDateTime startLdt = LocalDateTime.now();
        long start = System.currentTimeMillis();

        RecursiveMap<Thread> threads = InstancesTracker.getThreadBlockingMap();

        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            String str = makeStackTraceString(entry.getValue(),  "\n");
            StackTrace stack = stackTraces.get(str);
            if (stack == null) {
                stack = new StackTrace(str);
                stackTraces.put(str, stack);
            }
            stack.add(entry.getKey());
        }

        Set<Thread> circularLocks = threads.getCircularKeys();

        long end = System.currentTimeMillis();

        List<StackTrace> sorted = new LinkedList(stackTraces.values()) ;
        Collections.sort(sorted);

        System.out.println();
        System.out.println();
        System.out.println("###################################################################");
        System.out.println("                 CURRENT THREADS, STACK TRACES");
        System.out.println(startLdt);
        System.out.println("###################################################################");

        Set<Thread> include = new LinkedHashSet<>();

        for (StackTrace stack : sorted) {
            System.out.println(stack.toString(include));

            Collection<Set<Thread>> iterate;

            if (stack.count > 10) {
                if (stack.intersection == null) {
                    System.out.println();
                    continue;
                }
                iterate = new ArrayList(1);
                iterate.add(stack.intersection);

            } else {
                iterate = stack.threads.values();
            }

            for (Set<Thread> set : iterate) {
                for (Thread thread : set) {
                    RecursiveMap<Thread> blocking = threads.get(thread);
                    if (blocking != null) {
                        System.out.println(blocking);

                        Set<Thread> parents = blocking.getParents();
                        if (parents.size() <= 0) {
                            if (!circularLocks.contains(blocking)) {
                                include.add(blocking.getKey());
                            }
                        }

                        System.out.println("\tBLOCKED BY:\n\t\t" + parents);

                        printParentBlocks(parents, threads, circularLocks, include, "\t\t");
                        System.out.println();
                    }
                }
            }

            System.out.println();
        }

        System.out.println();
        System.out.println();

        System.out.println("CIRCULAR BLOCKS:");
        System.out.println(circularLocks);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("###################################################################");
        System.out.println("                END OF THREAD STACK TRACES");
        System.out.print(LocalDateTime.now());
        System.out.println("   (processing time " + ((double)(end - start) / 1000) + " seconds)");
        System.out.println("###################################################################");
        System.out.println();
        System.out.println();
        System.out.println();
    }

    /**
     * Produces a string representation of the current thread's stack trace
     *
     * @return the string
     */
    public static String makeStackTraceString() {
        return makeStackTraceString(Thread.currentThread().getStackTrace(), "\n");
    }

    /**
     * Produces a string representation of the provided thread's stack trace
     *
     * @param thread the thread
     * @return the string
     */
    public static String makeStackTraceString(Thread thread) {
        return makeStackTraceString(thread.getStackTrace(), "\n");
    }

    /**
     * Produces a string representation of the provided stack trace elements array
     *
     * @param elements the stack trace elements
     * @return the string representation
     */
    public static String makeStackTraceString(StackTraceElement[] elements) {
        return makeStackTraceString(elements, "\n");
    }

    /**
     * Produces a string representation of the provided stack trace elements array
     * using the provided separator between frames of the stack.  This can be used,
     * for example, to indent the entire stack trace by a tab, or the like
     *
     * @param elements  the stack trace elements
     * @param separator the separator between frames
     * @return the string representation
     */
    public static String makeStackTraceString(StackTraceElement[] elements, String separator) {
        String str = "";
        for (StackTraceElement el : elements) {
            str += el.toString() + separator;
        }
        if (str.length() > separator.length()) {
            return str.substring(0, str.length() - separator.length());

        } else {
            return str;
        }
    }

    private static void printParentBlocks(Set<Thread> parents,
                                            RecursiveMap<Thread> threads,
                                            Set<Thread> circularLocks,
                                            Set<Thread> include,
                                            String tabs) {
        tabs += "\t";

        for (Thread blockedBy : parents) {
            include.add(blockedBy);
            if (circularLocks.contains(blockedBy)) continue;
            RecursiveMap<Thread> map = threads.get(blockedBy);
            if (map != null) {
                parents = map.getParents();
                if (parents.size() > 0) {
                    System.out.println(tabs + parents);
                    printParentBlocks(parents, threads, circularLocks, include, tabs);

                }
            }
        }
    }

    private static class StackTrace implements Comparable<StackTrace> {
        private Map<Thread.State, Set<Thread>> threads = new LinkedHashMap<>();
        private String stacktrace;
        private int count = 0;
        private long minId = Long.MAX_VALUE;

        private StackTrace(String stacktrace) {
            this.stacktrace = stacktrace;
        }

        private void add(Thread thread) {
            Thread.State state = thread.getState();
            Set<Thread> threads = this.threads.get(state);
            if (threads == null) {
                threads = new LinkedHashSet<>();
                this.threads.put(state, threads);
            }
            threads.add(thread);
            count++;
            long id = thread.getId();
            if (id < this.minId) {
                this.minId = id;
            }
        }

        @Override
        public int compareTo(StackTrace other) {
            if (this.count != other.count) {
                return this.count < other.count ? -1 : 1;

            } else {
                return this.minId < other.minId ? -1 : 1;
            }
        }

        public String toString() {
            return this.toString(null);
        }


        private Set<Thread> intersection;

        /**
         * Produces a stack trace analysis that focuses on the provided set of threads
         *
         * @param include the include
         * @return the string
         */
        public String toString(Set<Thread> include) {
            String str = "THREADS:";

            for (Map.Entry<Thread.State, Set<Thread>> entry : this.threads.entrySet()) {
                str += "\n\t" + entry.getKey() + " : ";
                Set<Thread> threads = entry.getValue();
                str += threads.size();
                if (threads.size() <= 10) {
                    str += " " + threads;
                    continue;

                } else if (include == null) {
                    continue;
                }

                this.intersection = new LinkedHashSet<>();

                for (Thread thread : threads) {
                    if (include.contains(thread)) {
                        intersection.add(thread);
                    }
                }

                if (intersection.size() > 0) {
                    str += " " + intersection;

                } else {
                    intersection = null;
                }

            }
            return str + "\n" + this.stacktrace;
        }
    }
}
