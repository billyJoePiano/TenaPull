package data;


import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.persistence.*;
import nessusTools.run.Main;
import nessusTools.util.*;
import org.junit.*;

import org.junit.jupiter.api.BeforeAll;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

/**
 * Tests the pipeline from deserialization into persistence, and then fetching back out of the
 * database, to ensure the re-constituted entity matches the originally deserialized entity
 * <br/>
 * https://www.guru99.com/junit-parameterized-test.html#:~:text=What%20is%20Parameterized%20Test%20in,their%20inputs%20and%20expected%20results.
 *
 * @param <R> the response type being tested
 */
@RunWith(Parameterized.class)
public class TestDeserializationPersistence<R extends NessusResponse> {
    /**
     * The directory which contains the test parameters
     */
    public static final String PARAMS_DIR = "deserializationPersistence-params/";
    /**
     * The constant BILLION, used to convert from nanoseconds into seconds for benchmarking
     */
    public static final double BILLION = 1000000000;

    private static final Logger staticLogger =
            LogManager.getLogger(TestDeserializationPersistence.class);


    static {
        Main.loadTestConfig();
    }

    /******************************
     * Parameters for each test
     *
     * Different Response types and Expected Data responses will go into these parameters
     *
     ******************************/

    private static Map<Integer, IndexResponse> indexResponses = new LinkedHashMap<>();
    private static Map<Integer, ScanResponse> scanResponses = new LinkedHashMap<>();
    private static Map<Integer, ScanHostResponse> hostResponses = new LinkedHashMap<>();

    private static Map<Class<? extends NessusResponse>, Map>
            responses = Map.of(
                            IndexResponse.class, indexResponses,
                            ScanResponse.class, scanResponses,
                            ScanHostResponse.class, hostResponses
                        );

    /**
     * Obtains the test parameters from the params directory, and produces
     * the appropriate inputs for each test
     *
     * @return the collection
     * @throws FileNotFoundException the file not found exception
     */
    @Parameterized.Parameters
    public static Collection testParams() throws FileNotFoundException {
        Map<Integer, Set<Integer>> map = parseFilenames();
        List<Object[]> params = makeParams(map);

        if (params.size() > 0) {
            Database.hardReset();

        } else {
            staticLogger.warn("Nothing to test!  Please provide json files to test with!");
        }

        StackTracePrinter.startThread(360000);
        StackTracePrinter.startThread(600000);

        return params;
    }

    private static Map<Integer, Set<Integer>> parseFilenames() {
        File[] files = new File(PARAMS_DIR).listFiles();
        Map<Integer, Set<Integer>> map = new TreeMap<>();

        for (File file : files) {
            String name = file.getName();
            if (name == null || name.length() <= 11
                    || !(".json".equals(name.substring(name.length() - 5)))) {

                if ("index.json".equals(name)) {
                    map.put(-1, null);

                } else {
                    staticLogger.warn("Invalid filename for test params: " + name);
                }

                continue;
            }

            if ("scan.".equals(name.substring(0, 5))) {
                String idStr = name.substring(5, name.length() - 5);
                int id;
                try {
                    id = Integer.parseInt(idStr);

                } catch (NumberFormatException e) {
                    staticLogger.warn("Invalid filename for test params: " + name, e);
                    continue;
                }

                if (!map.containsKey(id)) {
                    map.put(id, new TreeSet<>());
                }
                continue;

            } else if (!("host.".equals(name.substring(0, 5)))) {
                staticLogger.warn("Invalid filename for test params: " + name);
                continue;
            }

            String[] idStr = name.split("\\.");

            if (idStr.length != 4) {
                staticLogger.warn("Invalid filename for test params: " + name);
                continue;
            }

            int scanId;
            int hostId;
            try {
                scanId = Integer.parseInt(idStr[1]);
                hostId = Integer.parseInt(idStr[2]);

            } catch (NumberFormatException e) {
                staticLogger.warn("Invalid filename for test params: " + name, e);
                continue;
            }

            Set<Integer> hosts = map.get(scanId);
            if (hosts == null) {
                hosts = new TreeSet<>();
                map.put(scanId, hosts);
            }
            hosts.add(hostId);
        }
        return map;
    }

