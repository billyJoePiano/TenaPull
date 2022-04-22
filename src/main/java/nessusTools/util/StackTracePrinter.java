package nessusTools.util;

import nessusTools.sync.*;

import java.time.*;
import java.util.*;

public class StackTracePrinter extends Thread {
    private static boolean started = false;

    private int delay;

    private StackTracePrinter(int delay) {
        this.delay = delay;
    }

    public static void startThread() {
        new StackTracePrinter(0).start();
    }

    public static void startThread(int delay) {
        new StackTracePrinter(delay).start();
    }

    StackTracePrinter() {
        super("StackTracePrinter");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        if (this.delay > 0) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        print();
    }

    public static void print() {
        System.err.println("STARTING STACK TRACE PRINTER ...");

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

    public static String makeStackTraceString() {
        return makeStackTraceString(Thread.currentThread().getStackTrace(), "\n");
    }

    public static String makeStackTraceString(Thread thread) {
        return makeStackTraceString(thread.getStackTrace(), "\n");
    }

    public static String makeStackTraceString(StackTraceElement[] elements) {
        return makeStackTraceString(elements, "\n");
    }

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
        Map<Thread.State, Set<Thread>> threads = new LinkedHashMap<>();
        String stacktrace;
        int count = 0;
        long minId = Long.MAX_VALUE;

        StackTrace(String stacktrace) {
            this.stacktrace = stacktrace;
        }

        public void add(Thread thread) {
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
            return str + this.stacktrace;
        }

        public Thread.State getThreadState(Thread thread) {
            for (Map.Entry<Thread.State, Set<Thread>> entry : this.threads.entrySet()) {
                if (entry.getValue().contains(thread)) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }
}
