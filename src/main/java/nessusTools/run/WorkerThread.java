package nessusTools.run;

import org.apache.logging.log4j.*;

public class WorkerThread extends Thread {
    private static long idCounter = 1;
    private final JobFactory jobFactory;

    private static Logger logger = LogManager.getLogger(WorkerThread.class);

    public WorkerThread(JobFactory jobFactory) {
        if (jobFactory == null) throw new NullPointerException();
        this.setName("WorkerThread #" + idCounter++);
        this.jobFactory = jobFactory;
        this.start();
    }

    public void run() {
        Job job;
        while ((job = jobFactory.getNextJob()) != null) {
            try {
                job.start(this);

            } catch (Throwable e) {
                logger.error("Fatal error on thread " + this.getName(), e);
                throw e;
            }
        }
    }
}
