package nessusTools.run;

import nessusTools.sync.*;
import nessusTools.util.*;
import org.apache.logging.log4j.*;

import java.util.*;

public class JobFactory {
    public static final long MAX_NEW_JOB_PROCESSING_TIME_MS = 1000;
    public static final int NUM_WORKER_THREADS = 10;
    public static final long MAX_MAIN_WAIT_TIME = 300000;


    private final WorkerThread thread;
    private boolean kill = false;

    private JobFactory() {
        synchronized (this) {
            thread = new WorkerThread(this);
            workerThreads.put(thread, this);
        }
    }

    private void checkThread() {
        if (!Objects.equals(Thread.currentThread(), thread)) {
            throw new IllegalStateException("Job Factory can only be accessed on the thread to which it" +
                    "was given!\nExpected: " + thread + "\nActual: " + thread);
        }
    }

    public synchronized Job getNextJob() {
        checkThread();
        Job job = null;
        synchronized (readyJobs) {
            while (true) {
                for (Iterator<Job> iterator = readyJobs.iterator();
                     iterator.hasNext(); ) {

                    job = iterator.next();
                    iterator.remove();
                    if (job != null) {
                        synchronized (underwayJobs) {
                            underwayJobs.add(job);
                        }
                        break;
                    }
                }
                if (job != null) break;

                try {
                    readyJobs.wait();

                } catch (InterruptedException e) {
                    if (kill) {
                        throw new ThreadDeath();
                    }
                }
            }
        }

        Job j = job;
        Job.Accessor accessor = accessors.read(accessors -> accessors.get(j));
        if (accessor != null) {
            synchronized (jobDelayTimes) {
                jobDelayTimes[currentIndex++] = System.currentTimeMillis() - accessor.getTryAtTime();
                if (currentIndex >= jobDelayTimes.length) currentIndex = 0;
            }
        }
        return job;
    }



    public static void init(Var<Init> holder) {
        if (!Main.isMain()) {
            throw new IllegalStateException("Only the main thread can call JobFactory.init()");

        } else if (init != null) {
            throw new IllegalStateException("Can only call JobFactory.init() once");
        }

        init = new Init();
        holder.value = init;
    }

    private static Init init;

    static class Init {
        public void runJobsLoop(Job seed) {
            if (!Main.isMain()) {
                throw new IllegalStateException("Only the main thread can use JobFactory.Init");
            }
            newJobs.add(seed);
            haveNewJobsToProcess = true;
            JobFactory.runJobsLoop();
        }
    }


    private static final Logger logger = LogManager.getLogger(JobFactory.class);
    private static final Set<Job> newJobProviders = new LinkedHashSet<>();
    private static final Set<Job> newJobs = new LinkedHashSet<>();

    private static final ReadWriteLock<Map<Job, Job.Accessor>, Job.Accessor>
            accessors = ReadWriteLock.forMap(new WeakHashMap<>());

    private static final Map<Long, Job> delayedJobs = new TreeMap<>();
    private static final Set<Job> readyJobs = new LinkedHashSet<>();
    private static final Set<Job> underwayJobs = new LinkedHashSet<>();

    private static Map<WorkerThread, JobFactory> workerThreads = new LinkedHashMap<>();

    public static void notifyOfJobExit(Job source) {
        synchronized (newJobProviders) {
            newJobProviders.notifyAll();
        }
    }

    public static void notifyOfJobProvider(Job source) {
        synchronized (newJobProviders) {
            newJobProviders.add(source);
            newJobProviders.notifyAll();
        }
    }


    private static boolean haveNewJobsToProcess = false;
    private static boolean haveDelayedJobs = false;
    private static boolean haveUnderwayJobs = false;

    private static void runJobsLoop() {
        int exitChecks = 0;
        while (exitChecks < 2) {

            checkWorkerThreads();

            if (haveDelayedJobs) {
                processDelayedJobs();
                exitChecks = 0;
            }

            if (haveNewJobsToProcess) {
                processNewJobs();
                exitChecks = 0;
            }

            waitForNextJob();

            if (!(haveUnderwayJobs || haveDelayedJobs || haveNewJobsToProcess)) {
                exitChecks++;
            } else {
                exitChecks = 0;
            }

        }
        logger.info("No jobs left... exiting");
        System.exit(0);
    }


