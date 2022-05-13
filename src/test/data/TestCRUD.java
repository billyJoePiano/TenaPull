package data;

import java.io.*;
import java.util.*;

import tenapull.data.entity.scan.*;
import tenapull.data.entity.template.DbPojo;
import tenapull.data.persistence.*;
import tenapull.run.*;
import tenapull.util.*;

import com.fasterxml.jackson.databind.*;

import javax.json.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests Create, Read, Update, Delete operations through the Hibernate ORM,
 * using parameters from provided .json files.
 */
@RunWith(Parameterized.class)
public class TestCRUD {
    /**
     * Name of the SQL script which performs a partial reset between tests, and repopulates the
     * tables which were reset.  It resets the scan and host data tables but not the lookup tables
     */
    public static final String DB_PARTIAL_RESET = "dbPartialReset.sql"; // resets the scan data tables but not the lookup tables
    /**
     * The SQL script which populates the database
     */
    public static final String DB_POPULATE_SQL = "dbPopulate.sql";


    /**
     * The directory where the test params can be found, with .json files matching class names.
     */
    public static final String PARAMS_DIR = "crud-params/";

    /**
     * The array of test parameters for the tests to be run
     */
    public static final Object[][] TESTS = {
            // { pojoType, (optionals) dbPopulate script, jsonFile with params }
            { Folder.class },
            { Scan.class } ,
            { Scan.class, null, "Scan.extraJson.json"} // null = use default

    };

    static {
        Main.loadTestConfig();
    }


    private final TestParams params;

    /**
     * Instantiates a new Test crud with the provided params
     *
     * @param params the params
     */
    public TestCRUD(TestParams params) {
        if (params.dao == null) throw new IllegalArgumentException(
                "Must provide a non-null dao for TestCRUD constructor");

        this.params = params;
    }

    /**
     * Performs a partial reset on the database
     *
     * @throws FileNotFoundException the file not found exception
     */
    @After
    public void dbReset() throws FileNotFoundException {
        Database.runSQL(DB_PARTIAL_RESET);
    }

    private List<DbPojo> convertFromJsonNode(List<JsonNode> nodeList) {
        return convertFromJsonNode(params.pojoType, nodeList);
    }

    private static List<DbPojo> convertFromJsonNode(Class<? extends DbPojo> pojoType,
                                                    List<JsonNode> paramNodes) {
        List<DbPojo> pojoList = new ArrayList();
        ObjectMapper mapper = new ObjectMapper();
        for (JsonNode node : paramNodes) {
            pojoList.add(convertFromJsonNode(pojoType, mapper, node));
        }
        return pojoList;
    }

