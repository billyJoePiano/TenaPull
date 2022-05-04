package nessusTools.run;

import com.fasterxml.jackson.core.*;
import nessusTools.client.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import org.apache.logging.log4j.*;

import java.sql.*;

public class ScanHostJob extends Job {
    private static Logger logger = LogManager.getLogger(ScanHostJob.class);

    private final ScanHost host;
    private final NessusClient client = new NessusClient();
    private ScanHostResponse response;

    public ScanHostJob(ScanHost host) {
        this.host = host;
    }

    @Override
    protected boolean isReady() {
        return true;
    }

    @Override
    protected void fetch() throws JsonProcessingException {
        response = client.fetchJson(ScanHostResponse.getUrlPath(this.host), ScanHostResponse.class);
    }

    @Override
    protected void process() {
        ScanHostResponse.dao.saveOrUpdate(response);
    }

    @Override
    protected void output() {
        ScanResponse scanResponse = this.host.getResponse();
        if (response != null) {
            response.setScanResponse(scanResponse);
        } else {
            scanResponse = response.getScanResponse();
        }
        this.addJob(new SplunkOutputJob(scanResponse, response));
    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        switch (stage) {
            case IDLE: break;

            case FETCH:
                logger.error("Error processing fetching scan host response id "
                        + host.getId() + "\n" + client.getResponse(), e);
                break;

            case PROCESS:
                logger.error("Error persisting scan response to DB, id "
                        + host.getId() + "\n" + response, e);
                break;

            case OUTPUT:
                logger.error("Error generating next jobs after scan response id "
                        + host.getId() + "\n" + response, e);
                break;
        }
        this.failed();
        return false;
    }
}