    private static void processNewJobs() {
        long start = System.currentTimeMillis();
        for (Iterator<Job> iterator = newJobs.iterator();
             iterator.hasNext(); ) {

            Job job = iterator.next();
            iterator.remove();

            if (job != null) {
                Job.Accessor accessor = job.getAccessor();
                if (accessor != null) {
                    accessors.write(accessors -> accessors.put(job, accessor));
                    long tryAtTime = accessor.getTryAtTime();
                    if (tryAtTime <= System.currentTimeMillis()) {
                        haveUnderwayJobs = true;
                        synchronized (readyJobs) {
                            readyJobs.add(job);
                            readyJobs.notify();
                        }
                    } else {
                        haveDelayedJobs = true;
                        while (delayedJobs.containsKey(tryAtTime)) {
                            tryAtTime++;
                        }
                        delayedJobs.put(tryAtTime, job);
                    }

                } else {
                    logger.error("Null accessor for new job!? " + job + " discarding, unable to execute");
                }
            } else {
                logger.error("Null new job!?");
            }


            if (System.currentTimeMillis() - start
                    > MAX_NEW_JOB_PROCESSING_TIME_MS) {
                break;
            }
        }
    }


    private static Long[] jobDelayTimes = new Long[16];
    private static int currentIndex = 0;

    private static long calcAvgDelay() {
        synchronized (jobDelayTimes) {
            long sum = 0;
            long number = 0;
            for (Long time : jobDelayTimes) {
                if (time != null) {
                    sum += time;
                    number++;

                }
            }
            if (number == 0) return 0;
            return sum / number;
        }
    }

    private static void processDelayedJobs() {
        long start = System.currentTimeMillis();
        long referenceTime = calcAvgDelay() + System.currentTimeMillis();
        for (Iterator<Map.Entry<Long, Job>> iterator = delayedJobs.entrySet().iterator();
             iterator.hasNext();) {

            Map.Entry<Long, Job> entry = iterator.next();
            Long tryAtTime = entry.getKey();
            Job job = entry.getValue();

            if (tryAtTime != null) {
                if (job != null) {
                    if (tryAtTime <= referenceTime) {
                        synchronized (readyJobs) {
                            haveUnderwayJobs = true;
                            readyJobs.add(job);
                            readyJobs.notify();
                        }
                        iterator.remove();
                    }
                } else {
                    logger.error("Null delayed job!?  tryAtTime: " + tryAtTime);
                }
            } else if (job != null) {
                logger.error("Delayed job with null tryAtTime key!? " + job);
                Job.Accessor accessor = accessors.read(accessors -> accessors.get(job));
                if (accessor != null) {
                    long tat = accessor.getTryAtTime();
                    while (delayedJobs.containsKey(tat)) {
                        tat++;
                    }
                    delayedJobs.put(tat, job);
                } else {
                    logger.error("Null accessor for delayed job!? " + job);
                }
            } else {
                logger.error("Null delayed job with null tryAtTime key!? " + job);
            }

            if (System.currentTimeMillis() - start
                    > MAX_NEW_JOB_PROCESSING_TIME_MS) {
                break;
            }
        }

        haveDelayedJobs = delayedJobs.size() > 0;
    }

    private static void processUnderwayJobs() {
        long start = System.currentTimeMillis();
        accessors.read(accessors ->{
            Set<Job> copy;
            synchronized (underwayJobs) {
                copy = new LinkedHashSet<>(underwayJobs);
            }

            for (Iterator<Job> iterator = copy.iterator();
                 iterator.hasNext(); ) {

                Job job = iterator.next();
                boolean remove = false;
                if (job != null) {
                    Job.Accessor accessor = accessors.get(job);
                    if (accessor != null) {

                        if (accessor.getStage() == Job.Stage.DONE) {
                            remove = true;

                        } else if (accessor.hasException()) {
                            remove = true;
                            long tryAtTime = accessor.getTryAtTime();
                            while (delayedJobs.containsKey(tryAtTime)) {
                                tryAtTime++;
                            }
                            delayedJobs.put(tryAtTime, job);
                            haveDelayedJobs = true;
                        }

                    } else {
                        logger.error("Null accessor for running job!? " + job + " discarding, unable to execute");
                        remove = true;
                    }
                } else {
                    logger.error("Null running job!?");
                    remove = true;
                }


                if (remove) {
                    synchronized (underwayJobs) {
                        underwayJobs.remove(job);
                    }
                }

                if (System.currentTimeMillis() - start
                        > MAX_NEW_JOB_PROCESSING_TIME_MS) {
                    break;
                }
            }
            return null;
        });


        synchronized (readyJobs) {
            if (readyJobs.size() > 0) {
                haveUnderwayJobs = true;
                return;
            }

            synchronized (underwayJobs) {
                if (underwayJobs.size() > 0) {
                    haveUnderwayJobs = true;
                    return;
                }

                synchronized (newJobProviders) {
                    haveUnderwayJobs = newJobProviders.size() > 0;
                }
            }
        }
    }

