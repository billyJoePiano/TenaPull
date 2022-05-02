package nessusTools.run;

public class WorkerThread extends Thread {
    private static long idCounter = 1;
    private final JobFactory jobFactory;

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
                job.start();

            } catch (Exception e) {

            }
        }
    }
}
