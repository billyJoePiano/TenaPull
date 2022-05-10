package nessusTools.client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import org.apache.logging.log4j.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.*;


/**
 * Abstract generic client with methods for fetching resources at URLS, adding HTTP headers,
 * and JSON deserialization
 */
public abstract class GenericClient {
    private static final Logger logger = LogManager.getLogger(GenericClient.class);

    private final Client client;

    private String response;

    /**
     * Instantiates a new Generic client.
     *
     * @param acceptAnySSL whether to construct a normal client, or a client which
     * accepts any SSL certificate using the AcceptAnySSL static utility
     */
    public GenericClient(boolean acceptAnySSL) {
        if (acceptAnySSL) {
            this.client = AcceptAnySSL.makeClient();

        } else {
            this.client = ClientBuilder.newClient();
        }
    }

    /**
     * Gets the last response string fetched by this client.  Typically used in the event
     * of exceptions being thrown during deserialization of the string.
     *
     * @return the most recent response string
     */
    public String getResponse() {
        return this.response;
    }

    /**
     * Fetches the resource at the provided URL and returns it as a string.
     *
     * @param URL the url to fetch
     * @return the string returned by the server at the given URL
     */
    public String fetch(String URL) {
        return fetch(URL, null, null, String.class);
    }

    /**
     * Fetches the resource at the provided URL and returns it as a string
     *
     * @param URL       the url to fetch
     * @param headers   the HTTP headers to include
     * @param mediaType the HTTP mime type
     * @return the string return by the server at the given URL
     */
    public String fetch(String URL,
                         Map<String, String> headers,
                         MediaType mediaType) {
        return fetch(URL, headers, mediaType, String.class);
    }

    /**
     * Fetches the resource at the provided URL and returns it as the class provided in
     * the responseClass argument.
     *
     * @param <R>           The responseType
     * @param URL           the url to fetch
     * @param responseType the responseType
     * @return the response returned by the server at the given URL, converted into the responseType
     */
    public <R> R fetch(String URL, Class<R> responseType) {
        return fetch(URL, null, null, responseType);
    }

    /**
     * Fetches the resource at the provided URL and returns it as the class provided in
     * the responseClass argument.
     *
     * @param <R>           the responseType
     * @param URL           the url to fetch
     * @param headers       the HTTP headers to include
     * @param mediaType     the HTTP mime type
     * @param responseType the response type
     * @return the response returned by the server at the given URL, converted into the responseType
     */
    public <R> R fetch(String URL,
                        Map<String, String> headers,
                        MediaType mediaType,
                        Class<R> responseType) {

        WebTarget target = client.target(URL);

        Invocation.Builder builder;
        if (mediaType != null) {
            builder = target.request(mediaType);

        } else {
            builder = target.request();
        }

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
        }

        return builder.get(responseType);
    }

    /**
     * Fetches the resource at the given url and converts it into a JsonNode using a standard
     * Jackson ObjectMapper
     *
     * @param URL the url to fetch
     * @return the response returned by the server at the given URL, converted into a JsonNode
     * @throws JsonProcessingException if the Jackson ObjectMapper runs into a problem converting
     * the response into a JsonNode
     */
    public JsonNode fetchJson(String URL)
            throws JsonProcessingException {

        return fetchJson(URL,  null, JsonNode.class, null);
    }

    /**
     * Fetch json json node.
     *
     * @param URL     the url to fetch
     * @param headers the HTTP headers to include
     * @return the response returned by the server at the given URL, converted into a JsonNode
     * @throws JsonProcessingException if the Jackson ObjectMapper runs into a problem converting
     * the response into a JsonNode
     */
    public JsonNode fetchJson(String URL,
                              Map<String, String> headers)
            throws JsonProcessingException {

        return fetchJson(URL, headers, JsonNode.class,null);
    }

    /**
     * Fetches the resource at the given url and converts it into the given mapToType
     * using a standard Jackson ObjectMapper
     *
     * @param <T>       The type to map the response to
     * @param URL       the url to fetch
     * @param mapToType the type to convert the returned JSON into
     * @return the deserialized response
     * @throws JsonProcessingException if the Jackson ObjectMapper runs into a problem converting
     * the response into the mapToType
     */
    public <T> T fetchJson(String URL, Class<T> mapToType)
            throws JsonProcessingException {

        return fetchJson(URL, null, mapToType, null);
    }

    /**
     * Fetches the resource at the given url and converts it into the given mapToType
     * using a standard Jackson ObjectMapper
     *
     * @param <T>       the type to map the response to
     * @param URL       the url to fetch
     * @param headers   the HTTP headers to include
     * @param mapToType the type to convert the returned JSON into
     * @return the deserialized response
     * @throws JsonProcessingException if the Jackson ObjectMapper runs into a problem converting
     * the response into a JsonNode
     */
    public <T> T fetchJson(String URL,
                              Map<String, String> headers,
                              Class<T> mapToType)
            throws JsonProcessingException {

        return fetchJson(URL, headers, mapToType, null);
    }

    /**
     * Fetches the resource at the given url and converts it into the given mapToType
     * using the provided Jackson ObjectMapper.  Can be used for customized mapping
     *
     * @param <R>       the type to map the response to
     * @param URL       the url to fetch
     * @param headers   the HTTP headers to include
     * @param mapToType the type to convert the returned JSON into
     * @param mapper    the Jackson ObjectMapper to use
     * @return the deserialized response
     * @throws JsonProcessingException the json processing exception
     */
    public <R> R fetchJson(String URL,
                           Map<String, String> headers,
                           Class<R> mapToType,
                           ObjectMapper mapper)
            throws JsonProcessingException {

        String response = fetch(URL, headers, MediaType.APPLICATION_JSON_TYPE);
        this.response = response;

        if (response == null || response.length() < 2) {
            //JSON needs at least two characters to be valid
            // e.g. {} or [] or ""
            return null;
        }

        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        return mapper.readValue(response, mapToType);
    }
}
