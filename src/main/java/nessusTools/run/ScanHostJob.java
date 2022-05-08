package nessusTools.run;

import com.fasterxml.jackson.core.*;
import nessusTools.client.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.splunk.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ScanHostJob extends DbManagerJob.Child {
    public static final String OUTPUT_DIR = "splunk-output/";

    private static Logger logger = LogManager.getLogger(ScanHostJob.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withZone(TimeZone.getDefault().toZoneId());

    private final ScanResponse scanResponse;
    private final ScanHost host;
    private final NessusClient client = new NessusClient();
    private List<Vulnerability> vulns;
    private ScanHostResponse response;
    private final HostOutput output = new HostOutput();
    private final Timestamp scanTimestamp;

    public ScanHostJob(ScanResponse scanResponse, ScanHost host) {
        this.scanResponse = scanResponse;
        this.host = host;
        ScanInfo info = scanResponse.getInfo();
        if (info != null) {
            Timestamp scanTimestamp = info.getTimestamp();
            if (scanTimestamp == null) scanTimestamp = info.getScanEnd();
            if (scanTimestamp == null) scanTimestamp = info.getScannerEnd();
            this.scanTimestamp = scanTimestamp;

        } else {
            this.scanTimestamp = null;
        }
    }

    @Override
    protected boolean isReady() {
        ScanHostResponse oldRes = ScanHostResponse.dao.getById(this.host.getId());
        if (oldRes == null) {
            oldRes = new ScanHostResponse();
            oldRes.setId(this.host.getId());
            ScanHostResponse.dao.insert(oldRes);
        }
        assert oldRes.getId() == this.host.getId();

        this.output.setId(oldRes.getId());
        HostOutput oldOut = HostOutput.dao.getById(oldRes.getId());
        if (oldOut == null) {
            HostOutput.dao.insert(this.output);
        }

        return true;
    }

    @Override
    protected void fetch() throws JsonProcessingException {
        this.response = client.fetchJson(ScanHostResponse.getUrlPath(this.host), ScanHostResponse.class);
        this.vulns = response.getVulnerabilities();
        this.response._prepare();
    }

    @Override
    protected void process() {
        this.response.setId(this.host.getId());
        if (this.scanResponse != null) {
            this.response.setScanResponse(this.scanResponse);

        } else {
            if (this.response.getOrFetchScanResponse() == null) {
                throw new IllegalStateException("ScanResponse could not be found for ScanHost:\n"
                        + this.host);
            }

        }

        List<HostVulnerabilityOutput> list = new ArrayList<>(this.vulns.size());

        for (Vulnerability vuln : this.vulns) {
            list.add(new HostVulnerabilityOutput(this.scanResponse, this.host, this.response, vuln, this.output));
        }

        this.output.setScanTimestamp(scanTimestamp);
        this.output.setVulnerabilities(list);
        this.output.setVulnerabilityTimestamps();
    }


    @Override
    protected void output() throws FileNotFoundException, IOException {
        LocalDateTime useForFilename;
        if (this.scanTimestamp != null) {
            useForFilename = this.scanTimestamp.toLocalDateTime();

        } else {
            useForFilename = LocalDateTime.now();
        }

        String dateStr = formatter.format(useForFilename);
        Integer hostId = this.host.getHostId();

        String filename = OUTPUT_DIR + dateStr + "." + scanResponse.getId()
                + "." + hostId + ".json";

        this.output.setFilename(filename);

        logger.info("Writing " +  output.size() + " results to " + filename);

        LocalDateTime outputTime = LocalDateTime.now();
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
        SplunkOutputMapper.get().writeValue(writer, output);

        this.output.setOutputTimestamp(Timestamp.valueOf(outputTime));

        this.addDbTask(this::runDbInsert);
    }

    private void runDbInsert() {
        ScanHostResponse.dao.saveOrUpdate(this.response);
        HostOutput.dao.saveOrUpdate(this.output);
    }

    private int fetchCount = 0;

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        switch (stage) {
            case IDLE: break;

            case FETCH:
                logger.error("Error fetching scan host response id "
                        + host.getId() + "\n" + client.getResponse(), e);
                if (fetchCount++ <= 2) {
                    this.tryAgainIn(120000);
                    return true;
                }
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

    private boolean dbErredOnce = false;

    @Override
    protected boolean dbExceptionHandler(Exception e) {
        logger.error("Db error", e);
        if (dbErredOnce) return false;
        return dbErredOnce = true;
    }

    @Override
    public int compareTo(@NotNull DbManagerJob.Child o) {
        if (o == this) return 0;
        if (!(o instanceof ScanHostJob)) return 1;
        ScanHostJob other = (ScanHostJob)o;
        if (this.scanTimestamp == null) {
            if (other.scanTimestamp != null) return 1;

            int myId = this.scanResponse.getId();
            int theirId = other.scanResponse.getId();
            if (myId == theirId) return this.hashCode() - other.hashCode();
            return myId - theirId;

        } else if (other.scanTimestamp == null) {
            return -1;
        }
        long mine = this.scanTimestamp.getTime();
        long theirs = other.scanTimestamp.getTime();
        if (mine != theirs) {
            return mine < theirs ? -1 : 1;
        }

        int myId = this.host.getId();
        int theirId = other.host.getId();

        if (myId != theirId) return myId - theirId;
        return this.hashCode() - other.hashCode();
    }
}
