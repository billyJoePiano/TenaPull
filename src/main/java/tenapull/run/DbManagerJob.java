package tenapull.run;

import tenapull.client.*;
import org.apache.logging.log4j.*;

import java.util.*;

/**
 * DbManager Job manages the pile of dbTasks which other threads will provide.  Because multi-threaded
 * DB write operations tend to cause many exceptions, the critical dbTasks are all handled by a single
 * thread whose job is strictly to execute these tasks.  The thread actually executing the tasks runs
 * the DbManager.Helper job, while the DbManager job/thread itself merely manages the pile of db tasks
 * coming in from the child jobs.
 */
public class DbManagerJob extends Job {
    private static final Logger logger = LogManager.getLogger(DbManagerJob.class);
    /**
     * The number of helper jobs.  Experience has shown that any more than 1 will cause exceptions
     */
    public static final int NUM_HELPERS = 1;
    /**
     * The constant MAX_JOBS_TASKS.  The limit on the number of tasks which will be allowed
     * to pile up, before new child jobs are released to the main thread for scheduling in the
     * readyJobs queue
     */
    public static final int MAX_JOBS_TASKS = 64;
    /**
     * The constant MAX_EXCEPTIONS.  The maximum number of exceptions that will be allowed before
     * the job exits with failure status.  Prevents infinite loops of exceptions and rescheduling
     * the jobs causing them.
     */
    public static final int MAX_EXCEPTIONS = 32;
    /**
     * The constant WAIT_FOR_CHILD_JOB_TIMEOUT_MS.  The maximum amount of time the DbManager job will
     * wait for a child job to finish before double-checking the status of other tasks or jobs.  This
     * is probably unnecessary, but was included as a fail-safe in case of a bug or mistake in the
     * DbManager logic...
     */
    public static final long WAIT_FOR_CHILD_JOB_TIMEOUT_MS = 1800000; //30 minutes in ms

    private final String name;
    private String nameForNext;
    private final Set<Child> newChildJobs = new TreeSet<>();
    private final Set<Child> childJobs = new TreeSet<>();
    private final Map<Runnable, Child> dbTasks = new LinkedHashMap<>();
    private final List<Child> nextJobs = new LinkedList<>();
    private final List<Job> addAfterDone = new LinkedList<>();
    private final List<Helper> helpers = new LinkedList<>();

    private final Object monitor = new Object();
    private boolean done = false;

    /**
     * Instantiates a new Db manager job with the provided name
     *
     * @param name the name
     */
    public DbManagerJob(String name) {
        this(name, null);
    }

    /**
     * Instantiates a new Db manager job with the provided name and list of child jobs
     *
     * @param name      the name
     * @param childJobs the child jobs
     */
    public DbManagerJob(String name, List<Child> childJobs) {
        this.name = name;
        if (childJobs == null) return;
        for (Child job : childJobs) {
            if (job == null) continue;
            this.addCurrentChildSkipCheck(job);
        }

    }

    @Override
    protected boolean isReady() {
        return true;
    }

    @Override
    protected void fetch(NessusClient client) {
        logger.info("'" + this.name + "' starting");
        long start = System.currentTimeMillis();
        while (true) {
            synchronized (this.monitor) {
                synchronized (this.newChildJobs) {
                    if (this.newChildJobs.size() > 0) return;

                    if (System.currentTimeMillis() - start
                            >= WAIT_FOR_CHILD_JOB_TIMEOUT_MS) {

                        logger.error(this.name + " timed out waiting for child jobs ... exiting");

                        this.failed();
                        return;
                    }
                }

                try {
                    this.monitor.wait(JobFactory.MAIN_MAX_WAIT_TIME);
                } catch (InterruptedException e) { }
            }
        }
    }

    @Override
    protected void process() throws Exception {
        do {
            this.checkHelperJobs();

        } while (this.processLoop(this));


        //Wait for helper(s) to finish
        waitForHelpersToExit();

        this.done = true;
    }

