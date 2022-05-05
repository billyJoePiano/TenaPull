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

    private DbManagerJob() {
        this(null);
    }

    private DbManagerJob(List<Child> childJobs) {
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
                    } catch (InterruptedException e) {
                    }
                } else {
                    this.done = true;
                }
            }
        }
    }

    @Override
    protected void output() {
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
        Map.Entry<Runnable, Child> entry = null;
        synchronized (this.dbTasks) {
            if (this.dbTasks.size() > 0) {
                for (Iterator<Map.Entry<Runnable, Child>>
                     iterator = this.dbTasks.entrySet().iterator();
                     iterator.hasNext();) {

                    entry = iterator.next();
                    iterator.remove();
                    if (entry != null) break;
                }
            }
        }
        if (entry == null) {
            return false;
        }

        Runnable task = entry.getKey();
        if (task == null) return false;

        try {
            task.run();

        } catch (Exception e) {
            if (entry.getValue().dbExceptionHandler(e)) {
                this.addDbTask(task, entry.getValue());
            }
        }
        return true;
    }

    private boolean checkStatusOfChildren() {
        synchronized (this.childJobs) {
            for (Child child : this.childJobs) {
                if (child.getStage() != Stage.DONE) {
                    return true;
                }
            }
        }
        return false;
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

        protected final void addToNextDbJobs(Child jobToRunAfterDone) {
            this.dbManager.addToNextDbJobs(jobToRunAfterDone);
        }

        protected final void addAfterDone(Job nonDbJobToRunAfterDone) {
            this.dbManager.addAfterDone(nonDbJobToRunAfterDone);
        }

        protected final void addCurrentChildJob(Child jobToRunBeforeDone) {
            this.dbManager.addCurrentChildJob(jobToRunBeforeDone);
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
        notifyParent();
    }

    private final void addToNextDbJobs(Child jobToRunAfterDone) {
        checkStatus();
        synchronized (this.nextJobs) {
            this.nextJobs.add(jobToRunAfterDone);
        }
        notifyParent();
    }

    private final void addAfterDone(Job nonDbJobToRunAfterDone) {
        checkStatus();
        synchronized (this.addAfterDone) {
            this.addAfterDone.add(nonDbJobToRunAfterDone);
        }
    }

    private final void addCurrentChildJob(Child jobToRunBeforeDone) {
        checkStatus();
        if (jobToRunBeforeDone != null) {
            this.addCurrentChildSkipCheck(jobToRunBeforeDone);
        }
        notifyParent();
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

    private void notifyParent() {
        synchronized (this.monitor) {
            this.monitor.notifyAll();
        }
    }
}