    private static DbPojo convertFromJsonNode(Class<? extends DbPojo> pojoType,
                                              ObjectMapper mapper,
                                              JsonNode node) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        return mapper.convertValue(node, pojoType);
    }

    /**
     * Test create operations
     */
    @Test
    public void testCreate() {
        if (params.create == null) return;
        List<DbPojo> pojoList = this.convertFromJsonNode(params.create);

        for (DbPojo create : pojoList) {
            int id = params.dao.insert(create);

            if (id < 0) {
                fail("Id returned from dao.insert was less than zero: " + id);
            }

            assertEquals(create.getId(), id);

            DbPojo persisted = params.dao.getById(id);
            assertEquals(create, persisted);
        }
    }

    /**
     * Test update operations
     */
    @Test
    public void testUpdate() {
        if (params.update == null) return;
        List<DbPojo> pojoList = this.convertFromJsonNode(params.update);

        for (DbPojo update : pojoList) {
            params.dao.saveOrUpdate(update);

            DbPojo persisted = params.dao.getById(update.getId());
            assertEquals(update, persisted);
        }
    }

    /**
     * Test delete operations
     */
    @Test
    public void testDelete() {
        if (params.delete == null) return;
        List<DbPojo> pojoList = this.convertFromJsonNode(params.delete);

        for (DbPojo delete : pojoList) {
            int id = delete.getId();

            DbPojo verifyExistence = params.dao.getById(id);
            assertNotNull(verifyExistence);

            params.dao.delete(delete);

            DbPojo persisted = params.dao.getById(id);
            assertNull(persisted);
        }
    }

    /**
     * Test read operations
     */
    @Test
    public void testRead() {
        if (params.read == null) return;

        for (Read read : (List<Read<DbPojo>>) params.read) {
            Object actual;

            switch (read.readType) {
                case GET_ALL:
                    actual = params.dao.getAll();
                    break;

                case GET_SOME:
                    actual = params.dao.findByPropertyEqual(read.searchMap);
                    break;

                case GET_ONE:
                    actual = params.dao.getById(read.searchId);
                    break;

                default:
                    throw new IllegalStateException("Invalid value for TestCRUD.Read.readType");
            }

            read.assertExpected(actual);
        }
    }

    private static class TestParams<POJO extends DbPojo> {
        private final String sqlPopulate;
        private final Class<POJO> pojoType;
        private final Dao<POJO> dao;
        private final List<POJO> create;
        private final List<POJO> update;
        private final List<POJO> delete;
        private final List<Read<POJO>> read;

        private TestParams(Class<POJO> pojoType, String sqlPopulate,
                           List<POJO> create, List<POJO> update,
                           List<POJO> delete, List<Read<POJO>> read)
                throws NoSuchFieldException, IllegalAccessException {

            this.pojoType = pojoType;
            this.dao = (Dao<POJO>) pojoType.getDeclaredField("dao").get(null);

            this.sqlPopulate = sqlPopulate;
            this.create = create;
            this.update = update;
            this.delete = delete;
            this.read = read;
        }
    }


    private static class Read<POJO extends DbPojo> {
        private enum Type {
            /**
             * Get all type.
             */
            GET_ALL,
            /**
             * Get one type.
             */
            GET_ONE,
            /**
             * Get some type.
             */
            GET_SOME
        }

        private final Type readType;
        private final Class<POJO> pojoType;

        // For GET_ONE only
        private final JsonNode expected;
        private POJO expectedPojo;
        private final int searchId;

        // For GET_ALL and GET_SOME
        private final Map<String, Object> searchMap;
        private final int expectedCount;

        private final List<JsonNode> expectedList;
                // Doesn't need to be complete, but anything in this list needs to
                // Objects.equals() an entry in the return, after being converted to POJO

        // GET_ALL assumed, no search params.  NOTE: expected list can be null if not used. If expectedCount < 0 then it is not checked
        private Read(Class<POJO> pojoType, int expectedTotalCount, List<JsonNode> expectedList) {
            this.pojoType = pojoType;
            this.readType = Read.Type.GET_ALL;
            this.expectedCount = expectedTotalCount;
            this.expectedList = expectedList;
            this.searchMap = null;
            this.searchId = -1;
            this.expected = null;

        }

        //GET_SOME assumed, with search params.  NOTE: expected list can be null if not used. If expectedCount < 0 then it is not checked
        private Read(Class<POJO> pojoType, Map<String,Object> searchMap,
                     int expectedCount, List<JsonNode> expectedList) {

            if (searchMap == null) throw new IllegalArgumentException(
                    "Must provide a non-null search map for TestCRUD.Read of type GET_SOME");

            this.pojoType = pojoType;
            this.readType = Read.Type.GET_SOME;
            this.searchMap = searchMap;
            this.expectedCount = expectedCount;
            this.expectedList =  expectedList;
            this.searchId = -1;
            this.expected = null;
        }

        //GET_ONE assumed, with individual DbPojo.
        private Read(Class<POJO> pojoType, int searchId, JsonNode expected) {
            this.pojoType = pojoType;
            this.readType = Read.Type.GET_ONE;
            this.searchId = searchId;
            this.expected = expected;
            this.expectedCount = 1;
            this.searchMap = null;
            this.expectedList = null;

        }

        private void assertExpected(Object actual) {
            switch (this.readType) {
                case GET_ONE:
                    this.assertOne((POJO) actual);
                    break;

                case GET_SOME: case GET_ALL:
                    this.assertSomeOrAll((List<POJO>) actual);
                    break;

                default:
                    throw new IllegalStateException(
                            "TestCRUD.Read.readType must be one of the TestCRUD.Read.Type enums");
            }
        }

        private void assertOne(POJO actual) {
            POJO expected = (POJO) convertFromJsonNode(this.pojoType, null, this.expected);
            if (expected != null) {
                assertEquals(expected, actual);

            } else {
                assertNull(actual);
            }
        }

        private void assertSomeOrAll(List<POJO> actual) {
            assertNotNull(actual);

            if (this.expectedCount >= 0) {
                assertEquals(this.expectedCount, actual.size());
            }

            if (expectedList != null) {
                List<POJO> expectedList =
                        (List<POJO>) convertFromJsonNode(this.pojoType, this.expectedList);

                for (POJO pojo : expectedList) {
                    if (!actual.contains(pojo)) {
                        fail(pojo.toString());
                    }
                }
            }
        }
    }

    /**
     * For JUnit to obtain the test parameters which will be passed into each
     * instance of TestCRUD
     *
     * @return the test params
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws IOException            the io exception
     */
    @Parameterized.Parameters
    public static Collection getTestParams()
            throws NoSuchFieldException, IllegalAccessException, IOException {

        Database.hardReset();
        Database.runSQL(DB_POPULATE_SQL);

        List<TestParams[]> paramsList = new ArrayList();

        for (Object[] test : TESTS) {
            Class<? extends DbPojo> pojoType = null;
            String dbPopulate = null;
            String jsonFile = null;

            if (test.length > 0) {
                pojoType = (Class<? extends DbPojo>) test[0];

            } else {
                throw new IllegalStateException("Empty test parameters");
            }

            if (test.length > 1) {
                dbPopulate = DB_POPULATE_SQL;
            }

            if (test.length > 2) {
                jsonFile = (String) test[2];
            }

            if (dbPopulate == null) {
                dbPopulate = DB_POPULATE_SQL;
            }

            if (jsonFile == null) {
                jsonFile = PARAMS_DIR + pojoType.getSimpleName() + ".json";

            } else if (!jsonFile.substring(0, 1).equals("/")) {
                jsonFile = PARAMS_DIR + jsonFile;
            }

            for (TestParams params :
                    createParamsFromJson(pojoType, dbPopulate, jsonFile)) {
                paramsList.add(new TestParams[] { params });
            }
        }

        return paramsList;
    }

    private static List<TestParams> createParamsFromJson(Class<? extends DbPojo> pojoType,
                                                         String sqlPopulate,
                                                         String jsonFile)
            throws IOException, NoSuchFieldException, IllegalAccessException {

        BufferedReader reader = new BufferedReader(new FileReader(jsonFile));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(reader, JsonNode.class);

        List<TestParams> list = new ArrayList();

        if (rootNode.isArray()) {
            for (JsonNode node: rootNode) {
                if (!node.isObject()) {
                    throw new JsonException("Invalid json node in file '"
                            + jsonFile + "'.  Must be an object:\n"
                            + node.toString());
                }
                list.add(createParams(pojoType, sqlPopulate, node));
            }

        } else if (rootNode.isObject()) {
            list.add(createParams(pojoType, sqlPopulate, rootNode));

        } else {
            throw new JsonException("Invalid json root node in file '"
                    + jsonFile + "'.  Must be an array or object:\n"
                    + rootNode.toString());
        }

        return list;
    }

    private static TestParams createParams(Class<? extends DbPojo> pojoType,
                                           String sqlPopulate,
                                           JsonNode rootNode)
            throws NoSuchFieldException, IllegalAccessException {

        List<JsonNode> create = new ArrayList();
        List<JsonNode> update = new ArrayList();
        List<JsonNode> delete = new ArrayList();
        List<Read> read = new ArrayList();

        JsonNode n;
        n = rootNode.get("create");

        if (n != null) {
            for (JsonNode node : n) {
                create.add(node);
            }
        }

        n = rootNode.get("update");

        if (n != null) {
            for (JsonNode node : n) {
                update.add(node);
            }
        }

        n = rootNode.get("delete");
        if (n != null) {
            for (JsonNode node : n) {
                delete.add(node);
            }
        }

        n = rootNode.get("read");
        if (n != null) {
            for (JsonNode node : n) {
                read.add(createReadParams(pojoType, node));
            }
        }

        return new TestParams(pojoType, sqlPopulate, create, update, delete, read);
    }

    // delegation method for createParamsFromJson
    private static Read createReadParams(
            Class<? extends DbPojo> pojoType,
            JsonNode node) {

        JsonNode expected = null;
        List<JsonNode> expectedList = null;
        Integer expectedCount = null;
        Integer searchId = null;
        Map<String, Object> searchMap = null;

        if (node.has("expected")) {
            expected = node.get("expected");
        }

        if (node.has("expectedList")) {
            expectedList = new ArrayList();
            for (JsonNode subNode : node.get("expectedList")) {
                expectedList.add(subNode);
            }
        }

        if (node.has("expectedCount")) {
            expectedCount = node.get("expectedCount").asInt();
            if (!expectedCount.toString().equals(
                    node.get("expectedCount").toString())) {

                throw new JsonException("Invalid non-integer value for 'expectedCount' :\n"
                        + node.toString());
            }
        }

        if (node.has("searchId")) {
            searchId = node.get("searchId").asInt();
            if (!searchId.toString().equals(
                    node.get("searchId").toString())) {

                throw new JsonException("Invalid non-integer value for 'searchId' :\n"
                        + node.toString());
            }
        }

        if (node.has("searchMap")) {
            JsonNode jsonSearchMap = node.get("searchMap");
            if (!jsonSearchMap.isObject()) {
                throw new JsonException("Search map must be a JSON *object*:\n"
                        + node.toString());
            }

            searchMap = MapLookupDao.makeSearchMapFromJson(jsonSearchMap);
        }

        if (searchId != null) {
            // GET_ONE
            if (expectedList != null || expectedCount != null || searchMap != null) {
                throw new JsonException(
                        "Invalid properties combination for TestParams\n" + node);
            }
            return new Read(pojoType, searchId, expected);
        }

        if (expectedCount == null) expectedCount = -1;

        if (searchMap != null) {
            // GET_SOME
            return new Read(pojoType, searchMap, expectedCount, expectedList);

        } else {
            // GET_ALL
            return new Read(pojoType, expectedCount, expectedList);
        }

    }
}
