package nessusData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;


import nessusData.entity.*;
import nessusData.persistence.*;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import testUtils.Database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.json.JsonException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TestCRUD {
    @Parameterized.Parameters
    public static Collection daoTypes()
            throws NoSuchFieldException, IllegalAccessException, IOException {

        Database.hardReset();
        LookupDao.runningTests();

        return Arrays.asList(new Object[][] { {
            createParamsFromJson(Scan.class, "dbPopulate.sql", "scanCRUD.json")
        }});
    }


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

    @Test
    public void testCreate() {
        if (params.persistLookupLater != null) params.persistLookupLater.create.run();
        if (params.create == null) return;

        for (Pojo create : (List<Pojo>) params.create) {
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
        if (params.persistLookupLater != null) params.persistLookupLater.update.run();
        if (params.update == null) return;

        for (Pojo update : (List<Pojo>) params.update) {
            params.dao.saveOrUpdate(update);

            Pojo persisted = params.dao.getById(update.getId());
            assertEquals(update, persisted);
        }
    }

    @Test
    public void testDelete() {
        if (params.persistLookupLater != null) params.persistLookupLater.delete.run();
        if (params.delete == null) return;

        for (Pojo delete : (List<Pojo>) params.delete) {
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
        if (params.persistLookupLater != null) params.persistLookupLater.read.run();
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
        private final Dao<POJO> dao;
        private final List<POJO> create;
        private final List<POJO> update;
        private final List<POJO> delete;
        private final List<Read<POJO>> read;
        private PersistLookupLater persistLookupLater = null;

        private TestParams(Class<POJO> pojoClass, String sqlPopulate,
                           List<POJO> create,   List<POJO> update,
                           List<POJO> delete,   List<Read<POJO>> read)
                throws NoSuchFieldException, IllegalAccessException {

            this.dao = (Dao<POJO>) pojoClass.getDeclaredField("dao").get(null);

            this.sqlPopulate = sqlPopulate;
            this.create = create;
            this.update = update;
            this.delete = delete;
            this.read = read;
        }
    }

    private static class PersistLookupLater {
        private Runnable create;
        private Runnable update;
        private Runnable delete;
        private Runnable read;

        private PersistLookupLater() { }
    }



    private static class Read<POJO extends Pojo> {
        //SV = search field value type, only needed when applicable

        private enum Type {
            GET_ALL, GET_ONE, GET_SOME
        }

        private final Type readType;

        // For GET_ONE only
        private final POJO expected;

        // For GET_ALL and GET_SOME
        private final int searchId;
        private final Map<String, Object> searchMap;
        private final int expectedCount;
        private final List<POJO> expectedList;
                // Doesn't need to be complete, but anything in this list needs to
                // Objects.equals() an entry in the return

        // GET_ALL assumed, no search params.  NOTE: expected list can be null if not used. If expectedCount < 0 then it is not checked
        private Read(int expectedTotalCount, List<POJO> expectedList) {
            this.readType = Read.Type.GET_ALL;
            this.expectedCount = expectedTotalCount;
            this.expectedList = expectedList;
            this.searchMap = null;
            this.searchId = -1;
            this.expected = null;

        }

        //GET_SOME assumed, with search params.  NOTE: expected list can be null if not used. If expectedCount < 0 then it is not checked
        private Read(Map<String,Object> searchMap, int expectedCount, List<POJO> expectedList) {
            if (searchMap == null) throw new IllegalArgumentException(
                    "Must provide a non-null search map for TestCRUD.Read of type GET_SOME");

            this.readType = Read.Type.GET_SOME;
            this.searchMap = searchMap;
            this.expectedCount = expectedCount;
            this.expectedList =  expectedList;
            this.searchId = -1;
            this.expected = null;
        }

        //GET_ONE assumed, with individual Pojo.
        private Read(int searchId, POJO expected) {
            this.readType = Read.Type.GET_ONE;
            this.searchId = searchId;
            this.expected = expected;
            this.expectedCount = 1;
            this.searchMap = null;
            this.expectedList = null;

        }


        private void assertExpected(Object actual) {
            if (this.readType.equals(Read.Type.GET_ONE)) {
                this.assertOne((POJO) actual);

            } else if (this.readType.equals(Read.Type.GET_SOME) || this.readType.equals(Read.Type.GET_ALL)) {
                this.assertSomeOrAll((List<POJO>) actual);

            } else {
                throw new IllegalStateException("TestCRUD.Read.readType must be one of the TestCRUD.Read.Type enums");
            }
        }

        private void assertOne(POJO actual) {
            if (this.expected != null) {
                assertEquals(this.expected, actual);

            } else {
                assertNull(actual);
            }
        }

        private void assertSomeOrAll(List<POJO> actual) {
            assertNotNull(actual);

            if (this.expectedCount >= 0) {
                assertEquals(this.expectedCount, actual.size());
            }

            if (this.expectedList != null) {
                for (POJO pojo : this.expectedList) {
                    if (!actual.contains(pojo)) {
                        fail(pojo.toString());
                    }
                }
            }
        }
    }

    private static TestParams createParamsFromJson(
            Class<? extends Pojo> pojoClass, String sqlPopulate, String jsonFile)
            throws IOException, NoSuchFieldException, IllegalAccessException {

        BufferedReader reader = new BufferedReader(new FileReader(jsonFile));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(reader, JsonNode.class);

        List<Pojo> create = new ArrayList<Pojo>();
        List<Pojo> update = new ArrayList<Pojo>();
        List<Pojo> delete = new ArrayList<Pojo>();
        List<Read> read = new ArrayList<Read>();

        PersistLookupLater persistLookupLater = new PersistLookupLater();

        for (JsonNode node : rootNode.get("create")) {
            create.add(mapper.convertValue(node, pojoClass));
        }
        persistLookupLater.create = LookupDao.getPersistLater();

        for (JsonNode node : rootNode.get("update")) {
            update.add(mapper.convertValue(node, pojoClass));
        }
        persistLookupLater.update = LookupDao.getPersistLater();

        for (JsonNode node : rootNode.get("delete")) {
            delete.add(mapper.convertValue(node, pojoClass));
        }
        persistLookupLater.delete = LookupDao.getPersistLater();

        for (JsonNode node : rootNode.get("read")) {
            read.add(processReadParamsJson(pojoClass, mapper, node));
        }
        persistLookupLater.read = LookupDao.getPersistLater();

        TestParams params = new TestParams(pojoClass, sqlPopulate, create, update, delete, read);
        params.persistLookupLater = persistLookupLater;
        return params;
    }

    // delegation method for createParamsFromJson
    private static Read processReadParamsJson(
            Class<? extends Pojo> pojoClass,
            ObjectMapper mapper,
            JsonNode node) {

        Pojo expected = null;
        List<Pojo> expectedList = null;
        Integer expectedCount = null;
        Integer searchId = null;
        Map<String, Object> searchMap = null;

        if (node.has("expected")) {
            expected = mapper.convertValue(node.get("expected"), pojoClass);
        }

        if (node.has("expectedList")) {
            expectedList = new ArrayList();
            for (JsonNode subNode : node.get("expectedList")) {
                expectedList.add(mapper.convertValue(subNode, pojoClass));
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
            return new Read(searchId, expected);
        }

        if (expectedCount == null) expectedCount = -1;

        if (searchMap != null) {
            // GET_SOME
            return new Read(searchMap, expectedCount, expectedList);

        } else {
            // GET_ALL
            return new Read(expectedCount, expectedList);
        }

    }
}
