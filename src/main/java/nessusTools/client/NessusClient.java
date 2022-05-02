package nessusTools.client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;


public class NessusClient extends GenericClient {
    public static final String PROPERTIES_FILE = "nessus-api.properties";
    private static final Logger logger = LogManager.getLogger(NessusClient.class);

    private static final Properties properties = loadProperties();

    public static final String API_PROTOCOL = properties.getProperty("api.url.protocol", "https");
    public static final String API_HOST = properties.getProperty("api.url.host", "localhost");
    public static final String API_PORT = properties.getProperty("api.url.port", "");
    public static final String API_URL = API_PROTOCOL + "://" + API_HOST
            + (API_PORT.length() > 0 ? ":" + API_PORT : "");

    public static final boolean ACCEPT_ANY_SSL = properties.containsKey("client.acceptAnySSL");

    private static final String API_ACCESS_KEY = properties.getProperty("api.key.access");
    private static final String API_SECRET_KEY = properties.getProperty("api.key.secret");
    private static final String API_FULL_KEY = "accessKey=" + API_ACCESS_KEY +"; secretKey=" + API_SECRET_KEY;
    private static final String API_KEY_HEADER_KEY = "X-ApiKeys";

    private static final Map<String, String> API_HEADERS = Map.of(API_KEY_HEADER_KEY, API_FULL_KEY);

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


    public NessusClient() {
        super(ACCEPT_ANY_SSL);
    }

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
            ScanResponse infoResponse = fetchScanInfo(scan);
            ScanResponse.logger.info(infoResponse);

            if (infoResponse == null) {
                continue;
            }

            ScanInfo info = infoResponse.getInfo();
            if (info != null) {
                ScanInfo.dao.saveOrUpdate(info);
            }
        }
    }

    public ScanResponse fetchScanInfo (Scan scan) {
        if (scan != null) {
            return fetchScanInfo(scan.getId());

        } else {
            return null;
        }
    }

    public ScanResponse fetchScanInfo(int scanId) {
        try {
            ScanResponse response = fetchJson(
                    ScanResponse.getUrlPath(scanId),
                    ScanResponse.class);

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

        String URL = API_URL + pathOnly;

        if (headers == null) {
            return super.fetchJson(URL, API_HEADERS, mapToType, mapper);

        } else {
            Map<String, String> combinedHeaders = new LinkedHashMap();
            combinedHeaders.putAll(API_HEADERS);
            combinedHeaders.putAll(headers);
            return super.fetchJson(URL, combinedHeaders, mapToType, mapper);
        }
    }
}