    private static List<Object[]> makeParams(Map<Integer, Set<Integer>> map) {
        List<Object[]> params = new LinkedList();

        if (map.containsKey(-1)) {
            map.remove(-1);
            params.add(new Object[]{
                    "index.json",
                    IndexResponse.class,
                    null
            });
        }



        for (Map.Entry<Integer, Set<Integer>> entry : map.entrySet()) {
            int scanId = entry.getKey();

            params.add(new Object[]{
                    "scan." + scanId + ".json",
                    ScanResponse.class,
                    new Lambda1<ObjectNode, Integer>() {
                        public Integer call(ObjectNode node) {
                            node.remove("filters");
                            ScanResponse scan = new ScanResponse();
                            scan.setId(scanId);
                            ScanResponse.dao.insert(scan);
                            return scanId;
                        }
                    }
            });

            for (int hostId : entry.getValue()) {
                params.add(new Object[]{
                        "host." + scanId + "." + hostId + ".json",
                        ScanHostResponse.class,
                        new Lambda1<ObjectNode, Integer>() {
                            public Integer call(ObjectNode node) {
                                ScanHost host = new ScanHost();
                                host.setHostId(hostId);
                                ScanResponse response = scanResponses.get(scanId);
                                if (response == null) {

                                    fail("Unable to find scan response matching id: " + scanId);
                                }
                                host.setResponse(response);

                                ScanHost actual = ScanHost.dao.getOrCreate(host);
                                ScanHostResponse hostRes = new ScanHostResponse();

                                hostRes.setId(actual.getId());
                                ScanHostResponse.dao.insert(hostRes);
                                return actual.getId();
                            }
                        }
                });
            }
        }

        return params;
    }

    /*******************************
     * Instance variables/methods
     *******************************/

    /**
     * The logger for the response type being tested
     */
    private final Logger logger;

    private final Class<R> responseType;
    private final String jsonFile;
    private String origJson = "";
    private final Lambda1<ObjectNode, Integer> initialize;
    private Integer idToSet;
    private ObjectNode node;
    private R response;

    /**
     * Instantiates a new Test deserialization persistence.
     *
     * @param jsonFile     the json file
     * @param responseType the response type
     * @param initialize   the initialize
     */
    public TestDeserializationPersistence(String jsonFile,
                                          Class<R> responseType,
                                          Lambda1<ObjectNode, Integer> initialize) {
        this.logger = LogManager.getLogger(responseType);
        this.responseType = responseType;
        this.jsonFile = PARAMS_DIR + jsonFile;
        this.initialize = initialize;
    }

    /**
     * Db reset performed before any of the tests run
     *
     * @throws FileNotFoundException if the db reset SQL script cannot be found
     */
    @BeforeAll
    public static void dbReset() throws FileNotFoundException {
        Database.reset();
    }


    /**
     * Checks a json "response" (provided from the params file) from the 'index' level.
     *
     * @throws InvocationTargetException if there are problem instantiating the response
     * @throws NoSuchMethodException     if there are problem instantiating the response
     * @throws InstantiationException    if there are problem instantiating the response
     * @throws IllegalAccessException    if there are problem instantiating the response
     */
    @Test
    public void run()
            throws InvocationTargetException, NoSuchMethodException,
                    InstantiationException, IllegalAccessException {

        staticLogger.info("Fetching JSON: " + this.jsonFile);
        long start = System.nanoTime();
        this.fetchJson();
        long time = System.nanoTime() - start;

        staticLogger.info("Fetch took " + (time / BILLION) + ".  Starting deserialization.");

        long sub = System.nanoTime();
        this.deserialize();
        time = System.nanoTime() - sub;

        staticLogger.info("Deserialization took " + (time / BILLION) + ".  Starting persistence.");

        sub = System.nanoTime();
        this.persist();
        time = System.nanoTime();

        staticLogger.info("Persistence took " + ((time - sub) / BILLION)
                + "\nTotal time: " + ((time - start) / BILLION));
    }

    /**
     * Fetch json from the file
     */
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

    /**
     * Deserialize the response obtained from the file.
     *
     * @throws InvocationTargetException if there are problem instantiating the response
     * @throws NoSuchMethodException     if there are problem instantiating the response
     * @throws InstantiationException    if there are problem instantiating the response
     * @throws IllegalAccessException    if there are problem instantiating the response
     */
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

    /**
     * Persist the deserialized response
     */
    public void persist() {

        // Put the deserialized objects into the persistence layer
        Dao<R> dao = Dao.get(responseType);
        R persisted = null;

        try {
            dao.saveOrUpdate(this.response);
            responses.get(this.responseType).put(response.getId(), this.response);
            persisted = dao.getById(this.response.getId());

        } catch (Exception e) {
            fail(e);
        }

        assertNotNull(persisted);
        assertEquals(response, persisted);
        assertEquals(node, persisted.toJsonNode());
    }

    /*
    @AfterClass
    public static void printHashAvgs() {
        Hash.printAverages();
    }
     */
}
