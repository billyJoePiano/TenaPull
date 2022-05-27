package splunk;



import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.scan.*;
import tenapull.data.entity.splunk.*;
import tenapull.run.*;
import org.hibernate.*;
import org.junit.*;

import java.sql.*;
import java.time.*;
import java.util.*;


import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

/**
 * Tests the outputs produced for Splunk, using whatever data is pre-existing in the database
 */
@RunWith(Parameterized.class)
public class TestSplunkOutput {
    private static final Logger logger = LogManager.getLogger(TestSplunkOutput.class);

    static {
        Main.loadTestConfig();

        if (!Main.initOutputDir()) {
            throw new IllegalStateException("Could not initialize output directory");

        } else if (!Main.initDbConnection()) {
            throw new IllegalStateException("Could not initialize database connection");
        }

        ScanResponse.dao.holdSession();
    }
    private static List<ScanResponse> scanResponses = (List<ScanResponse>)Hibernate.unproxy(ScanResponse.dao.getAll());

    /**
     * Obtains the test params from the database's existing data
     *
     * @return the test params
     */
    @Parameterized.Parameters
    public static Collection getTestParams() {
        ScanHostResponse.dao.holdSession();
        try {
            return makeParams();

        } finally {
            ScanHostResponse.dao.releaseSession();
            ScanResponse.dao.releaseSession();
        }
    }

    private static Collection makeParams() {
        List<ScanHostResponse> list = ScanHostResponse.dao.getAll();
        list = (List<ScanHostResponse>)Hibernate.unproxy(list);

        List<Object[]> params = new LinkedList<>();

        for (ScanHostResponse response : list) {
            if (response == null) continue;
            response = (ScanHostResponse)Hibernate.unproxy(response);

            ScanHost host = response.getHost();
            if (host != null) {
                host = (ScanHost)Hibernate.unproxy(host);

                ScanResponse sr = host.getResponse();
                if (sr != null) {
                    sr = (ScanResponse)Hibernate.unproxy(sr);

                    int id = sr.getId();
                    if (id != 0) {

                        for (ScanResponse res : scanResponses) {
                            if (res == null) continue;
                            res = (ScanResponse)Hibernate.unproxy(res);

                            if (res.getId() == id) {
                                response.setScanResponse(res);
                                host.setResponse(res);
                                break;
                            }
                        }
                    }
                }
            }

            List<Vulnerability> vulns = response.getVulnerabilities();
            Hibernate.initialize(vulns);

            params.add(new Object[]{
                    response.getOrFetchScanResponse(),
                    host,
                    response,
                    vulns
                });

        }

        return params;
    }

    private static List<HostOutput> results = new LinkedList<>();

    private final ScanResponse scanResponse;
    private final ScanHost host;
    private final ScanHostResponse response;
    private final List<Vulnerability> vulns;

    /**
     * Instantiates a new Test splunk output using the provided responses, host, and vulnerabilities
     *
     * @param scanResponse the scan response
     * @param host         the host
     * @param response     the response
     * @param vulns        the vulns
     */
    public TestSplunkOutput(ScanResponse scanResponse, ScanHost host, ScanHostResponse response, List<Vulnerability> vulns) {
        this.scanResponse = scanResponse;
        this.host = host;
        this.response = response;
        this.vulns = vulns;
    }

    /**
     * Runs the test, producing a HostOutput record.  The record is serialized into a JsonNode
     * which is saved for writing to a file at the very end of all tests
     */
    @Test
    public void run() {
        logger.info ("Creating HostOutput for response id " + this.response.getId());

        int id = this.response.getId();

        HostOutput output = new HostOutput();
        output.setId(this.response.getId());
        output.setScanHostResponse(this.response);

        if (HostOutput.dao.getById(output.getId()) == null) {
            int insertId = HostOutput.dao.insert(output);
            assertEquals(id, output.getId());
            assertEquals(id, insertId);
        }

        List<HostVulnerabilityOutput> list = new ArrayList<>(this.vulns.size());

        for (Vulnerability vuln : vulns) {
            list.add(new HostVulnerabilityOutput(this.scanResponse, this.host, this.response, vuln, output));
        }

        output.setVulnerabilities(list);
        output.setOutputTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        output.useTimestampFrom(this.scanResponse);

        HostOutput.dao.saveOrUpdate(output);

        assertEquals(id, output.getId());

        JsonNode node = output.toJsonNode();

        assertNotNull(node);
        logger.info("\n" + node);

        results.add(output);

        HostOutput persisted = null;
        HostOutput.dao.holdSession();

        try {
            persisted = (HostOutput)Hibernate.unproxy(HostOutput.dao.getById(output.getId()));

            ScanHostResponse shr = (ScanHostResponse)Hibernate.unproxy(persisted.getScanHostResponse());
            persisted.setScanHostResponse(shr);

            //replace the persistent bag from Hibernate
            // ... it does not follow the List contract for .equals(), apparently :-(
            persisted.setVulnerabilities(new ArrayList(persisted.getVulnerabilities()));
            output.setVulnerabilities(new ArrayList(output.getVulnerabilities()));

            ScanResponse sr = shr.getOrFetchScanResponse();

            for (HostVulnerabilityOutput hvo : persisted.getVulnerabilities()) {
                hvo.setScanResponse(sr);
                ScanHost sh = (ScanHost)Hibernate.unproxy(hvo.getScanHost());
                hvo.setScanHost(sh);
            }


            output.setOutputTimestamp(persisted.getOutputTimestamp());

            Timestamp orig = output.getScanTimestamp();
            Timestamp pers = persisted.getScanTimestamp();
            if (orig.getTime() != pers.getTime()
                    && ((long) Math.round((double) orig.getTime() / 1000) * 1000)
                    == pers.getTime()) {

                output.setScanTimestamp(pers);
                output.setVulnerabilityTimestamps();
            }

            assertEquals(output, persisted);

        } catch(Throwable e) {
            throw e;

        } finally {
            HostOutput.dao.releaseSession();
        }

        try {
            OutputTestJob job = new OutputTestJob(output);
            job.doWrite();

        } catch (Throwable e) {
            throw e;
        }

    }


    /*
    @AfterClass
    public static void printHashAvgs {
        Hash.printAverages();
    }
     */

    private class OutputTestJob extends HostVulnsJob {
        public OutputTestJob(HostOutput testOutput) {
            super(TestSplunkOutput.this.scanResponse, TestSplunkOutput.this.host,
                    TestSplunkOutput.this.response, testOutput);
        }

        private void doWrite() {
            try {
                assertTrue(this.isReady());
                this.process();
                this.singleOutput(this.makeFilename());
                this.runDbInsert();


            } catch (Exception e) {
                fail(e);
            }

        }
    }
}
