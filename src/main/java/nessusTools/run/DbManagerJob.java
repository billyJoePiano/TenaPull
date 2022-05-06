package nessusTools.run;

import org.apache.logging.log4j.*;

import java.util.*;

public class DbManagerJob extends Job {
    private static final Logger logger = LogManager.getLogger(DbManagerJob.class);

    private final List<Child> newChildJobs = new LinkedList<>();
    private final List<Child> childJobs = new LinkedList<>();
    private final Map<Runnable, Child> dbTasks = new LinkedHashMap<>();
    private final List<Child> nextJobs = new LinkedList<>();
    private final List<Job> addAfterDone = new LinkedList<>();

    private final Object monitor = new Object();
    private boolean done = false;

    public DbManagerJob() {
        this(null);
    }

    public DbManagerJob(List<Child> childJobs) {
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
    protected void fetch() {
        while (!processNewChildJobs()) {
            synchronized (this.monitor) {
                try {
                    this.monitor.wait(JobFactory.MAX_MAIN_WAIT_TIME);
                } catch (InterruptedException e) { }
            }
        }
    }

    @Override
    protected void process() throws Exception {
        while (true) {
            boolean dbTask = this.processNextDbTask();
            synchronized (this.monitor) {
                this.processNewChildJobs();
                boolean childrenRunning = this.checkStatusOfChildren();
                if (dbTask) continue;
                synchronized (this.dbTasks) {
                    if (this.dbTasks.size() > 0) continue;
                }

                if (childrenRunning || this.processNewChildJobs()) {
                    try {
                        this.monitor.wait(JobFactory.MAX_MAIN_WAIT_TIME);
                    } catch (InterruptedException e) { }

                } else {
                    this.done = true;
                    return;
                }
            }
        }
    }

    @Override
    protected void output() {
        synchronized (this.addAfterDone) {
            for (Job job : this.addAfterDone) {
                this.addJob(job);
            }
        }

        synchronized (this.nextJobs) {
            if (this.nextJobs.size() > 0) {
                this.addJob(new DbManagerJob(this.nextJobs));
            }
        }
    }

    private boolean processNewChildJobs() {
        List<Child> copy;
        synchronized (this.newChildJobs) {
            if (this.newChildJobs.size() <= 0) return false;
            copy = new ArrayList<>(this.newChildJobs);
            this.newChildJobs.clear();
        }
        this.childJobs.addAll(copy);
        for (Child job : copy) {
            if (job == null) continue;
            this.addJob(job);
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
            if (child != null && child.dbExceptionHandler(e)) {
                this.addDbTask(task, child);
            }
        }
        return true;
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

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        logger.error("Exception in DbManagerJob", e);
        this.tryAgainIn(0);
        return true;
    }

    public static abstract class Child extends Job {
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

    private void checkStatus() {
        synchronized (this.monitor) {
            if (this.done) {
                throw new IllegalStateException("Cannot call DbManagerJob add methods after the processing stage is done!");
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