    /**
     * Process loop boolean.
     *
     * @param runningJob the running job
     * @return the boolean
     * @throws Exception the exception
     */
    protected boolean processLoop(Job runningJob) throws Exception {
        boolean dbTask;
        if (runningJob != this) {
            if (this.processNextDbTask()) return true;
        }

        synchronized (this.monitor) {
            if (this.exceptionCount > MAX_EXCEPTIONS) return false;

            this.processNewChildJobs(runningJob);
            boolean childrenRunning = this.checkStatusOfChildren();

            synchronized (this.dbTasks) {
                if (this.dbTasks.size() > 0) return true;
            }

            if (childrenRunning || this.processNewChildJobs(runningJob)) {
                try {
                    this.monitor.wait(JobFactory.MAIN_MAX_WAIT_TIME);
                } catch (InterruptedException e) { }

            } else {
                return false;
            }
        }
        return true;
    }

    private void checkHelperJobs() {
        synchronized (this.monitor) {
            if (this.exceptionCount > MAX_EXCEPTIONS) {
                this.failed();
                return;
            }
        }
        for (ListIterator<Helper> iterator = this.helpers.listIterator();
                iterator.hasNext();) {

            Helper helper = iterator.next();
            if (helper == null) {
                iterator.remove();

            } else if (helper.getStage() == Stage.DONE && !helper.done) {
                iterator.remove();
                iterator.add(new Helper());

            }
        }
        while (this.helpers.size() < NUM_HELPERS) {
            synchronized (this.dbTasks) {
                if (this.dbTasks.size() <= 0) break;
                this.helpers.add(new Helper());
            }
        }
    }

    private void waitForHelpersToExit() {
        synchronized (this.monitor) {
            while (true) {
                checkHelperJobs();
                if (this.helpers.size() <= 0) break;

                Helper helper = helpers.get(0);
                if (helper == null || helper.getStage() == Stage.DONE) {
                    this.helpers.remove(0);
                    continue;
                }

                try {
                    this.monitor.wait(JobFactory.MAIN_MAX_WAIT_TIME);

                } catch (InterruptedException e) { }
            }
        }
    }

    @Override
    protected void output() {
        logger.info("'" + this.name + "' finished");
        synchronized (this.addAfterDone) {
            for (Job job : this.addAfterDone) {
                this.addJob(job);
            }
        }

        synchronized (this.nextJobs) {
            if (this.nameForNext == null) {
                this.nameForNext = "After " + this.name;
            }
            if (this.nextJobs.size() > 0) {
                this.addJob(new DbManagerJob(this.nameForNext, this.nextJobs));
            }
        }
    }

    private boolean processNewChildJobs(Job runningJob) {
        int addJobs;
        synchronized (this.dbTasks) {
            addJobs = MAX_JOBS_TASKS - this.dbTasks.size() - this.childJobs.size();
        }

        List<Child> copy = addJobs > 0 ? new ArrayList<>(addJobs) : null;
        synchronized (this.newChildJobs) {
            if (this.newChildJobs.size() <= 0) return false;
            else if (addJobs <= 0) return true;
            for (Iterator<Child> iterator = this.newChildJobs.iterator();
                        iterator.hasNext();) {

                Child child = iterator.next();
                iterator.remove();
                if (child != null) {
                    copy.add(child);
                    if (copy.size() >= addJobs) break;
                }
            }
        }
        this.childJobs.addAll(copy);
        for (Child job : copy) {
            if (job == null) continue;
            runningJob.addJob(job);
        }
        return true;
    }

    private boolean processNextDbTask() {
        Child child = null;
        Runnable task = null;
        synchronized (this.dbTasks) {
            if (this.dbTasks.size() > 0) {
                for (Iterator<Map.Entry<Runnable, Child>>
                     iterator = this.dbTasks.entrySet().iterator();
                     iterator.hasNext();) {

                    Map.Entry<Runnable, Child> entry = iterator.next();
                    iterator.remove();
                    task = entry.getKey();
                    child = entry.getValue();
                    if (task != null) break;
                }
            }
        }
        if (task == null) {
            return false;
        }

        try {
            task.run();

        } catch (Exception e) {
            synchronized (this.monitor) {
                if (++this.exceptionCount > MAX_EXCEPTIONS) {
                    synchronized (this.dbTasks) {
                        this.dbTasks.clear();
                    }
                    return false;
                }
            }

            if (child != null && child.dbExceptionHandler(e)) {
                this.addDbTask(task, child);
            }
        }
        return true;
    }

