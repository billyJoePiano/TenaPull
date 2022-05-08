package nessusTools.run;

import com.fasterxml.jackson.core.*;
import nessusTools.client.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import org.apache.logging.log4j.*;

import java.sql.*;
import java.time.*;
import java.util.*;

public class IndexJob extends Job {
    private static Logger logger = LogManager.getLogger(IndexJob.class);

    private final NessusClient client = new NessusClient();
    private IndexResponse response;

    @Override
    protected boolean isReady() {
        return true;
    }

    @Override
    protected void fetch() throws JsonProcessingException {
        logger.info("Fetching scan index");
        response = client.fetchJson(IndexResponse.pathFor(), IndexResponse.class);
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
                logger.error("Error processing fetching index response:\n"
                        + client.getResponse(), e);
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
