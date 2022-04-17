package sync;

import data.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.apache.logging.log4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.core.config.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(Parameterized.class)
public class SyncMultiTypeWrapper {
    public static final Logger logger = LogManager.getLogger(SyncMultiTypeWrapper.class);

    public static final int THREADS = 8192;
    public static final int WARM_UP_ITERATIONS = 64;
    public static final int RANDOM_INSTANCES = 64;
    public static final int RANDOM_INVALID_INSTANCES = 32; //invalid dbString
            // per-thread... number of random instances created with "RANDOM_LAMBDAS"


    public static <K, V> LinkedHashMap<K, V> randomizeMapOrder(Map<K, V> orig) {
        LinkedHashMap<K, V> randomized = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entries = new LinkedList(orig.entrySet());

        while(entries.size() > 0) {
            int i = (int)Math.floor(Math.random() * entries.size());
            Map.Entry<K, V> entry = entries.remove(i);
            randomized.put(entry.getKey(), entry.getValue());
        }
        return randomized;
    }

    public static final List<Lambda0<Object>> RANDOM_LAMBDAS = List.of(
            () -> {
                    int len = (int)Math.floor(Math.pow((Math.random() * 15.5) + 0.5, 2));
                    char[] chars = new char[len];
                    for (int i = 0; i < len; i++) {
                        chars[i] = (char)(int)Math.floor(32 + Math.random() * 95);
                    }
                    return new String(chars);
                },
            () -> ThreadLocalRandom.current().nextInt(),
            () -> ThreadLocalRandom.current().nextBoolean(),
            () -> {
                int exp = ThreadLocalRandom.current().nextInt(-323, 308);
                double sig = Math.random();
                while (sig < 1) {
                    sig *= 10;
                }
                return Double.valueOf(sig + "E" + exp);
            },
            () -> ThreadLocalRandom.current().nextLong(),
            () -> (byte)ThreadLocalRandom.current().nextInt(-128, 128),
            () -> (short)ThreadLocalRandom.current().nextInt(-32768, 32768),
            () -> {
                int exp = ThreadLocalRandom.current().nextInt(-44, 38);
                double sig = Math.random();
                while (sig < 1) {
                    sig *= 10;
                }
                return Float.valueOf(sig + "E" + exp);
            },
            () -> {
                int len = ThreadLocalRandom.current().nextInt(15, 256);
                String str = "" + ThreadLocalRandom.current().nextInt(1, 10);
                for (int i = 1; i < len; i++) {
                    str += ThreadLocalRandom.current().nextInt(10);
                }
                return new BigInteger(str);
            },
            () -> {
                int lenLeft = ThreadLocalRandom.current().nextInt(15, 256);
                int lenRight = ThreadLocalRandom.current().nextInt(15, 256);
                String str = "" + ThreadLocalRandom.current().nextInt(1, 10);
                for (int i = 1; i < lenLeft; i++) {
                    str += ThreadLocalRandom.current().nextInt(10);
                }
                str += ".";

                for (int i = 1; i < lenRight; i++) {
                    str += ThreadLocalRandom.current().nextInt(10);
                }
                str += ThreadLocalRandom.current().nextInt(1, 10);

                return new BigDecimal(str);
            }
        );


    private static Collection<Lambda0[]> params = List.of(new Lambda0[][] {
        {
                /**
                 * TEST 1.  Default params from TestMultiTypeWrapper, but with a randomized order
                */
            () -> randomizeMapOrder(TestMultiTypeWrapper.TEST_BOTH_DIRECTIONS),
            () -> randomizeMapOrder(TestMultiTypeWrapper.TEST_FROM_DB),
            () -> randomizeMapOrder(TestMultiTypeWrapper.TEST_TO_DB),
            null

        }, {
            /**
             * TEST 2.  Random values of various types, including both valid and invalid dbStrings
            */

            () -> {
                // Produces random valid instances of the various types
                Map<String, Object> instances = new LinkedHashMap();
                int numLambdas = RANDOM_LAMBDAS.size();
                for (int i = 0; i < RANDOM_INSTANCES; i++) {
                    Lambda0 rand = RANDOM_LAMBDAS.get(ThreadLocalRandom.current().nextInt(0, numLambdas));
                    Object instance = rand.call();
                    String dbString = MultiTypeWrapper.makeDbStringFor(instance);
                    instances.put(dbString, instance);
                }
                return instances;
            },
            new Lambda0<Map<String, Object>>() {
                // Creates Invalid dbStrings
                private final Lambda0 strLambda = RANDOM_LAMBDAS.get(0);
                private final List<Character> types = makeTypes();
                private final int size = types.size();
                private final ThreadLocalRandom rand = ThreadLocalRandom.current();

                private List<Character> makeTypes() {
                    List<Character> types = new ArrayList(MultiTypeWrapper.TYPE_MAP.keySet());
                    types.remove(Character.valueOf('S')); //remove String
                    return types;
                }

                public Map<String, Object> call() {
                    Map<String, Object> map = new LinkedHashMap<>();

                    for (int i = 0; i < RANDOM_INVALID_INSTANCES / 2; i++) {
                        String str = "";
                        Object o = null;

                        // 30% chance that it will be a valid type... constructor String will still be invalid though
                        if (Math.random() < 0.3) {
                            str += types.get((int)Math.floor(Math.random() * size));
                            if (str.equals("B")) { //Boolean constructor creates "False" instance from invalid constructor strings
                                o = Boolean.FALSE;
                            }

                        } else {
                            char c;
                            do {
                                c = (char) (rand.nextInt(' ', '~' + 1));

                            } while (types.contains(c) || c == 'S');

                            str += c;
                        }
                        str += strLambda.call();

                        MultiTypeWrapper test = MultiTypeWrapper.buildFrom(str);
                        if (test.getObject() instanceof MultiTypeWrapper  //means the string was invalid or of type "U" (Valid but unknown type)
                                || test.getObject().equals(Boolean.FALSE)) {
                            map.put(str, o);

                        } else {
                            i--; //occasionally, a random non-Boolean string produces a valid MultiTypeWrapper...
                        }
                    }

                    return map;
                }
            },
            null,
            () -> Map.of(MultiTypeWrapper.class, Level.FATAL)
                    //gets very noisy with all the error messages from invalid dbStrings on this test
        }
    });

