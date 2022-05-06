package nessusTools.run;

import org.apache.logging.log4j.*;

import java.util.*;

public abstract class Job {
    public static long DEFAULT_TRY_AGAIN_TIME = 600000;
    private static Logger logger = LogManager.getLogger(Job.class);

    public enum Stage {
        IDLE,
        FETCH,
        PROCESS,
        OUTPUT,
        DONE
    }

    private final Accessor accessor = new Accessor();
    private boolean accessorDelivered = false;
    private WorkerThread runThread;
    private Stage stage = Stage.IDLE;
    private boolean exception = false;
    private boolean failed = false;
    private long tryAtTime;

    protected Job() {
        this(0);
    }

    protected Job(long waitMs) {
        this.tryAtTime = System.currentTimeMillis() + waitMs;
    }

    abstract protected boolean isReady();

    abstract protected void fetch() throws Exception;

    abstract protected void process() throws Exception;

    abstract protected void output() throws Exception;

    abstract protected boolean exceptionHandler(Exception e, Stage stage);

    public synchronized void waitForExit() {
        while (this.stage == Stage.IDLE) {
            try {
                this.wait();
            } catch (InterruptedException e) { }
        }
    }

    protected final void failed() {
        checkForRunThread();
        this.failed = true;
    }

    protected final void tryAgainIn(long ms) {
        checkForRunThread();
        if (ms < 0) ms = 0;
        this.tryAtTime = System.currentTimeMillis() + ms;
    }


    public final Stage getStage() {
        //checkForRunThread();
        return this.stage;
    }

    protected void addJob(Job newJob) {
        checkForRunThread();
        if (newJob == null) return;
        accessor.addJob(newJob);
        JobFactory.notifyOfJobProvider(this);
    }

    protected final void addJobs(Collection<Job> newJobs) {
        checkForRunThread();
        if (newJobs == null) return;
        accessor.addJobs(newJobs);
        JobFactory.notifyOfJobProvider(this);
    }

    public final void start(WorkerThread runThread) {
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
            this.runStages();

        } finally {
            synchronized (accessor) {
                this.runThread = null;
            }
            this.notifyOfExit();
            JobFactory.notifyOfJobExit(this);
        }
    }

    private final synchronized void runStages() {
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
                            this.fetch();
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

    //To be overridden by subclasses
    protected void notifyOfExit() {

    }

    private static abstract class JobException extends RuntimeException { }

    private static class JobNotReadyException extends JobException {
        private JobNotReadyException() { }
    }

    private static class JobFailedException extends JobException {
        private JobFailedException() { }
    }

    private void checkForRunThread() {
        if (this.runThread == null || !Objects.equals(this.runThread, Thread.currentThread())) {
            throw new IllegalStateException("Only the run thread can call the protected methods during the implemented run methods");
        }
    }


    public final Accessor getAccessor() throws IllegalStateException {
        if (accessorDelivered || !Main.isMain()) {
            throw new IllegalStateException("Only the main thread is allowed to obtain the new job accessor, " +
                    "and it may only request it once, then store its reference.  Current thread: "
                    + Thread.currentThread());
        }
        this.accessorDelivered = true;
        return accessor;
    }


    public final class Accessor {
        private Set<Job> newJobs = new LinkedHashSet<>();
        private Accessor() { }

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

        public synchronized final Thread getRunThread() {
            return Job.this.runThread;
        }

        public synchronized boolean isFailed() {
            return Job.this.failed;
        }

        public synchronized Stage getStage() {
            return Job.this.stage;
        }

        public synchronized boolean hasException() {
            return Job.this.exception;
        }

        public synchronized long getTryAtTime() {
            return Job.this.tryAtTime;
        }

        public final Job getCurrentJob() {
            return Job.this;
        }
    }
}