    //ONLY CALL WITH LOCK ON newJobProviders
    private static void fetchFromProviders() {
        if (newJobProviders.size() <= 0) {
            haveNewJobsToProcess = newJobs.size() > 0;
            return;
        }

        long start = System.currentTimeMillis();

        for (Iterator<Job> iterator = newJobProviders.iterator();
             iterator.hasNext(); ) {

            Job provider = iterator.next();
            iterator.remove();

            if (provider != null) {
                Job.Accessor accessor = accessors.read(accessors -> accessors.get(provider));
                if (accessor != null) {
                    Set<Job> jobs = accessor.getNewJobs();
                    newJobs.addAll(jobs);

                } else {
                    logger.error("Null accessor for running provider job " + provider);
                }
            } else {
                logger.error("Null running provider job!?");
            }

            if (System.currentTimeMillis() - start
                    > MAX_NEW_JOB_PROCESSING_TIME_MS) {
                break;
            }
        }

        haveNewJobsToProcess = newJobs.size() > 0;
    }


    private static void checkWorkerThreads() {
        workerThreads.keySet().removeIf(thread -> !thread.isAlive());

        while (workerThreads.size() < NUM_WORKER_THREADS) {
            JobFactory factory = new JobFactory();
            workerThreads.put(factory.thread, factory);
        }
    }

    private static void waitForNextJob() {
        long exitAt = System.currentTimeMillis() + MAX_MAIN_WAIT_TIME;
        while (System.currentTimeMillis() < exitAt) {

            processUnderwayJobs();

            synchronized (newJobProviders) {
                fetchFromProviders();
                if (haveNewJobsToProcess) return;
                if (haveDelayedJobs) break; // run sleepUntilDelayedJobs, but without lock on newJobProviders
                if (!haveUnderwayJobs) return; //probably means the application should exit


                checkWorkerThreads();

                try {
                    newJobProviders.wait(MAX_MAIN_WAIT_TIME);

                } catch (InterruptedException e) { }

                fetchFromProviders();
                if (haveNewJobsToProcess) return;
            }
        }
        sleepUntilDelayedJobs();
    }

    private static void sleepUntilDelayedJobs() {
        boolean haveDispatcedJobs = false;
        while (delayedJobs.size() > 0) {
            Job job = null;
            Job.Accessor accessor = null;

            //restart the iterator each time
            Long mappedTryAtTime = null;
            for (Iterator<Map.Entry<Long, Job>> iterator = delayedJobs.entrySet().iterator();
                 iterator.hasNext(); ) {

                Map.Entry<Long, Job> entry = iterator.next();
                mappedTryAtTime = entry.getKey();
                Job j = entry.getValue();
                if (j == null) {
                    iterator.remove();
                    continue;
                }

                accessor = accessors.read(accessors -> accessors.get(j));
                if (accessor == null) {
                    iterator.remove();
                    continue;
                }

                job = j;
                break;
            }

            if (job == null || accessor == null) return;

            assert mappedTryAtTime != null;

            long tryAtTime = accessor.getTryAtTime();

            if (tryAtTime != mappedTryAtTime) {
                delayedJobs.remove(mappedTryAtTime);
                delayedJobs.put(tryAtTime, job);
                if (tryAtTime > mappedTryAtTime) continue;
            }

            long sleepTime = tryAtTime - calcAvgDelay() - System.currentTimeMillis();

            if (sleepTime > 0) {
                if (haveDispatcedJobs) return;

                try {
                    synchronized (newJobProviders) {
                        if (newJobProviders.size() > 0) return;

                        checkWorkerThreads();
                        newJobProviders.wait(Math.min(sleepTime, MAX_MAIN_WAIT_TIME));

                        if (accessor.getTryAtTime() > calcAvgDelay() + System.currentTimeMillis()) {
                            // premature waking-up of thread probably means a new jobProvider or job done
                            //... OR MAX_WAIT_TIME was exceeded
                            fetchFromProviders();
                            return;
                        }
                    }

                } catch (InterruptedException e) {
                }
            }
            haveDispatcedJobs = true;
            haveUnderwayJobs = true;
            delayedJobs.remove(tryAtTime);
            synchronized (readyJobs) {
                readyJobs.add(job);
                readyJobs.notify();
            }
        }

    }
}