    @Parameterized.Parameters
    public static Collection getTestParams() {
        return params;
    }


    private final Lambda0<Map<String, Object>> both;
    private final Lambda0<Map<String, Object>> from;
    private final Lambda0<Map<Object, String>> to;
    private final Map<Class, Level> targetLoggingLevels;
    private final Map<String, Level> origLoggingLevels;

    private List<TestMultiTypeWrapper> warmUpTests = new ArrayList(WARM_UP_ITERATIONS);
    private List<TestMultiTypeWrapper> tests = new ArrayList(THREADS);

    private List<Runnable> warmups = new ArrayList(WARM_UP_ITERATIONS);
    private List<Thread> staged = new ArrayList(THREADS);
    private List<Throwable> exceptions = new LinkedList<>();
    long neededCount = 0; // instances count after first iteration of warm-up,
    // tells us the number of instances needed per-thread.

    public SyncMultiTypeWrapper(Lambda0<Map<String, Object>> both,
                                Lambda0<Map<String, Object>> from,
                                Lambda0<Map<Object, String>> to,
                                Lambda0<Map<Class, Level>> loggingLevels) {
        this.both = both;
        this.from = from;
        this.to = to;

        if (loggingLevels != null) {
            this.targetLoggingLevels = loggingLevels.call();
            if (this.targetLoggingLevels != null) {
                this.origLoggingLevels = new HashMap<>();
            } else {
                this.origLoggingLevels = null;
            }

        } else {
            this.targetLoggingLevels = null;
            this.origLoggingLevels = null;
        }
    }

    @Before
    public void setTargetLoggingLevels() {
        if (this.targetLoggingLevels == null) return;

        logger.info("Setting logging levels:\n" + targetLoggingLevels.toString());

        for (Map.Entry<Class, Level> entry : this.targetLoggingLevels.entrySet()) {
            Class type = entry.getKey();
            Level level = entry.getValue();
            Logger logger = LogManager.getLogger(type);

            String typeStr = logger.getName();
            Level origLevel = logger.getLevel();

            this.origLoggingLevels.put(typeStr, origLevel);
            Configurator.setLevel(typeStr, level);
        }
    }

    @After
    public void restoreDefaultLoggingLevels() {
        if (this.origLoggingLevels == null) return;
        Configurator.setLevel(this.origLoggingLevels);
        logger.info("Restored default logging levels:\n" + origLoggingLevels.toString());
    }

    public void logCounter() {
        logger.info("\nConstructed instances counter for MultiTypeWrapper: " + MultiTypeWrapper.getCounter()
                    + "\nAlive instances in weak map: " + MultiTypeWrapper.size());
    }

