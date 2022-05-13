package tenapull.client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import tenapull.run.*;
import org.apache.logging.log4j.*;

import java.util.*;


/**
 * The NessusClient for fetching response from a specific Nessus installation provided in the configurations
 * first parsed by Main
 */
public class NessusClient extends GenericClient {
    private static final Logger logger = LogManager.getLogger(NessusClient.class);

    /**
     * Url of the Nessus installation's API which will be queried
     */
    public static final String API_URL;

    /**
     * Whether to accept any SSL certificate
     */
    public static final boolean ACCEPT_ANY_SSL;

    /**
     * The Nessus API access key provided in the configuration file
     */
    private static final String API_ACCESS_KEY;

    /**
     * The Nessus API secret key provided in the configuration file
     */
    private static final String API_SECRET_KEY;

    static {
        Properties properties = Main.getConfig();
        API_URL = properties.getProperty("api.url");
        ACCEPT_ANY_SSL = properties.containsKey("client.acceptAnySSL");
        API_ACCESS_KEY = properties.getProperty("api.key.access");
        API_SECRET_KEY = properties.getProperty("api.key.secret");
    }

    /**
     * The access key and secret key concatonated, for the "value" side of the HTTP header
     */
    private static final String API_FULL_KEY = "accessKey=" + API_ACCESS_KEY +"; secretKey=" + API_SECRET_KEY;

    /**
     * The "key" side of the HTTP header with the API access keys
     */
    private static final String API_KEY_HEADER_KEY = "X-ApiKeys";

    /**
     * A map to be used for the HTTP headers for API access
     */
    private static final Map<String, String> API_HEADERS = Map.of(API_KEY_HEADER_KEY, API_FULL_KEY);


    /**
     * Instantiates a new Nessus client using the ACCEPT_ANY_SSL parameter as provided
     * in the configuration file
     */
    public NessusClient() {
        super(ACCEPT_ANY_SSL);
    }


    /**
     * A convenience method which automatically includes the API access keys in the HTTP header,
     * and adds the full protocol, domain name, and port (if applicable) to the URL.  NOTE:  DO
     * NOT INCLUDE THE PROTOCOL, DOMAIN NAME, AND PORT IN THE URL PASSED TO THIS METHOD.
     * <br>
     * Also note that due to the way overloaded fetchJson methods are invoked in the GenericClient,
     * this method is the end-point method for all the fetchJson methods inherited from
     * GenericClient.
     *
     *
     * @param pathOnly the path of the resource to be fetched.  Do not include protocol, domain, or port
     * @param headers   Additional HTTP headers to include
     * @param mapToType the type to convert the returned JSON into
     * @param mapper    the Jackson ObjectMapper to use
     * @param <R>
     * @return
     * @throws JsonProcessingException
     */
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
