package nessusTools.run;

import nessusTools.client.*;
import org.apache.logging.log4j.*;

import java.util.*;

public class DbManagerJob extends Job {
    private static final Logger logger = LogManager.getLogger(DbManagerJob.class);
    public static final int NUM_HELPERS = 1;
    public static final int MAX_JOBS_TASKS = 64;
    public static final int MAX_EXCEPTIONS = 32;
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

    public DbManagerJob(String name) {
        this(name, null);
    }

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
                    this.monitor.wait(JobFactory.MAX_MAIN_WAIT_TIME);
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
                    this.monitor.wait(JobFactory.MAX_MAIN_WAIT_TIME);
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
                    this.monitor.wait(JobFactory.MAX_MAIN_WAIT_TIME);

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

    public static abstract class Child extends Job implements Comparable<Child> {
        DbManagerJob dbManager;

        protected abstract boolean dbExceptionHandler(Exception e);

        protected final void addDbTask(Runnable task) {
            if (task == null) return;
            this.dbManager.addDbTask(task, this);
        }

        protected final void addToNextDbJobs(Child dbJobToRunAfterDone) {
            if (dbJobToRunAfterDone == null) return;
            this.dbManager.addToNextDbJobs(dbJobToRunAfterDone);
        }

        protected final void addAfterDone(Job nonDbJobToRunAfterDone) {
            if (nonDbJobToRunAfterDone == null) return;
            this.dbManager.addAfterDone(nonDbJobToRunAfterDone);
        }

        protected final void addCurrentChildJob(Child jobToRunBeforeDone) {
            if (jobToRunBeforeDone == null) return;
            this.dbManager.addCurrentChildJob(jobToRunBeforeDone);
        }

        @Override
        protected final void notifyOfExit() {
            if (this.dbManager != null) {
                synchronized (this.dbManager.monitor) {
                    this.dbManager.monitor.notifyAll();
                }
            }
        }
    }

    public void setNameForNext(String name) {
        checkStatus();
        this.nameForNext = name;
    }

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
