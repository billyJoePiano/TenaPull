package nessusTools.run;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.splunk.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplunkOutputJob extends Job {
    public static final String OUTPUT_DIR = "splunk-output/";
    private static final Logger logger = LogManager.getLogger(SplunkOutputJob.class);

    private final ScanResponse scanResponse;
    private final ScanHostResponse hostResponse;

    private List<Vulnerability> vulns;

    private final List<SplunkOutput> outputs = new LinkedList<>();

    public SplunkOutputJob(ScanResponse scanResponse, ScanHostResponse hostResponse) {
        this.scanResponse = scanResponse;
        this.hostResponse = hostResponse;
    }

    @Override
    protected boolean isReady() {
        if (hostResponse != null) return true;
        this.failed();
        return false;
    }

    @Override
    protected void fetch() {
        this.vulns = hostResponse.getVulnerabilities();
        hostResponse._prepare();
    }

    @Override
    protected void process() {
        for (Vulnerability vuln : this.vulns) {
            outputs.add(new SplunkOutput(this.hostResponse, vuln));
        }
        for (SplunkOutput output : this.outputs) {
            SplunkOutput.dao.saveOrUpdate(output);
        }
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withZone(TimeZone.getDefault().toZoneId());


    @Override
    protected void output() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = formatter.format(now);

        String filename = OUTPUT_DIR + dateStr + "." + scanResponse.getId()
                + "." + hostResponse.getId() + ".json";

        logger.info("Writing " +  outputs.size() + " results to " + filename);

        try (OutputStreamWriter writer
                     = new OutputStreamWriter(
                new FileOutputStream(filename))) {

            SplunkOutputMapper.mapper.writerWithDefaultPrettyPrinter().writeValue(writer, outputs);

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
