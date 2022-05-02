package nessusTools.run;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.splunk.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplunkOutputJob extends Job {
    public static final String OUTPUT_DIR = "splunk-output/";
    private static final Logger logger = LogManager.getLogger(SplunkOutputJob.class);

    private final ScanHostResponse response;

    private List<Vulnerability> vulns;

    private final List<SplunkOutput> outputs = new LinkedList<>();

    public SplunkOutputJob(ScanHostResponse hostResponse) {
        this.response = hostResponse;
    }

    @Override
    protected boolean isReady() {
        if (response != null) return true;
        this.failed();
        return false;
    }

    @Override
    protected void fetch() {
        this.vulns = response.getVulnerabilities();
        response._prepare();
    }

    @Override
    protected void process() {
        for (Vulnerability vuln : this.vulns) {
            outputs.add(new SplunkOutput(this.response, vuln));
        }
        for (SplunkOutput output : this.outputs) {
            SplunkOutput.dao.saveOrUpdate(output);
            SplunkOutput persisted = null;
            SplunkOutput.dao.holdSession();
            try {
                persisted = SplunkOutput.dao.getById(output.getId());

                Timestamp orig = output.getTimestamp();
                Timestamp pers = persisted.getTimestamp();
                if (orig.getTime() != pers.getTime()
                        && ((long) Math.round((double) orig.getTime() / 1000) * 1000)
                        == pers.getTime()) {

                    output.setTimestamp(pers);
                }

                assertEquals(output, persisted);

            } catch(Throwable e) {
                throw e;

            } finally {
                SplunkOutput.dao.releaseSession();
            }
        }
    }

    @Override
    protected void output() {
        LocalDateTime now = LocalDateTime.now();
        String filename = OUTPUT_DIR + now + " " + response.getId();

        filename = filename.replace(":", ".");

        logger.info("Writing " +  outputs.size() + " results to " + filename);

        try (OutputStreamWriter writer
                     = new OutputStreamWriter(
                new FileOutputStream(filename))) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, outputs);

        } catch (FileNotFoundException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }

    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        return false;
    }
}
