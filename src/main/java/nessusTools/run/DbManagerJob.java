package nessusTools.run;

import org.apache.logging.log4j.*;

import java.util.*;

public class DbManagerJob extends Job {
    private static final Logger logger = LogManager.getLogger(DbManagerJob.class);
    public static int NUM_HELPERS = 1;
    public static int MAX_JOBS_TASKS = 64;

    private final String name;
    private String nameForNext;
    private final List<Child> newChildJobs = new LinkedList<>();
    private final List<Child> childJobs = new LinkedList<>();
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
    protected void fetch() {
        logger.info("DbManagerJob '" + this.name + "' starting");
        while (true) {
            synchronized (this.monitor) {
                synchronized (this.newChildJobs) {
                    if (this.newChildJobs.size() > 0) return;
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

        this.done = true;
    }

    protected boolean processLoop(Job runningJob) throws Exception {
        boolean dbTask;
        if (runningJob != this) {
            dbTask = this.processNextDbTask();
            if (dbTask) return true;
        } else {
            dbTask = false;
        }

        synchronized (this.monitor) {
            this.processNewChildJobs(runningJob);
            boolean childrenRunning = this.checkStatusOfChildren();
            if (dbTask) return true;
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
            this.helpers.add(new Helper());
        }
    }

    @Override
    protected void output() {
        logger.info("DbManagerJob '" + this.name + "' finished");
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
        protected void fetch() { }

        @Override
        protected void process() throws Exception {
            while (DbManagerJob.this.processLoop(this)) { }
            this.done = true;
        }

        @Override
        protected void output() throws Exception { }

        @Override
        protected boolean exceptionHandler(Exception e, Stage stage) {
            return DbManagerJob.this.exceptionHandler(e, stage);
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
