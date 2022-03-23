package sync;

import data.*;
import nessusTools.data.entity.*;
import nessusTools.data.persistence.*;
import net.bytebuddy.implementation.bytecode.*;
import org.apache.logging.log4j.*;
import org.junit.*;
import org.junit.Test;
import org.junit.jupiter.api.*;

import java.math.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SyncMultiTypeWrapper {
    public static final Logger logger = LogManager.getLogger(SyncMultiTypeWrapper.class);

    public static final int THREADS = 1024;
    public static final int WARM_UP_ITERATIONS = 64;

    public SyncMultiTypeWrapper() { }

    public <K, V> LinkedHashMap<K, V> randomizeMapOrder(Map<K, V> orig) {
        LinkedHashMap<K, V> randomized = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entries = new LinkedList(orig.entrySet());

        while(entries.size() > 0) {
            int i = (int)Math.floor(Math.random() * entries.size());
            Map.Entry<K, V> entry = entries.remove(i);
            randomized.put(entry.getKey(), entry.getValue());
        }
        return randomized;
    }

    public void logCounter() {
        logger.info("Constructed instances counter for MultiTypeWrapper: " + MultiTypeWrapper.getCounter());
    }

    @Test
    public void startThreads() throws InterruptedException {
        long startTime = System.nanoTime();
        long endTime;
        long nanos;
        double seconds;

        List<Runnable> warmups = new ArrayList(WARM_UP_ITERATIONS);
        List<Thread> staged = new ArrayList(THREADS);
        List<Throwable> exceptions = new LinkedList<>();

        for (int i = 0; i < WARM_UP_ITERATIONS + THREADS; i++) {
            TestMultiTypeWrapper test = new TestMultiTypeWrapper();
            if (i != 0) {
                test.testBothDirections = randomizeMapOrder(TestMultiTypeWrapper.TEST_BOTH_DIRECTIONS);
                test.testFromDb = randomizeMapOrder(TestMultiTypeWrapper.TEST_FROM_DB);
                test.testToDb = randomizeMapOrder(TestMultiTypeWrapper.TEST_TO_DB);
            }
            test.printInfo = false;


            Integer number = i - WARM_UP_ITERATIONS;
            Thread[] thread = new Thread[] { null };

            Runnable executor = () -> {
                List<Runnable> tests = new ArrayList<Runnable>(List.of(
                        () -> test.testBothDirections(),
                        () -> test.testToDb(),
                        () -> test.testFromDb()
                    ));

                while (tests.size() > 0) {
                    int t = (int)Math.floor(Math.random() * tests.size());
                    try {
                        tests.remove(t).run();

                    } catch(Throwable e) {
                        logger.error(e);
                        if (thread[0] != null) {
                            // don't count exceptions unless they are part of the multi-threaded test
                            synchronized (exceptions) {
                                exceptions.add(e);
                            }
                            fail(e);
                        }
                    }
                }

                if (thread[0] == null) return;
                synchronized (staged) {
                    staged.remove(thread[0]);
                    int size = staged.size();
                    logger.info("Finished thread # " + number
                            + "\t" + size + " remaining");


                    if (size == 255 || size % 50 == 0) {
                        logCounter();
                    }
                }
            };

            if (i < WARM_UP_ITERATIONS) {
                warmups.add(executor);

            } else {
                thread[0] = new Thread(executor);
                staged.add(thread[0]);
            }
        }

        nanos = (endTime = System.nanoTime()) - startTime;
        seconds = (double)nanos / 1000000000;

        logger.info("\nSTAGING TIME:\nNanoseconds: " + nanos + "\nSeconds:" + seconds);

        startTime = System.nanoTime();


        //run a few iterations in the main thread, just to warm up the JVM
        long neededCount = 0;
        for (Runnable warmup : warmups) {
            warmup.run();

            if (neededCount == 0) {
                neededCount = MultiTypeWrapper.getCounter();
            }
        }

        nanos = (endTime = System.nanoTime()) - startTime;
        seconds = (double)nanos / 1000000000;

        logger.info("\nWARM-UP TIME:\nNanoseconds: " + nanos + "\nSeconds:" + seconds);

        long count = MultiTypeWrapper.getCounter();
        long lost = count - neededCount;
        double perIteration = (double)lost / WARM_UP_ITERATIONS;

        logger.info("MultiTypeWrapper counter, after single-threaded JVM warm-up:\n"
                        + "  Total: " + count + "\n"
                        + " Needed: " + neededCount +  " per run\n"
                        + "   Lost: " + lost
                        + "  or  " + perIteration + " per iteration, on average\n"
                        + "Iterations: " + WARM_UP_ITERATIONS + "\n\n\n"
                        + "Sleeping for 5 seconds before resetting MultiTypeWrapper ...\n\n");

        Thread.sleep(5000);

        logger.info("\nCounter after 5 seconds sleep:\n" + MultiTypeWrapper.getCounter()
                + "\nNow resetting counter and instances to zero");

        MultiTypeWrapper.clearInstances();
        MultiTypeWrapper.resetCounter();

        Thread.sleep(1000);
        logger.info("\n\n\n\n#######################   STARTING MULTI-THREADED TEST #########################\n\n"
                + THREADS + " independent threads will be spun off\n\n\n\n");

        startTime = System.nanoTime();

        TestMultiTypeWrapper.TEST_BOTH_DIRECTIONS.put(
                "Uclass nessusTools.data.entity.ScanType\nthis is a test",
                MultiTypeWrapper.wrap(TestMultiTypeWrapper.createUnknownType()));

        synchronized (staged) {
            for (Thread test : staged) {
                test.start();
            }
        }

        while (true) {
            Thread test;
            synchronized (staged) {
                if (staged.size() <= 0) {
                    break;
                }
                test = staged.get(0);
            }

            if (test.isAlive()) {
                try {
                    test.join();

                } catch(Throwable e) {
                    synchronized (staged) {
                        staged.remove(test);
                    }
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }

            } else {
                synchronized (staged) {
                    staged.remove(test);
                }
            }
        }

        nanos = (endTime = System.nanoTime()) - startTime;
        seconds = (double)nanos / 1000000000;
        long nAvg = nanos / THREADS;
        double sAvg = seconds / THREADS;

        logger.info("\nMULTI-THREADED TEST COMPLETION TIME:"
                    + "\nNanoseconds:  " + nanos
                    + "\n    Seconds: " + seconds
                    + "\nAverage per thread: " + nAvg + " nanoseconds / " + sAvg + " seconds"
                    + "\nMultiTypeWrapper instance counter: " + MultiTypeWrapper.getCounter()
                    + "\nNeeded instances to complete a thread: " + neededCount);

        if (exceptions.size() > 0) {
            logger.error("Exceptions list had " + exceptions.size() + " throwable(s) present:\n"
                    + exceptions.toString());

            fail();
        }
    }
}
