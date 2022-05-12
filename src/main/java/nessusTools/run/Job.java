package nessusTools.run;

import nessusTools.client.*;
import org.apache.logging.log4j.*;

import java.util.*;

/**
 * Represents a Job that will be queued by the JobFactory and run by a WorkerThread.
 * Many of the critical internal methods of a job can only be accessed using its
 * accessor, which will only be provided to the Main thread once.
 */
public abstract class Job {
    /**
     * The constant DEFAULT_TRY_AGAIN_TIME.
     */
    public static long DEFAULT_TRY_AGAIN_TIME = 600000;
    private static Logger logger = LogManager.getLogger(Job.class);

    /**
     * The enum Stage, which represents the step in the job process which the jobs is at.
     */
    public enum Stage {
        /**
         * Idle stage.  A job will remain at the idle stage until isReady() returns true,
         * or if it fails first.
         */
        IDLE,
        /**
         * Fetch stage.
         */
        FETCH,
        /**
         * Process stage.
         */
        PROCESS,
        /**
         * Output stage.
         */
        OUTPUT,
        /**
         * Done stage, when a job is finished, OR has been marked as failed and then exited
         */
        DONE
    }

    private final Accessor accessor = new Accessor();
    private boolean accessorDelivered = false;
    private WorkerThread runThread;
    private Stage stage = Stage.IDLE;
    private boolean exception = false;
    private boolean failed = false;
    private long tryAtTime;

    /**
     * Instantiates a new Job with a wait timer of zero
     */
    protected Job() {
        this(0);
    }

    /**
     * Instantiates a new Job, with a wait timer of the provided milliseconds
     *
     * @param waitMs the wait ms
     */
    protected Job(long waitMs) {
        this.tryAtTime = System.currentTimeMillis() + waitMs;
    }

    /**
     * Is ready boolean.  Indicates whether the job is ready to run yet.  If false is returned,
     * the job will be placed in the JobFactory's waitingJobs queue, with either the default wait
     * time, or another wait time if a new one is set before returning false from isReady.
     *
     * The job will remain in the IDLE stage until isReady returns true, or the job is marked
     * as failed.  Failed jobs are not marked as DONE until after the method has returned
     *
     * @return the boolean
     */
    abstract protected boolean isReady();

    /**
     * Fetch any API resources, using the NessusClient provided by the worker thread.
     *
     * The job will remain in the FETCH stage until fetch returns without an exception,
     * or the job is marked as failed.  Failed jobs are not marked as DONE until after
     * the method has returned
     *
     * @param client the nessus client provided by the worker thread which is running this job
     * @throws Exception any relevant exception which the job implementation may need to throw
     */
    abstract protected void fetch(NessusClient client) throws Exception;

    /**
     * Perform any processing of the fetched data
     *
     * The job will remain in the PROCESS stage until fetch returns without an exception,
     * or the job is marked as failed.  Failed jobs are not marked as DONE until after
     * the method has returned
     *
     * @throws Exception any relevant exception which the job implementation may need to throw
     */
    abstract protected void process() throws Exception;

    /**
     * Perform any outputting of the processed data
     *
     * The job will remain in the OUTPUT stage until fetch returns without an exception,
     * or the job is marked as failed.  Failed jobs are not marked as DONE until after
     * the method has returned
     *
     * @throws Exception any relevant exception which the job implementation may need to throw
     */
    abstract protected void output() throws Exception;

    /**
     * Handles any exceptions thrown by isReady, fetch, process, and output methods.  Return
     * true if the operation should be attempted again immediately.  Mark as failed to permanently
     * end the job.  Otherwise, the job will be placed in the delayedJobs queue, and put back into
     * readyJobs once its wait time is up
     *
     * @param e     the e
     * @param stage the stage
     * @return the boolean
     */
    abstract protected boolean exceptionHandler(Exception e, Stage stage);

    /**
     * Can be called by other jobs/worker threads while waiting for this job to finish.  The other
     * thread/job will be awoken when this job either returns from the output method or is marked as
     * failed.  When marked as failed, the awakening won't happen until the job's method has returned
     */
    public synchronized void waitForExit() {
        while (this.stage == Stage.IDLE) {
            try {
                this.wait();
            } catch (InterruptedException e) { }
        }
    }

