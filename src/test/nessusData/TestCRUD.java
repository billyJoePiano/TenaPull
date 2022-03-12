package nessusData;

import java.io.*;
import java.util.*;

import nessusData.entity.*;
import nessusData.persistence.*;
import testUtils.Database;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import javax.json.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(Parameterized.class)
public class TestCRUD {
    // default .sql script to populate the db between tests
    public static final String DB_POPULATE_SQL = "dbPopulate.sql";

    // directory with .json files matching class names, with test params
    public static final String JSON_DIR = "crud-params/";
    // ...used when no file is provided (JSON_DIR/pojoClassName.json)
    // OR when file is a relative path i.e. doesn't start with "/"

    public static final Object[][] TESTS = {
            // { pojoClass, (optionals) dbPopulate script, jsonFile with params] }
            { Scan.class } ,
            { Folder.class },
            { Scan.class, null, "Scan.2.json"} // null = use default

    };


    private final TestParams params;

    public TestCRUD(TestParams params) {
        if (params.dao == null) throw new IllegalArgumentException(
                "Must provide a non-null dao for TestCRUD constructor");

        this.params = params;
    }

    @Before
    public void dbReset() {
        Database.reset();
        if (params.sqlPopulate != null) {
            Database.runSQL(params.sqlPopulate);
        }
    }

    private List<Pojo> convertFromJsonNode(List<JsonNode> nodeList) {
        return convertFromJsonNode(params.pojoClass, nodeList);
    }

    private static List<Pojo> convertFromJsonNode(Class<? extends Pojo> pojoClass,
                                                  List<JsonNode> paramNodes) {
        List<Pojo> pojoList = new ArrayList();
        ObjectMapper mapper = new ObjectMapper();
        for (JsonNode node : paramNodes) {
            pojoList.add(convertFromJsonNode(pojoClass, mapper, node));
        }
        return pojoList;
    }

