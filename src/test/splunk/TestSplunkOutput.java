package splunk;



import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.splunk.*;
import nessusTools.data.persistence.*;
import org.hibernate.*;
import org.junit.*;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;


import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

@RunWith(Parameterized.class)
public class TestSplunkOutput {
    public static final String OUTPUT_DIR = "splunk-output/";
    private static final Logger logger = LogManager.getLogger(TestSplunkOutput.class);

    static {
        ScanResponse.dao.holdSession();
    }
    private static List<ScanResponse> scanResponses = ScanResponse.dao.getAll();

    @Parameterized.Parameters
    public static Collection getTestParams() {
        ScanHostResponse.dao.holdSession();
        try {
            Hibernate.initialize(scanResponses);
            return makeParams();

        } finally {
            ScanHostResponse.dao.releaseSession();
            ScanResponse.dao.releaseSession();
        }
    }

    private static Collection makeParams() {
        List<ScanHostResponse> list = ScanHostResponse.dao.getAll();
        Hibernate.initialize(list);

        List<Object[]> params = new LinkedList<>();

        for (ScanHostResponse response : list) {
            if (response == null) continue;
            Hibernate.initialize(response);

            ScanHost host = response.getHost();
            if (host != null) {
                Hibernate.initialize(host);

                ScanResponse sr = host.getResponse();
                if (sr != null) {
                    Hibernate.initialize(sr);

                    int id = sr.getId();
                    if (id != 0) {

                        for (ScanResponse res : scanResponses) {
                            if (res == null) continue;
                            Hibernate.initialize(res);

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
            if (vulns == null) continue;

            for (Vulnerability vuln : vulns) {
                if (vuln != null) {
                    params.add(new Object[]{
                            response,
                            vuln
                    });
                }
            }
        }

        return params;
    }

    private static List<ObjectNode> results = new LinkedList<>();

    private final ScanHostResponse response;
    private final Vulnerability vuln;

    public TestSplunkOutput(ScanHostResponse response, Vulnerability vuln) {
        this.response = response;
        this.vuln = vuln;
    }

    @Test
    public void run() {
        logger.info ("Creating SplunkOutput for response id " + this.response.getId()
                + ", vulnerability id " + this.vuln.getId());

        SplunkOutput output = new SplunkOutput(this.response, this.vuln);
        SplunkOutput.dao.saveOrUpdate(output);

        ObjectNode node = output.toJsonNode();

        assertNotNull(node);
        logger.info("\n" + node.toString());

        results.add(node);

        SplunkOutput.dao.holdSession();
        SplunkOutput persisted = SplunkOutput.dao.getById(output.getId());

        Timestamp orig = output.getTimestamp();
        Timestamp pers = persisted.getTimestamp();
        if (orig.getTime() != pers.getTime()
            && ((long)Math.round((double)orig.getTime() / 1000) * 1000)
                        == pers.getTime()) {

            output.setTimestamp(pers);
        }

        assertEquals(output, persisted);
        SplunkOutput.dao.releaseSession();
    }



    @AfterClass
    public static void write() {
        LocalDateTime now = LocalDateTime.now();
        String filename = OUTPUT_DIR + now.toString();

        filename = filename.replace(":", ".");

        logger.info("Writing " +  results.size() + " results to " + filename);

        try (OutputStreamWriter writer
                     = new OutputStreamWriter(
                            new FileOutputStream(filename))) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, results);

        } catch (FileNotFoundException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }

        //Hash.printAverages();
    }
}