    /**
     * Marks the job as failed, so that it will be permanently removed from the jobs queue after
     * returning from the calling method.
     *
     * This method MAY ONLY BE INVOKED within the WorkerThread running
     * the job WHILE the job is underway in one of the processing methods (isReady, fetch, process,
     * or output)
     *
     * @throws IllegalStateException if this method is not being called from within one of the processing
     * methods by the established WorkerThread
     */
    protected final void failed() throws IllegalStateException {
        checkForRunThread();
        this.failed = true;
    }

    /**
     * Overrides the default tryAgainIn for a job which may have had an exception or will be
     * returning false from the isReady method
     *
     * This method MAY ONLY BE INVOKED within the WorkerThread running the job WHILE the job
     * is underway in one of the processing methods (isReady, fetch, process, or output)
     *
     * @throws IllegalStateException if this method is not being called from within one of the processing
     * methods by the established WorkerThread
     *
     * @param ms the ms to delay until the job is placed back in the readyJobs queue
     */
    protected final void tryAgainIn(long ms) {
        checkForRunThread();
        if (ms < 0) ms = 0;
        this.tryAtTime = System.currentTimeMillis() + ms;
    }


    /**
     * Gets the Stage which the job is at
     *
     * @return the stage
     */
    public final Stage getStage() {
        return this.stage;
    }

    /**
     * Adds a new job to the readyJobs queue or delayedJobs queue (depending on its tryAtTime).
     * Note that other than Main's seed job, this is the ONLY way to add new jobs.
     *
     * This method MAY ONLY BE INVOKED within the WorkerThread running the job WHILE the job
     * is underway in one of the processing methods (isReady, fetch, process, or output)
     *
     * @throws IllegalStateException if this method is not being called from within one of the processing
     * methods by the established WorkerThread
     *
     * @param newJob the new job to add
     */
    protected void addJob(Job newJob) {
        checkForRunThread();
        if (newJob == null) return;
        accessor.addJob(newJob);
        JobFactory.notifyOfJobProvider(this);
    }

    /**
     * Adds a list of new jobs to the readyJobs queue or delayedJobs queue (depending on their tryAtTime).
     * Note that other than Main's seed job, this is the ONLY way to add new jobs.
     *
     * This method MAY ONLY BE INVOKED within the WorkerThread running the job WHILE the job
     * is underway in one of the processing methods (isReady, fetch, process, or output)
     *
     * @throws IllegalStateException if this method is not being called from within one of the processing
     * methods by the established WorkerThread
     *
     * @param newJobs the list of new jobs
     */
    protected final void addJobs(Collection<Job> newJobs) {
        checkForRunThread();
        if (newJobs == null) return;
        accessor.addJobs(newJobs);
        JobFactory.notifyOfJobProvider(this);
    }

    /**
     * Start the job.  This method may only be called by the WorkerThread running the job, which must provide
     * itself and its NessusClient instances.
     *
     * @param runThread the run thread
     * @param client    the nessus client to be used by the fetch method
     * @throws IllegalStateException if the runThread does not match the current thread, if there is already
     * a runThread underway, or if the job was marked as failed or DONE
     */
    public final void start(WorkerThread runThread, NessusClient client) throws IllegalStateException {
        synchronized (accessor) {
            if (this.failed) {
                throw new IllegalStateException("Cannot call start() on a job which was marked as failed");
            } else if (this.runThread != null) {
                throw new IllegalStateException("Only one thread can call start() on a job at a time\n" +
                        "Run thread " + this.runThread + "\nCurrent thread: " + Thread.currentThread());

            } else if (this.stage == Stage.DONE) {
                throw new IllegalStateException(
                        "Cannot invoke start() on a job that is already done!\n" + this.getClass()
                                + "\n" + Thread.currentThread());

            } else if (runThread != null && !Objects.equals(runThread, Thread.currentThread())) {
                throw new IllegalStateException("Jobs must be run with a WorkerThread, not " + runThread);
            }

            this.runThread = runThread;
            this.exception = false;
        }

        try {
            this.runStages(client);

        } finally {
            synchronized (accessor) {
                this.runThread = null;
            }
            this.notifyOfExit();
            JobFactory.notifyOfJobExit(this);
        }
    }

