package client;

import com.fasterxml.jackson.core.type.*;
import nessusTools.client.*;
import nessusTools.data.entity.*;
import nessusTools.data.entity.template.*;

import nessusTools.data.persistence.*;
import org.junit.*;
import testUtils.*;

import java.security.*;
import java.util.*;

import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


import org.junit.runners.MethodSorters;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

import javax.ws.rs.client.*;


public class TestClient {
    @Before
    public void resetDB() {
        Database.hardReset();
    }

    @Test
    public void testFetchAllScans() throws NoSuchAlgorithmException, KeyManagementException {
        NessusClient client = new NessusClient();
        client.fetchAllScans();

        List<Folder> folders = Folder.dao.getAll();
        assertNotEquals(0, folders.size());

        List<Scan> scans = Scan.dao.getAll();
        List<ScanInfo> infos = ScanInfo.dao.getAll();
        assertNotEquals(0, scans.size());
        assertEquals(scans.size(), infos.size());

        System.out.println("FOLDERS:");

        List<Scan> scansFromFolders = new ArrayList(scans.size());
        for (Folder folder : folders) {
            System.out.println();
            System.out.println(folder);
            scansFromFolders.addAll(folder.getScans());
        }

        assertEquals(scans.size(), scansFromFolders.size());

        Comparator<Pojo> comparator = (a, b) -> a.getId() - b.getId();
        scans.sort(comparator);
        scansFromFolders.sort(comparator);
        infos.sort(comparator);

        System.out.println();
        System.out.println();
        System.out.println("SCANS");

        for (int i = 0; i < scans.size(); i++) {
            Scan scan = scans.get(i);
            ScanInfo info = infos.get(i);
            Scan fromFolders = scansFromFolders.get(i);

            System.out.println();
            System.out.println(scan);
            System.out.println(info);

            assertEquals(scan, info.getScan());
            assertEquals(scan, fromFolders);
        }


        //re-fetch as raw JsonNodes:
        ObjectMapper mapper = new ObjectMapper();
        JsonNode indexJson = client.fetch("scans");

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
            assertEquals(json, scan.toJsonNode());

            JsonNode infoJson = client.fetch("scans/" + scan.getId()).get("info");
            ScanInfo info = infos.get(i);
            assertEquals(infoJson, info.toJsonNode());
        }
    }
}
