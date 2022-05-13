package tenapull.run;

import tenapull.client.*;
import org.apache.logging.log4j.*;


/**
 * The worker thread which runs the jobs provided to the main thread.
 */
public class WorkerThread extends Thread {
    private static long idCounter = 1;
    private final JobFactory jobFactory;
    private final NessusClient client = new NessusClient();

    private static Logger logger = LogManager.getLogger(WorkerThread.class);

    /**
     * Instantiates a new Worker thread using the provided JobFactory
     * instance to obtain all of its jobs.
     *
     * @param jobFactory the job factory which will provide all of the jobs for this thread to run
     * @throws NullPointerException if the provided jobFactory is null
     */
    public WorkerThread(JobFactory jobFactory) throws NullPointerException {
        if (jobFactory == null) throw new NullPointerException();
        this.setName("WorkerThread #" + idCounter++);
        this.jobFactory = jobFactory;
        this.start();
    }

    public void run() {
        Job job;
        while ((job = jobFactory.getNextJob()) != null) {
            try {
                job.start(this, client);

            } catch (Throwable e) {
                logger.error("Fatal error on " + this.getName(), e);
                throw e;
            }
        }
    }
}