    private final synchronized void runStages(NessusClient client) {
        long tryTime = this.tryAtTime;
        try {
            while (this.stage.ordinal() < Stage.DONE.ordinal()) {
                try {
                    if (this.failed) throw new JobFailedException();
                    switch (this.stage) {
                        case IDLE:
                            if (!this.isReady()) {
                                throw new JobNotReadyException();
                            }
                            this.stage = Stage.FETCH;
                            break;

                        case FETCH:
                            this.fetch(client);
                            this.stage = Stage.PROCESS;
                            break;

                        case PROCESS:
                            this.process();
                            this.stage = Stage.OUTPUT;
                            break;

                        case OUTPUT:
                            this.output();
                            this.stage = Stage.DONE;

                    }

                } catch (Exception e) {
                    if (!(e instanceof JobException)) {
                        if (exceptionHandler(e, this.stage)
                                && !this.failed) {
                            continue;
                        }
                    }
                    this.exception = true;
                    return;
                }
            }

        } finally {
            if (this.failed || !this.exception) {
                this.stage = Stage.DONE;
            }
            this.notifyAll();
        }
    }

    /**
     * A method that will always be called whenever a job exits, due either the job returning false
     * from isReady, an exception being thrown, or due to the job finishing.  This method may be
     * overridden by subclass implementations, but is not required to be implemented.
     * By default, it does nothing.
     */
    protected void notifyOfExit() {

    }

    private static abstract class JobException extends RuntimeException { }

    private static class JobNotReadyException extends JobException {
        private JobNotReadyException() { }
    }

    private static class JobFailedException extends JobException {
        private JobFailedException() { }
    }

    private void checkForRunThread() throws IllegalStateException {
        if (this.runThread == null || !Objects.equals(this.runThread, Thread.currentThread())) {
            throw new IllegalStateException("Only the run thread can call the protected methods during the implemented run methods");
        }
    }


    /**
     * Gets the accessor, which will be provided only to the main thread and only once.  This is
     * to ensure private access to critical methods, which only the main thread should have, and
     * its worker threads as necessary.
     *
     * @return the accessor
     * @throws IllegalStateException if the invoking thread is not the main thread, or if the
     * accessor has already been delivered to the main thread
     */
    public final Accessor getAccessor() throws IllegalStateException {
        if (accessorDelivered || !Main.isMain()) {
            throw new IllegalStateException("Only the main thread is allowed to obtain the new job accessor, " +
                    "and it may only request it once, then store its reference.  Current thread: "
                    + Thread.currentThread());
        }
        this.accessorDelivered = true;
        return accessor;
    }


    /**
     * The "accessor" allows for quasi-public access to private methods of Job, which should only
     * be accessible to the main thread and to worker threads as necessary.  By restricting access
     * to the job's accessor, we can control who has access to these methods, making them
     * effectively private for all but select classes.
     */
    public final class Accessor {
        private Set<Job> newJobs = new LinkedHashSet<>();
        private Accessor() { }

        /**
         * Gets new jobs.
         *
         * @return the new jobs
         */
        public synchronized Set<Job> getNewJobs() {
            Set<Job> oldSet = this.newJobs;
            this.newJobs = new LinkedHashSet();
            return oldSet;
        }

        private synchronized void addJob(Job newJob) {
            this.newJobs.add(newJob);
        }

        private synchronized void addJobs(Collection<Job> jobs) {
            this.newJobs.addAll(jobs);
        }

        /**
         * Gets run thread for the job represented by this accessor
         *
         * @return the run thread
         */
        public synchronized final Thread getRunThread() {
            return Job.this.runThread;
        }

        /**
         * Whether the job has failed
         *
         * @return failure state
         */
        public synchronized boolean isFailed() {
            return Job.this.failed;
        }

        /**
         * Gets stage the job is at
         *
         * @return the stage
         */
        public synchronized Stage getStage() {
            return Job.this.stage;
        }

        /**
         * Gets whether the job had an exception during its last run.  Not
         * necessarily an indication of failure.
         *
         * @return the boolean
         */
        public synchronized boolean hasException() {
            return Job.this.exception;
        }

        /**
         * Gets try at time for the job represented by this accessor
         *
         * @return the try at time
         */
        public synchronized long getTryAtTime() {
            return Job.this.tryAtTime;
        }

        /**
         * Gets the job which this accessor represents
         *
         * @return the current job
         */
        public final Job getCurrentJob() {
            return Job.this;
        }
    }
}