    @Test
    public void run() throws InterruptedException {
        long startTime;
        long nanos;
        double seconds;

        startTime = System.nanoTime();

        this.stageTests();

        nanos = System.nanoTime() - startTime;
        seconds = (double)nanos / 1000000000;

        logger.info("\nSTAGING TIME:\nNanoseconds:  " + nanos
                                    + "\n    Seconds: "  + seconds);


        reset();
        rewrapTestParams(this.warmUpTests);

        int size = warmups.size();

        startTime = System.nanoTime();

        runWarmUps();

        nanos = System.nanoTime() - startTime;
        seconds = (double)nanos / 1000000000;

        long nAvg = nanos / size;
        double sAvg = seconds / size;
        long count = MultiTypeWrapper.getCounter();
        long lost = count - neededCount;
        double perIteration = (double)lost / size;

        logger.info("MultiTypeWrapper counter, after single-threaded JVM warm-up:\n"
                + "  Total: " + count + "\n"
                + " Needed: " + neededCount +  " per run\n"
                + "   Lost: " + lost
                + "  or  " + perIteration + " per iteration, on average\n"
                + "Iterations: " + size + "\n\n"
                + "WARM-UP TIME:\n"
                + "\nNanoseconds:  " + nanos
                + "\n    Seconds: " + seconds
                + "\nAverage per thread: " + nAvg + " nanoseconds / " + sAvg + " seconds"
                + "\n\n\n\n"
            );

        reset();
        rewrapTestParams(this.tests);

        logger.info("\n\n\n\n#######################   STARTING MULTI-THREADED TEST #########################\n\n"
                + THREADS + " independent threads will be spun off\n\n\n\n");

        size = this.staged.size();

        startTime = System.nanoTime();

        startThreads();

        nanos = System.nanoTime() - startTime;
        seconds = (double)nanos / 1000000000;

        nAvg = nanos / size;
        sAvg = seconds / size;

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

    private void stageTests() {

        for (int i = 0; i < WARM_UP_ITERATIONS + THREADS; i++) {
            TestMultiTypeWrapper test = new TestMultiTypeWrapper();
            test.printInfo = false;

            if (both != null) {
                test.testBothDirections = both.call();
            } else {
                test.testBothDirections = null;
            }

            if (from != null) {
                test.testFromDb = from.call();
            } else {
                test.testFromDb = null;
            }

            if (to != null) {
                test.testToDb = to.call();
            } else {
                test.testToDb = null;
            }


            Integer number = i - WARM_UP_ITERATIONS;
            Thread[] thread = new Thread[]{null};

            Runnable executor = () -> {
                List<Runnable> tests = new ArrayList<Runnable>(List.of(
                        () -> test.testBothDirections(),
                        () -> test.testToDb(),
                        () -> test.testFromDb()
                ));

                while (tests.size() > 0) {
                    int t = (int) Math.floor(Math.random() * tests.size());
                    try {
                        tests.remove(t).run();

                    } catch (Throwable e) {
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
                warmUpTests.add(test);
                warmups.add(executor);

            } else {
                this.tests.add(test);
                thread[0] = new Thread(executor);
                this.staged.add(thread[0]);
            }
        }
    }



    public void runWarmUps() {
        //run a few iterations in the main thread, just to warm up the JVM
        for (Runnable warmup : warmups) {
            warmup.run();

            if (neededCount == 0) {
                neededCount = MultiTypeWrapper.getCounter();
            }
        }
    }


    private static void reset() throws InterruptedException {
        logger.info("Sleeping for 5 seconds before resetting MultiTypeWrapper ...\n\n");

        Thread.sleep(5000);

        logger.info("\nCounter after 5 seconds sleep:\n" + MultiTypeWrapper.getCounter()
                + "\nNow resetting instance and counter... ");

        MultiTypeWrapper.clearInstances();
        MultiTypeWrapper.resetCounter();
        needsRewrap = true;

        logger.info("\n...Reset complete\nWaiting 5 more seconds to allow for garbage collection...\n");

        Thread.sleep(5000);

    }

    private static boolean needsRewrap = false;

    private static void rewrapTestParams(List<TestMultiTypeWrapper> tests) {
        for (TestMultiTypeWrapper test : tests) {
            for (Map<String, Object> map : new Map[] {
                                                test.testBothDirections,
                                                test.testFromDb }) {
                if (map == null) continue;

                for (Map.Entry<String, Object> entry :
                        new ArrayList<Map.Entry<String, Object>>(map.entrySet())) {

                    Object o = entry.getValue();
                    if (o instanceof MultiTypeWrapper) {
                        map.put(entry.getKey(), rewrap((MultiTypeWrapper) o));
                    }
                }
            }
            if (test.testToDb == null) continue;

            for (Object o : new LinkedHashSet(test.testToDb.keySet())) {
                if (o instanceof MultiTypeWrapper) {
                    String str = test.testToDb.get(o);
                    test.testToDb.remove(o);
                    test.testToDb.put(rewrap((MultiTypeWrapper) o), str);
                }
            }
        }
        needsRewrap = false;
    }

    private static MultiTypeWrapper rewrap(MultiTypeWrapper wrapper) {
        Object origObj = wrapper.getObject();
        if (origObj instanceof MultiTypeWrapper) {
            return MultiTypeWrapper.buildFrom(wrapper.toDb());
        } else  {
            return MultiTypeWrapper.wrap(origObj);
        }
    }

    private void startThreads() {
        synchronized (staged) {
            for (Thread test : staged) {
                test.start();
            }
        }

        // monitoring/logging thread ...
        new Thread(() -> {
            synchronized (staged) {
                logger.info("Threads: " + staged.size() + "  Instances: " + MultiTypeWrapper.getCounter());
                if (staged.size() <= 0) {
                    return;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }).start();

        // keep the main thread waiting for the worker threads to exit,
        // so the timer can be stopped as soon as the last one is done
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

                } catch (Throwable e) {
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
    }
}
