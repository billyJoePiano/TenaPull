package nessusTools.client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.*;

import nessusTools.client.response.*;
import nessusTools.data.entity.*;


public class NessusClient {
    public static final String PROPERTIES_FILE = "nessus-api.properties";
    private static Logger logger = LogManager.getLogger(NessusClient.class);

    private static final Properties properties = loadProperties();
    private static final String apiURL = properties.getProperty("api.url");
    private static final String apiAccessKey = properties.getProperty("api.key.access");
    private static final String apiSecretKey = properties.getProperty("api.key.secret");
    private static final String apiFullKey = "accessKey=" + apiAccessKey +"; secretKey=" + apiSecretKey;
    private static final String apiKeyHeaderKey = "X-ApiKeys";

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(PROPERTIES_FILE));

        } catch (IOException ioe) {
            logger.error("NessusClient.loadProperties()...Cannot load the properties file", ioe);
        } catch (Exception e) {
            logger.error("NessusClient.loadProperties()...", e);
        }

        return properties;
    }


    private Client client = AcceptAnySSL.makeClient();

    public NessusClient() { }

    public JsonNode fetch (String path) {
        return this.fetch(path, JsonNode.class);
    }

    public <R> R fetch (String path, Class<R> responseClass) {


        WebTarget target = client.target(apiURL + path);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
        builder.header(apiKeyHeaderKey, apiFullKey);
        String response = builder.get(String.class);

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(response, responseClass);

        } catch (JsonProcessingException e) {
            logger.error(e);
            return null;
        }
    }


    public void fetchAllScans() {
        IndexResponse response = fetch("scans", IndexResponse.class);
        if (response == null) return;

        List<Folder> folders = response.getFolders();
        List<Scan> scans = response.getScans();

        for (Folder folder : folders) {
            Folder.dao.saveOrUpdate(folder);
        }

        for (Scan scan : scans) {
            Scan.dao.saveOrUpdate(scan);
        }

        for (Scan scan: scans) {
            ScanInfo info = fetchScanInfo(scan.getId());
            if (info != null) {
                ScanInfo.dao.saveOrUpdate(info);
            }
        }
    }

    public ScanInfo fetchScanInfo(int scanId) {
        ScanInfo info = fetch("scans/" + scanId, ScanInfoResponse.class).getInfo();
        info.setId(scanId);
        return info;
    }




}
