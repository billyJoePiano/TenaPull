package nessusTools.run;

import com.fasterxml.jackson.core.*;
import nessusTools.client.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

public class IndexJob extends Job {
    private static Logger logger = LogManager.getLogger(IndexJob.class);

    private IndexResponse response;
    private final boolean markFailed;

    public IndexJob() {
        boolean markFailed = false;

        Properties config = Main.getConfig();
        String dir = config.getProperty("output.dir");

        logger.info("Checking output directory: " + dir);
        try {
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdir();
            }

        } catch (Exception e) {
            markFailed = true;
            logger.error("Error while trying to make directory '" + dir + "'", e);
        }

        if (!markFailed) {
            //force DB to initialize right away, using smallest table that likely has a value
            logger.info("Checking database connection: " + config.getProperty("db.url"));
            try {
                Timezone.dao.getById(1);

            } catch (Exception e) {
                markFailed = true;
                logger.error("Error while trying to initialize DB connection", e);
            }
        }

        this.markFailed = markFailed;
    }

    @Override
    protected boolean isReady() {
        if (this.markFailed) {
            Main.markErrorStatus();
            this.failed();
        }
        return !this.markFailed;
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
