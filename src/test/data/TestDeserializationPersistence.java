package data;


import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.junit.*;
import testUtils.*;

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

//https://www.guru99.com/junit-parameterized-test.html#:~:text=What%20is%20Parameterized%20Test%20in,their%20inputs%20and%20expected%20results.
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TestDeserializationPersistence<R extends NessusResponse> {
    public static final String PARAMS_DIR = "deserializationPersistence-params/";


    /******************************
     * Parameters for each test
     *
     * Different Response types and Expected Data responses will go into these parameters
     *
     ******************************/

    @Parameterized.Parameters
    public static Collection responseTypes()
            throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {

        Database.hardReset();
        StackTracePrinter.startThread(360000);
        StackTracePrinter.startThread(600000);

        return Arrays.asList(new Object[][] {
            { IndexResponse.class, "indexResponse.json" },
            { ScanResponse.class, "ScanInfo.json" }
        });
    }

    /*******************************
     * Instance variables/methods
     *******************************/

    private final Logger logger;

    private final Class<R> responseType;
    private final String jsonFile;

    public TestDeserializationPersistence(Class<R> responseType, String jsonFile) {
        this.logger = LogManager.getLogger(responseType);
        this.responseType = responseType;
        this.jsonFile = PARAMS_DIR + jsonFile;
    }

    @BeforeAll
    public static void dbReset() {
        Database.reset();
    }


    /**
     * Checks a dummy json API response from the 'index' level.  Dummy response is in
     * test/resource/indexResponse.json
     *
     */
    @Test
    public void run()
            throws InvocationTargetException, NoSuchMethodException,
                    InstantiationException, IllegalAccessException {

        String origJson = this.fetchJson();
        NodeAndResponse deserialized = deserialize(origJson);
        persist(deserialized);
    }

    public String fetchJson() {
        String origJson = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(this.jsonFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                origJson += line;
            }

        } catch (IOException e) {
            fail(e);
            return null;
        }

        assertNotNull(origJson);
        assertNotEquals("", origJson);

        return origJson;
    }

    public NodeAndResponse deserialize(String origJson)
            throws NoSuchMethodException, InvocationTargetException,
                    InstantiationException, IllegalAccessException {

        // To standardize the json string's format, first put into a JsonNode
        // then re-stringify to remove "prettifying" whitespace.
        // With the standardized/minified json, serialize it into the appropriate
        // DbPojo objects

        ObjectNode origNode;
        R deserialized;
        ObjectMapper mapper = new CustomObjectMapper();

        try {

            // https://stackoverflow.com/questions/12173416/how-do-i-get-the-compact-form-of-pretty-printed-json-code
            origNode = mapper.readValue(origJson, ObjectNode.class);

        } catch (JsonProcessingException e) {
            fail(e);
            return null;
        }

        JsonNode id = origNode.get("id");

        if (id != null) {
            origNode.remove("id");
            Dao<R> dao = Dao.get(this.responseType);
            R preExisting = dao.getById(id.intValue());
            if (preExisting == null) {
                preExisting = this.responseType.getDeclaredConstructor().newInstance();
                preExisting.setId(id.intValue());
                dao.insert(preExisting);
            }
        }

        try {
            deserialized = mapper.readValue(origJson, this.responseType);

        } catch (JsonProcessingException e) {
            fail(e);
            return null;
        }

        assertNotNull(origNode);
        assertNotNull(deserialized);

        assertEquals(origNode, deserialized.toJsonNode());

        return new NodeAndResponse(origNode, deserialized);
    }

    private class NodeAndResponse {
        private final JsonNode node;
        private final R res;

        private NodeAndResponse(JsonNode node, R res) {
            this.node = node;
            this.res = res;
        }
    }

    public void persist(NodeAndResponse deserialized) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        // Put the deserialized objects into the persistence layer
        Dao<R> dao = Dao.get(responseType);
        R persisted = null;

        try {
            dao.saveOrUpdate(deserialized.res);
            persisted = dao.getById(deserialized.res.getId());

        } catch (Exception e) {
            fail(e);
        }

        assertNotNull(persisted);
        assertEquals(deserialized.res, persisted);
        assertEquals(deserialized.node, persisted.toJsonNode());
    }
}
