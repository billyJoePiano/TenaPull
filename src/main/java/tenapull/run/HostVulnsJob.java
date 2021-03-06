package tenapull.run;

import com.fasterxml.jackson.core.*;
import tenapull.client.*;
import tenapull.data.deserialize.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.scan.*;
import tenapull.data.entity.splunk.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * Takes a given scanResponse and host from the scanResponse, and fetches the host vulnerability
 * data from the Nessus API about that host.  The resulting data is output as a .json file ready
 * for Splunk ingestion, and the DB is updated to show that this is the most recent file generated
 * for this host_id/scan_id combination
 */
public class HostVulnsJob extends DbManagerJob.Child {
    /**
     * The output directory, as obtained by Main from the config file specified in command-line arg[0]
     */
    public static final String OUTPUT_DIR = Main.getConfig("output.dir");

    public static final boolean SEPARATE_OUTPUTS = Main.hasConfig("output.separate");

    private static final Logger logger = LogManager.getLogger(HostVulnsJob.class);
    private static final DateTimeFormatter filenameFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH.mm.ss")
            .withZone(TimeZone.getDefault().toZoneId());

    private final ScanResponse scanResponse;
    private final ScanHost host;
    private List<Vulnerability> vulns;
    private ScanHostResponse response;
    private final HostOutput output;
    private final Timestamp scanTimestamp;

    /**
     * Instantiates a new Host vulns job using the provided scanResponse and scan host.
     *
     * @param scanResponse the scan response which the host originates from
     * @param host         the host to obtain the vulnerability data about
     */
    public HostVulnsJob(ScanResponse scanResponse, ScanHost host) {
        this(scanResponse, host, new HostOutput());
    }

    public HostVulnsJob(ScanResponse scanResponse, ScanHost host,
                        ScanHostResponse scanHostResponse, HostOutput testOutput) {

        this(scanResponse, host, testOutput);
        this.response = scanHostResponse;

    }

    private HostVulnsJob(ScanResponse scanResponse, ScanHost host, HostOutput testOutput) {
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
        this.output = testOutput;
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

    private NessusClient client = null;

    @Override
    protected void fetch(NessusClient client) throws JsonProcessingException {
        this.client = client;
        this.response = client.fetchJson(ScanHostResponse.getUrlPath(this.host), ScanHostResponse.class);
        this.client = null;
    }

    @Override
    protected void process() {
        this.vulns = response.getVulnerabilities();
        this.response._prepare();
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
    protected void output() throws IOException {
        String filename = makeFilename();

        if (SEPARATE_OUTPUTS) {
            this.separateOutputs(filename);

        } else {
            this.singleOutput(filename);
        }

        this.addDbTask(this::runDbInsert);
    }

    public String makeFilename() {
        LocalDateTime useForFilename;
        if (this.scanTimestamp != null) {
            useForFilename = this.scanTimestamp.toLocalDateTime();

        } else {
            useForFilename = LocalDateTime.now();
        }

        String timestampStr = filenameFormatter.format(useForFilename);
        Integer hostId = this.host.getHostId();

        return OUTPUT_DIR + timestampStr + "_" + scanResponse.getId()
                + "_" + hostId;
    }

    protected void singleOutput(String filePrefix) throws IOException {
        List<HostVulnerabilityOutput> list = this.output.getVulnerabilities();
        if (list == null || list.size() <= 0) {
            this.output.setFilename(null);
            return;
        }

        String filename = filePrefix + ".json";

        this.output.setFilename(filename);

        logger.info("Writing " + output.size() + " results to " + filename);

        LocalDateTime outputTime = LocalDateTime.now();
        this.output.setOutputTimestamp(Timestamp.valueOf(outputTime));

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename))) {

            SplunkOutputMapper mapper = SplunkOutputMapper.get();

            for (HostVulnerabilityOutput hvo : list) {
                mapper.writeValue(writer, hvo);
                writer.write("\n");
            }
        }
    }

    protected void separateOutputs(String filePrefix) throws IOException {
        this.output.setFilename(filePrefix + "_?.json");
        int i = 0;
        SplunkOutputMapper mapper = SplunkOutputMapper.get();

        logger.info("Writing " + output.size() + " separate results to files " + filePrefix + "_?.json");

        LocalDateTime outputTime = LocalDateTime.now();

        for (HostVulnerabilityOutput out : this.output.getVulnerabilities()) {
            String filename = filePrefix + "_" + (i++) + ".json";
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename))) {
                mapper.writeValue(writer, out);
            }
        }

        this.output.setOutputTimestamp(Timestamp.valueOf(outputTime));
    }

    protected void runDbInsert() {
        ScanHostResponse.dao.saveOrUpdate(this.response);
        HostOutput.dao.saveOrUpdate(this.output);
    }

    private int fetchCount = 0;

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        switch (stage) {
            case IDLE: break;

            case FETCH:
                String responseStr = this.client != null ? this.client.getResponse() : "";
                this.client = null;

                logger.error("Error fetching scan host response id "
                        + host.getId() + "\n" + responseStr, e);
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
    public int compareTo(DbManagerJob.Child o) {
        if (o == this) return 0;
        if (!(o instanceof HostVulnsJob)) return 1;
        HostVulnsJob other = (HostVulnsJob)o;
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
