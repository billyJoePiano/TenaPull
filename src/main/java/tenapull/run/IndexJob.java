package tenapull.run;

import com.fasterxml.jackson.core.*;
import tenapull.client.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.scan.*;
import org.apache.logging.log4j.*;

import java.util.*;

/**
 * Fetches the index of scans and folders from the root of the Nessus API.  Generates ScanJobs
 * based on this response, and puts them into a new DbManager job that is added to the readyJobs
 * queue
 */
public class IndexJob extends Job {
    private static Logger logger = LogManager.getLogger(IndexJob.class);

    private IndexResponse response;
    private final boolean ready;

    /**
     * Instantiates a new Index job.
     */
    public IndexJob() {
        boolean ready = Main.initOutputDir();

        if (ready) {
            //force DB to initialize right away, using smallest table that likely has a value
            logger.info("Checking database connection: " + Main.getConfig("db.url"));
            try {
                Timezone.dao.getById(1);

            } catch (Exception e) {
                ready = false;
                logger.error("Error while trying to initialize DB connection", e);
            }
        }

        this.ready = ready;
    }

    @Override
    protected boolean isReady() {
        if (this.ready) return true;
        Main.markErrorStatus();
        this.failed();
        return false;
    }

    private NessusClient client;
    @Override
    protected void fetch(NessusClient client) throws JsonProcessingException {
        this.client = client;
        logger.info("Fetching scan index");
        response = client.fetchJson(IndexResponse.pathFor(), IndexResponse.class);
        this.client = null;
    }

    @Override
    protected void process() {
        IndexResponse.dao.saveOrUpdate(response);
    }

    @Override
    protected void output() {
        List<DbManagerJob.Child> scanJobList = new ArrayList<>(response.getScans().size());
        for (Scan scan : response.getScans()) {
            if (scan == null) continue;
            scanJobList.add(new ScanJob(scan));
        }
        DbManagerJob dbManagerJob = new DbManagerJob("Process Scans", scanJobList);
        dbManagerJob.setNameForNext("Process Host Vulnerabilities");
        this.addJob(dbManagerJob);
    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        switch (stage) {
            case FETCH:
                String responseStr = this.client != null ? this.client.getResponse() : "";
                this.client = null;
                logger.error("Error processing or fetching index response\n"
                        + responseStr, e);
                break;

            case PROCESS:
                logger.error("Error persisting index response to DB:\n"
                                + response, e);
                break;

            case OUTPUT:
                logger.error("Error generating next jobs after index response:\n" +
                                response, e);
                break;
        }
        this.failed();
        return false;
    }
}