    private class Helper extends Job {
        private boolean done = false;

        private Helper() {
            DbManagerJob.this.addJob(this);
        }

        @Override
        protected boolean isReady() {
            return true;
        }

        @Override
        protected void fetch(NessusClient client) { }

        @Override
        protected void process() throws Exception {
            while (DbManagerJob.this.processLoop(this)) { }
        }

        @Override
        protected void output() throws Exception {
            synchronized (DbManagerJob.this.monitor) {
                this.done = true;
                DbManagerJob.this.monitor.notifyAll();
            }
        }

        @Override
        protected boolean exceptionHandler(Exception e, Stage stage) {
            return DbManagerJob.this.exceptionHandler(e, stage, this);
        }
    }

    private boolean checkStatusOfChildren() {
        boolean stillRunning = false;
        synchronized (this.childJobs) {
            for (Iterator<Child> iterator = this.childJobs.iterator();
                        iterator.hasNext();) {

                Child child = iterator.next();
                if (child == null || child.getStage() == Stage.DONE) {
                    iterator.remove();

                } else {
                    stillRunning = true;
                }
            }
        }
        return stillRunning;
    }

    private int exceptionCount = 0;

    @Override
    protected boolean exceptionHandler (Exception e, Stage stage) {
        return exceptionHandler(e, stage, this);
    }

    /**
     * If there is an exception, the exceptionCount is incremented, then checked to see if it
     * has surpassed MAX_EXCEPTIONS.  If it has, the job is marked as failed and false is returned.
     * Otherwise, the true is returned and processing continues.
     *
     * @param e          the e
     * @param stage      the stage
     * @param runningJob the running job
     * @return the boolean
     */
    protected boolean exceptionHandler(Exception e, Stage stage, Job runningJob) {
        logger.error("Exception in DbManagerJob", e);
        synchronized (this.monitor) {
            if (++this.exceptionCount > MAX_EXCEPTIONS) {
                logger.fatal("EXCESSIVE NUMBER OF EXCEPTIONS, GIVING UP");
                synchronized (this.dbTasks) {
                    this.dbTasks.clear();
                }
                runningJob.failed();
                return false;
            }
        }

        this.tryAgainIn(0);
        return true;
    }

    /**
     * Jobs which implement DbManager.Child are the jobs which do all of the fetching
     * and pre-processing before providing dbTasks to the db manager.  They may invoke
     * the addDbTask method to add a dbTask for the current dbManager to process,
     * or addToNextDbJobs method, as a way to add new child jobs for the next
     * dbManager to run, once the current dbManager is finished.
     *
     * Additionally, all child jobs must implement the comparable interface so they
     * can be sorted for order of execution.
     */
    public static abstract class Child extends Job implements Comparable<Child> {
        private DbManagerJob dbManager;

        /**
         * This method is invoked if the dbTask throws an exception.  Since the child
         * job will likely have returned and be marked as done by the time the task
         * is actually run, it is necessary to include this in addition to the run-time
         * exceptionHandler method of the Job superclass.  This method is specific to
         * exceptions thrown during the dbTask
         *
         * @param e the e
         * @return the boolean
         */
        protected abstract boolean dbExceptionHandler(Exception e);

        /**
         * Add db task to run in the current dbManager job
         *
         * @param task the task
         */
        protected final void addDbTask(Runnable task) {
            if (task == null) return;
            this.dbManager.addDbTask(task, this);
        }

