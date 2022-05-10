package nessusTools.run;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.client.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetFileInputJob<R extends NessusResponse> extends Job {
    private static final Logger logger = LogManager.getLogger(GetFileInputJob.class);

    private final Class<R> responseType;
    private final String jsonFile;
    private String origJson = "";
    private final Lambda1<ObjectNode, Integer> initialize;
    private Integer idToSet;
    private ObjectNode node;
    private R response;
    private List<Job> after;

    public GetFileInputJob(String jsonFile,
                           Class<R> responseType,
                           Lambda1<ObjectNode, Integer> initialize,
                           List<Job> after) {
        this.responseType = responseType;
        this.jsonFile = PARAMS_DIR + jsonFile;
        this.initialize = initialize;
        this.after = after;
    }

    public void addJobForAfter(Job job) {
        if (this.after == null) {
            this.after = new LinkedList<>();
        }
        this.after.add(job);
    }


    @Override
    protected boolean isReady() {
        return true;
    }

    @Override
    protected void fetch(NessusClient client) {
        logger.info("Fetching JSON: " + jsonFile);
        long start = System.nanoTime();
        this.fetchJson();
        long time = System.nanoTime() - start;

        logger.info("Fetch took " + (time / BILLION));
    }

    @Override
    protected void process() {
        logger.info("Starting deserialization");
        long start = System.nanoTime();
        try {
            this.deserialize();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            logger.error(e);
        }
        long time = System.nanoTime() - start;

        logger.info("Deserialization took " + (time / BILLION) + ".  Starting persistence.");
    }

    @Override
    protected void output() {
        long start = System.nanoTime();
        this.persist();
        long time = System.nanoTime() - start;

        logger.info("Persistence took " + (time / BILLION));

        if (this.after != null) {
            this.addJobs(this.after);
        }

        if (this.responseType == ScanHostResponse.class) {
            //this.addJob(new SplunkOutputJob(((ScanHostResponse)this.response).getOrFetchScanResponse() , (ScanHostResponse)this.response));
        }
    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        return false;
    }



    public static final String PARAMS_DIR = "deserializationPersistence-params/";
    public static final double BILLION = 1000000000;



    public void fetchJson() {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.jsonFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                origJson += line;
            }

        } catch (IOException e) {
            fail(e);
        }

        assertNotNull(origJson);
        assertNotEquals("", origJson);
    }

    public void deserialize()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {

        ObjectMapper mapper = new ObjectMapper();

        try {

            // https://stackoverflow.com/questions/12173416/how-do-i-get-the-compact-form-of-pretty-printed-json-code
            this.node = mapper.readValue(origJson, ObjectNode.class);

        } catch (JsonProcessingException e) {
            fail(e);
            return;
        }

        assertNotNull(this.node);

        if (this.initialize != null) {
            this.idToSet = this.initialize.call(this.node);
        }

        try {
            response = mapper.readValue(origJson, this.responseType);

        } catch (JsonProcessingException e) {
            fail(e);
            return;
        }

        assertNotNull(this.response);

        if (this.idToSet != null) {
            this.response.setId(this.idToSet);
        }

        assertEquals(node, response.toJsonNode());
    }

    public void persist() {

        // Put the deserialized objects into the persistence layer
        Dao<R> dao = Dao.get(responseType);
        R persisted = null;

        try {
            dao.saveOrUpdate(this.response);
            persisted = dao.getById(this.response.getId());

        } catch (Exception e) {
            fail(e);
        }

        assertNotNull(persisted);
        assertEquals(response, persisted);
        assertEquals(node, persisted.toJsonNode());
    }
}
