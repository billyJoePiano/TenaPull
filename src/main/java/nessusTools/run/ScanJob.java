package nessusTools.run;

import com.fasterxml.jackson.core.*;
import nessusTools.client.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import org.apache.logging.log4j.*;

import java.sql.*;
import java.util.*;

public class ScanJob extends Job {
    private static Logger logger = LogManager.getLogger(ScanJob.class);
    private static final ScanStatus COMPLETED = ScanStatus.dao.getOrCreate("completed");
    private static final ScanStatus CANCELED = ScanStatus.dao.getOrCreate("canceled");
    private static final long MAX_WAIT_FOR_COMPLETION = 2 * 60 * 60 * 1000; //2 hours in ms

    private final Scan scan;
    private final NessusClient client = new NessusClient();
    private ScanResponse response;
    boolean preexisting;
    Long startWait;

    public ScanJob(Scan scan) {
        this.scan = scan;
    }

    @Override
    protected boolean isReady() {
        if (!Objects.equals(COMPLETED, scan.getStatus())) {
            if (Objects.equals(CANCELED, scan.getStatus())) {
                this.failed();

            } else if (this.startWait == null) {
                this.startWait = System.currentTimeMillis();

            } else if (System.currentTimeMillis() - this.startWait > MAX_WAIT_FOR_COMPLETION) {
                logger.error("Timed out waiting for scan id " + scan.getId() + " to complete\n" + scan);
                this.failed();
            }
            return false;
        }

        ScanResponse old = ScanResponse.dao.getById(scan.getId());
        if (old == null) return true;

        preexisting = true;

        ScanInfo oldInfo = old.getInfo();
        if (oldInfo == null) return true;

        Timestamp oldTs = oldInfo.getTimestamp();
        Timestamp newTs = scan.getLastModificationDate();
        if (oldTs == null || newTs == null) return true;

        long diff = newTs.getTime() - oldTs.getTime();

        if (diff > 1000) return true; //have to account for DB rounding to the second
        else if (diff < 1000) {
            logger.error("Unexpected timestamp difference between old and new scan response in scan id "
                    + scan.getId()
                    + "\nold: " + oldTs
                    + "\nnew: " + newTs);
        }
        this.failed();
        return false;
    }

    @Override
    protected void fetch() throws JsonProcessingException {
        response = client.fetchJson(ScanResponse.getUrlPath(this.scan.getId()), ScanResponse.class);
    }

    @Override
    protected void process() {
        ScanResponse.dao.saveOrUpdate(response);
        if (preexisting) CachingMapper.resetCaches.valueToTree(this.scan);
    }

    @Override
    protected void output() {
        for (ScanHost host : response.getHosts()) {
            this.addJob(new ScanHostJob(host));
        }
    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        switch (stage) {
            case IDLE: break;

            case FETCH:
                logger.error("Error processing fetching scan response id "
                        + scan.getId() + "\n" + client.getResponse(), e);
                break;

            case PROCESS:
                logger.error("Error persisting scan response to DB, id "
                        + scan.getId() + "\n" + response, e);
                break;

            case OUTPUT:
                logger.error("Error generating next jobs after scan response id "
                        + scan.getId() + "\n" + response, e);
                break;
        }
        this.failed();
        return false;
    }
}