    private static Pojo convertFromJsonNode(Class<? extends Pojo> pojoClass,
                                            ObjectMapper mapper,
                                            JsonNode node) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        return mapper.convertValue(node, pojoClass);
    }

    @Test
    public void testCreate() {
        if (params.create == null) return;
        List<Pojo> pojoList = this.convertFromJsonNode(params.create);

        for (Pojo create : pojoList) {
            int id = params.dao.insert(create);

            if (id < 0) {
                fail("Id returned from dao.insert was less than zero: " + id);
            }

            assertEquals(create.getId(), id);

            Pojo persisted = params.dao.getById(id);
            assertEquals(create, persisted);
        }
    }

    @Test
    public void testUpdate() {
        if (params.update == null) return;
        List<Pojo> pojoList = this.convertFromJsonNode(params.update);

        for (Pojo update : pojoList) {
            params.dao.saveOrUpdate(update);

            Pojo persisted = params.dao.getById(update.getId());
            assertEquals(update, persisted);
        }
    }

    @Test
    public void testDelete() {
        if (params.delete == null) return;
        List<Pojo> pojoList = this.convertFromJsonNode(params.delete);

        for (Pojo delete : pojoList) {
            int id = delete.getId();

            Pojo verifyExistence = params.dao.getById(id);
            assertNotNull(verifyExistence);

            params.dao.delete(delete);

            Pojo persisted = params.dao.getById(id);
            assertNull(persisted);
        }
    }

    @Test
    public void testRead() {
        if (params.read == null) return;

        for (Read read : (List<Read<Pojo>>) params.read) {
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

    private static class TestParams<POJO extends Pojo> {
        private final String sqlPopulate;
        private final Class<POJO> pojoClass;
        private final Dao<POJO> dao;
        private final List<POJO> create;
        private final List<POJO> update;
        private final List<POJO> delete;
        private final List<Read<POJO>> read;

        private TestParams(Class<POJO> pojoClass, String sqlPopulate,
                           List<POJO> create,   List<POJO> update,
                           List<POJO> delete,   List<Read<POJO>> read)
                throws NoSuchFieldException, IllegalAccessException {

            this.pojoClass = pojoClass;
            this.dao = (Dao<POJO>) pojoClass.getDeclaredField("dao").get(null);

            this.sqlPopulate = sqlPopulate;
            this.create = create;
            this.update = update;
            this.delete = delete;
            this.read = read;
        }
    }


    private static class Read<POJO extends Pojo> {
        private enum Type {
            GET_ALL, GET_ONE, GET_SOME
        }

        private final Type readType;
        private final Class<POJO> pojoClass;

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
        private Read(Class<POJO> pojoClass, int expectedTotalCount, List<JsonNode> expectedList) {
            this.pojoClass = pojoClass;
            this.readType = Read.Type.GET_ALL;
            this.expectedCount = expectedTotalCount;
            this.expectedList = expectedList;
            this.searchMap = null;
            this.searchId = -1;
            this.expected = null;

        }

        //GET_SOME assumed, with search params.  NOTE: expected list can be null if not used. If expectedCount < 0 then it is not checked
        private Read(Class<POJO> pojoClass, Map<String,Object> searchMap,
                     int expectedCount, List<JsonNode> expectedList) {

            if (searchMap == null) throw new IllegalArgumentException(
                    "Must provide a non-null search map for TestCRUD.Read of type GET_SOME");

            this.pojoClass = pojoClass;
            this.readType = Read.Type.GET_SOME;
            this.searchMap = searchMap;
            this.expectedCount = expectedCount;
            this.expectedList =  expectedList;
            this.searchId = -1;
            this.expected = null;
        }

        //GET_ONE assumed, with individual Pojo.
        private Read(Class<POJO> pojoClass, int searchId, JsonNode expected) {
            this.pojoClass = pojoClass;
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
            POJO expected = (POJO) convertFromJsonNode(this.pojoClass, null, this.expected);
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
                        (List<POJO>) convertFromJsonNode(this.pojoClass, this.expectedList);

                for (POJO pojo : expectedList) {
                    if (!actual.contains(pojo)) {
                        fail(pojo.toString());
                    }
                }
            }
        }
    }

    @Parameterized.Parameters
    public static Collection getTestParams()
            throws NoSuchFieldException, IllegalAccessException, IOException {

        List<TestParams[]> paramsList = new ArrayList();

        for (Object[] test : TESTS) {
            Class<? extends Pojo> pojoClass = null;
            String dbPopulate = null;
            String jsonFile = null;

            if (test.length > 0) {
                pojoClass = (Class<? extends Pojo>) test[0];

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
                jsonFile = JSON_DIR + pojoClass.getSimpleName() + ".json";

            } else if (!jsonFile.substring(0, 1).equals("/")) {
                jsonFile = JSON_DIR + jsonFile;
            }

            for (TestParams params :
                    createParamsFromJson(pojoClass, dbPopulate, jsonFile)) {
                paramsList.add(new TestParams[] { params });
            }
        }

        Database.hardReset();

        return paramsList;
    }

    private static List<TestParams> createParamsFromJson(Class<? extends Pojo> pojoClass,
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
                list.add(createParams(pojoClass, sqlPopulate, node));
            }

        } else if (rootNode.isObject()) {
            list.add(createParams(pojoClass, sqlPopulate, rootNode));

        } else {
            throw new JsonException("Invalid json root node in file '"
                    + jsonFile + "'.  Must be an array or object:\n"
                    + rootNode.toString());
        }

        return list;
    }

    private static TestParams createParams(Class<? extends Pojo> pojoClass,
                                           String sqlPopulate,
                                           JsonNode rootNode)
            throws NoSuchFieldException, IllegalAccessException {

        List<JsonNode> create = new ArrayList();
        List<JsonNode> update = new ArrayList();
        List<JsonNode> delete = new ArrayList();
        List<Read> read = new ArrayList();

        for (JsonNode node : rootNode.get("create")) {
            create.add(node);
        }

        for (JsonNode node : rootNode.get("update")) {
            update.add(node);
        }

        for (JsonNode node : rootNode.get("delete")) {
            delete.add(node);
        }

        for (JsonNode node : rootNode.get("read")) {
            read.add(createReadParams(pojoClass, node));
        }

        TestParams params = new TestParams(pojoClass, sqlPopulate, create, update, delete, read);
        return params;
    }

    // delegation method for createParamsFromJson
    private static Read createReadParams(
            Class<? extends Pojo> pojoClass,
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
            searchMap = new HashMap();

            Iterator<Map.Entry<String, JsonNode>> iterator = node.get("searchMap").fields();
            // I guess you can't do a for loop over an iterator, only an iterable!!
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                String key = entry.getKey();
                JsonNode nodeVal = entry.getValue();

                if (!nodeVal.isValueNode()) {
                    throw new JsonException("Invalid non-primitive value in searchMap."
                            + key + "\n" + node.toString());
                }

                Object value;

                switch (nodeVal.getNodeType()) {
                    case NUMBER:
                        if (nodeVal.isFloatingPointNumber()) {
                            value = nodeVal.doubleValue();

                        } else if (nodeVal.isInt()) {
                            value = nodeVal.intValue();

                        } else if (nodeVal.isLong()) {
                            value = nodeVal.longValue();

                        } else {
                            throw new JsonException("Could not determine numeric type conversion for searchMap."
                                    + key + "\n" + node.toString());
                        }
                        break;

                    case BOOLEAN:
                        value = nodeVal.booleanValue();
                        break;

                    case STRING:
                        value = nodeVal.textValue();
                        break;

                    case NULL:
                        value = null;
                        break;

                    default:
                        throw new JsonException("Could not determine value type for JsonNode in searchMap"
                                + key + "\n" + node.toString());

                }

                searchMap.put(key, value);

            }
        }

        if (searchId != null) {
            // GET_ONE
            if (expectedList != null || expectedCount != null || searchMap != null) {
                throw new JsonException(
                        "Invalid properties combination for TestParams\n" + node);
            }
            return new Read(pojoClass, searchId, expected);
        }

        if (expectedCount == null) expectedCount = -1;

        if (searchMap != null) {
            // GET_SOME
            return new Read(pojoClass, searchMap, expectedCount, expectedList);

        } else {
            // GET_ALL
            return new Read(pojoClass, expectedCount, expectedList);
        }

    }
}