        /**
         * Add child job to be run by the next dbManager that will be
         * run after the current dbManager has finished all of its
         * child jobs and dbTasks.
         *
         * @param dbJobToRunAfterDone the child job to run by the next
         *                            dbManager
         */
        protected final void addToNextDbJobs(Child dbJobToRunAfterDone) {
            if (dbJobToRunAfterDone == null) return;
            this.dbManager.addToNextDbJobs(dbJobToRunAfterDone);
        }

        /**
         * Add a regular job (non-child job) to be run after the current
         * dbManager job has finished
         *
         * @param nonDbJobToRunAfterDone the non db job to run after done
         */
        protected final void addAfterDone(Job nonDbJobToRunAfterDone) {
            if (nonDbJobToRunAfterDone == null) return;
            this.dbManager.addAfterDone(nonDbJobToRunAfterDone);
        }

        /**
         * Add a child job to the list of child jobs to be run by the
         * current dbManager
         *
         * @param jobToRunBeforeDone the job to run before done
         */
        protected final void addCurrentChildJob(Child jobToRunBeforeDone) {
            if (jobToRunBeforeDone == null) return;
            this.dbManager.addCurrentChildJob(jobToRunBeforeDone);
        }

        /**
         * Notifies the dbManager that the job has exited.  Allows it to
         * process any dbTasks the job may have provided, and to remove
         * it from the list of running child jobs.  Final method -- may
         * not be overridden.  Child jobs may override notifyChildOfExit instead,
         * which will be invoked after notifying the dbManager.
         */
        @Override
        protected final void notifyOfExit() {
            if (this.dbManager != null) {
                synchronized (this.dbManager.monitor) {
                    this.dbManager.monitor.notifyAll();
                }
            }
            this.notifyChildOfExit();
        }

        /**
         * Substitute for notifyOfExit, that can be overridden by child jobs, since
         * notifyOfExit is final.  The default implementation does nothing.
         */
        protected void notifyChildOfExit() {

        }
    }

    /**
     * Sets name for the next DbManager job to be created from the list of nextJobs,
     * after this dbManager is finished
     *
     * @param name the name of the next dbManager
     */
    public void setNameForNext(String name) {
        checkStatus();
        this.nameForNext = name;
    }

    /**
     * Gets the name set for the next dbManager job
     *
     * @return the name for next dbManager job
     */
    public String getNameForNext() {
        return this.nameForNext;
    }

    private void checkStatus() {
        synchronized (this.monitor) {
            if (this.done) {
                throw new IllegalStateException("Cannot call DbManagerJob special methods after the processing stage is done!");
            }
        }
    }

    private final void addDbTask(Runnable task, Child child) {
        checkStatus();
        synchronized (this.dbTasks) {
            this.dbTasks.put(task, child);
        }
        notifyManager();
    }

    private final void addToNextDbJobs(Child dbJobToRunAfterDone) {
        checkStatus();
        synchronized (this.nextJobs) {
            this.nextJobs.add(dbJobToRunAfterDone);
        }
        notifyManager();
    }

    private final void addAfterDone(Job nonDbJobToRunAfterDone) {
        checkStatus();
        synchronized (this.addAfterDone) {
            this.addAfterDone.add(nonDbJobToRunAfterDone);
        }
    }

    private final void addCurrentChildJob(Child jobToRunBeforeDone) {
        checkStatus();
        this.addCurrentChildSkipCheck(jobToRunBeforeDone);
        notifyManager();
    }

    private final void addCurrentChildSkipCheck(Child jobToRunBeforeDone) {
        if (jobToRunBeforeDone.dbManager != null && jobToRunBeforeDone.dbManager != this) {
            throw new IllegalStateException("Cannot reassign DbManager of a Child db job: " + jobToRunBeforeDone);

        } else if (jobToRunBeforeDone.getStage() != Stage.IDLE) {
            throw new IllegalStateException("New db child jobs to run must be in Idle state: " + jobToRunBeforeDone);
        }

        jobToRunBeforeDone.dbManager = this;
        synchronized (this.newChildJobs) {
            this.newChildJobs.add(jobToRunBeforeDone);
        }
    }

    private void notifyManager() {
        synchronized (this.monitor) {
            this.monitor.notifyAll();
        }
    }
}
