package nessusTools.client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

import javax.json.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.*;

import nessusTools.client.response.*;
import nessusTools.data.entity.*;

import static org.hibernate.cfg.AvailableSettings.URL;


public class NessusClient extends GenericClient {
    public static final String PROPERTIES_FILE = "nessus-api.properties";
    private static final Logger logger = LogManager.getLogger(NessusClient.class);

    private static final Properties properties = loadProperties();

    public static final String apiProtocol = properties.getProperty("api.url.protocol", "https");
    public static final String apiHost = properties.getProperty("api.url.host", "localhost");
    public static final String apiPort = properties.getProperty("api.url.port", "");
    public static final String apiURL = apiProtocol + "://" + apiHost
            + (apiPort.length() > 0 ? ":" + apiPort : "");

    private static final String apiAccessKey = properties.getProperty("api.key.access");
    private static final String apiSecretKey = properties.getProperty("api.key.secret");
    private static final String apiFullKey = "accessKey=" + apiAccessKey +"; secretKey=" + apiSecretKey;
    private static final String apiKeyHeaderKey = "X-ApiKeys";

    private static final Map<String, String> apiHeaders = Map.of(apiKeyHeaderKey, apiFullKey);

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


    public NessusClient() { }

    public void fetchAllScans() {
        IndexResponse response = null;
        try {
            response = fetchJson(
                    IndexResponse.pathFor(),
                    IndexResponse.class);

        } catch (JsonProcessingException e) {
            logger.error(e);
            return;
        }

        if (response == null) return;

        List<Folder> folders = response.getFolders();
        List<Scan> scans = response.getScans();

        logger.info(response);

        for (Folder folder : folders) {
            Folder.dao.saveOrUpdate(folder);
        }

        for (Scan scan : scans) {
            Scan.dao.saveOrUpdate(scan);
        }

        for (Scan scan: scans) {
            ScanInfoResponse infoResponse = fetchScanInfo(scan);
            ScanInfoResponse.logger.info(infoResponse);

            if (infoResponse == null) {
                continue;
            }

            ScanInfo info = infoResponse.getInfo();
            if (info != null) {
                ScanInfo.dao.saveOrUpdate(info);
            }
        }
    }

    public ScanInfoResponse fetchScanInfo (Scan scan) {
        if (scan != null) {
            return fetchScanInfo(scan.getId());

        } else {
            return null;
        }
    }

    public ScanInfoResponse fetchScanInfo(int scanId) {
        try {
            ScanInfoResponse response = fetchJson(
                    ScanInfoResponse.pathFor(scanId),
                    ScanInfoResponse.class);

            if (response != null) {
                response.setId(scanId);
            }
            return response;


        } catch (JsonProcessingException e) {
            logger.error(e);
            return null;
        }
    }


    @Override
    public <R> R fetchJson(String pathOnly, // do not include the protocol, host, or port
                           Map<String, String> headers,
                           Class<R> mapToType,
                           ObjectMapper mapper)
            throws JsonProcessingException {

        if (pathOnly == null || pathOnly.length() < 1) {
            throw new IllegalArgumentException("nessusClient.fetchJson URL pathOnly string cannot be null or empty");

        } else if (pathOnly.charAt(0) != '/') {
            throw new IllegalArgumentException(
                    "nessusClient.fetchJson URL pathOnly string must start with '/' , was: '"
                            + pathOnly + "'");
        }

        String URL = apiURL + pathOnly;

        if (headers == null) {
            return super.fetchJson(URL, apiHeaders, mapToType, mapper);

        } else {
            Map<String, String> combinedHeaders = new LinkedHashMap();
            combinedHeaders.putAll(apiHeaders);
            combinedHeaders.putAll(headers);
            return super.fetchJson(URL, combinedHeaders, mapToType, mapper);
        }
    }
}
