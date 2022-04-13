package client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.client.*;
import nessusTools.client.response.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.*;
import nessusTools.data.entity.template.*;

import org.junit.*;

import java.sql.*;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;


public class TestNessusClient {
    public static final Logger logger = LogManager.getLogger(TestNessusClient.class);

    @Before
    public void resetDB() {
        Database.hardReset();
    }

    @Test
    public void testFetchAllScans()
            throws JsonProcessingException {

        NessusClient client = new NessusClient();
        client.fetchAllScans();

        List<Folder> folders = Folder.dao.getAll();
        assertNotEquals(0, folders.size());

        List<Scan> scans = Scan.dao.getAll();
        List<ScanInfo> infos = ScanInfo.dao.getAll();
        assertNotEquals(0, scans.size());
        assertEquals(scans.size(), infos.size());


        logger.info("FETCHED FOLDERS:");
        List<Scan> scansFromFolders = new ArrayList(scans.size());
        for (Folder folder : folders) {
            Folder.logger.info(folder);
            scansFromFolders.addAll(folder.getScans());
        }

        assertEquals(scans.size(), scansFromFolders.size());

        Comparator<DbPojo> comparator = (a, b) -> a.getId() - b.getId();
        scans.sort(comparator);
        scansFromFolders.sort(comparator);
        infos.sort(comparator);

        logger.info("FETCHED SCANS:");

        for (int i = 0; i < scans.size(); i++) {
            Scan scan = scans.get(i);
            ScanInfo info = infos.get(i);
            Scan fromFolders = scansFromFolders.get(i);

            Scan.logger.info(scan);
            ScanInfo.logger.info(info);

            assertEquals(scan, info.getScan());
            assertEquals(scan, fromFolders);
        }


        //re-fetch as raw JsonNodes:
        ObjectMapper mapper = new ObjectMapper();
        JsonNode indexJson = client.fetchJson(IndexResponse.pathFor());

        TypeReference<List<JsonNode>> typeReference =
                new TypeReference<List<JsonNode>>() {};

        List<JsonNode> foldersJson
                = mapper.convertValue(indexJson.get("folders"), typeReference);

        List<JsonNode> scansJson
                = mapper.convertValue(indexJson.get("scans"), typeReference);


        assertEquals(folders.size(), foldersJson.size());
        assertEquals(scans.size(), scansJson.size());

        Comparator<JsonNode> jsonComparator
                = (a, b) -> a.get("id").intValue() - b.get("id").intValue();

        foldersJson.sort(jsonComparator);
        scansJson.sort(jsonComparator);

        for (int i = 0; i < folders.size(); i++) {
            Folder folder = folders.get(i);
            JsonNode json = foldersJson.get(i);
            assertEquals(json, folder.toJsonNode());
        }

        for (int i = 0; i < scans.size(); i++) {
            Scan scan = scans.get(i);
            JsonNode json = scansJson.get(i);

            if (Objects.equals(scan.getStatus().toString(), "running")
                    || !Objects.equals(scan.getLiveResults(), Integer.valueOf(0))) {

                // indicates this scan is currently running ... set modification timestamps to the same
                Timestamp timestamp = EpochTimestamp.Deserializer.deserialize(
                                        json.get("last_modification_date").toString());

                scan.setLastModificationDate(timestamp);
            }

            assertEquals(json, scan.toJsonNode());



            JsonNode infoJson = client.fetchJson(ScanResponse.pathFor(scan)).get("info");
            ScanInfo info = infos.get(i);

            if (Objects.equals(info.getStatus().toString(), "running")) {
                ObjectNode on = (ObjectNode) infoJson;

                Timestamp scannerEnd = info.getScannerEnd();
                if (scannerEnd != null) {
                    on.put("scanner_end", scannerEnd.getTime() / 1000);

                } else {
                    on.putNull("scanner_end");
                }

                Timestamp scanEnd = info.getScanEnd();
                if (scanEnd != null) {
                    on.put("scan_end", scanEnd.getTime() / 1000);

                } else {
                    on.putNull("scan_end");
                }

                String severityProcessed = info.getSeverityProcessed();
                if (severityProcessed != null) {
                    on.put("severity_processed", severityProcessed);

                } else {
                    on.putNull("severity_processed");
                }
            }

            assertEquals(infoJson, info.toJsonNode());
        }
    }
}
