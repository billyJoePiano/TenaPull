package data;

import nessusTools.client.response.*;
import nessusTools.data.entity.*;
import nessusTools.data.entity.template.DbPojo;
import nessusTools.data.persistence.Dao;
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
public class TestDeserializationPersistence {
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

        return Arrays.asList(new Object[][] { {

            new TestParams(IndexResponse.class, "indexResponse.json",
                List.of(
                    new ExpectedData<Folder>("folders", Folder.class, 2),
                    new ExpectedData<Scan>("scans", Scan.class, 5)
                )
            )

        }, {

            new TestParams(ScanInfoResponse.class, "ScanInfo.json",
                List.of(
                    new ExpectedData<ScanInfo>("info", ScanInfo.class, null)
                )
            )
        }

        });
    }

    /*******************************
     * Instance variables/methods
     *******************************/

    private final Logger logger;
    private final TestParams params;

    private String origJson = "";
    private JsonNode origNode = null;
    private NessusResponse deserialized = null;
    private List<NessusResponse.PojoData> actualData = null;
    private NessusResponse persistedContainer = null;

    public TestDeserializationPersistence(TestParams params) {
        this.logger = LogManager.getLogger(params.responseClass);
        this.params = params;

        if (params.lastInstance != null && params.lastInstance != this) {
            // Unfortunately, these variables have to be passed between instances because
            // JUnit insists on creating a new instance for each method with a @Test annotation,
            // but the methods rely upon an order of execution and the results of the
            // previous test.

            this.origJson = params.lastInstance.origJson;
            this.origNode = params.lastInstance.origNode;
            this.deserialized = params.lastInstance.deserialized;
            this.actualData = params.lastInstance.actualData;
            this.persistedContainer = params.lastInstance.persistedContainer;
        }

        params.lastInstance = this;

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
    public void _1_getDummyJson() {
        // extract the dummy json out of the test file
        try (BufferedReader reader = new BufferedReader(new FileReader(params.filename))) {
            String line;

            while ((line = reader.readLine()) != null) {
                origJson += line;
            }

        } catch (IOException e) {
            fail(e);
            return;
        }

        assertNotNull(origJson);
        assertNotEquals("", origJson);
    }

    @Test
    public void _2_testDeserialize() {
        // To standardize the json string's format, first put into a JsonNode
        // then re-stringify to remove "prettifying" whitespace.
        // With the standardized/minified json, serialize it into the appropriate
        // DbPojo objects

        try {
            ObjectMapper mapper = new CustomObjectMapper();

            // https://stackoverflow.com/questions/12173416/how-do-i-get-the-compact-form-of-pretty-printed-json-code
            origNode = mapper.readValue(origJson, JsonNode.class);
            origJson = origNode.toString(); //removes the 'pretty printed' white space'

            deserialized = mapper.readValue(origJson, params.responseClass);

        } catch (JsonProcessingException e) {
            fail(e);
            return;
        }

        assertNotNull(deserialized);

        Iterator<ExpectedData> expectedIterator = params.expectedData.iterator();
        actualData = deserialized.getData();

        assertEquals(params.expectedData.size(), actualData.size());

        for (NessusResponse.PojoData actual : actualData) {
            assertTrue(expectedIterator.hasNext());
            ExpectedData expected = expectedIterator.next();
            assertEquals(expected, actual);
        }

        assertFalse(expectedIterator.hasNext());
    }

    @Test
    public void _3_testPersistence()
        throws NoSuchFieldException,
            IllegalAccessException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException {

        // Put the deserialized objects into the persistence layer

        for (NessusResponse.PojoData actual : actualData) {
            Dao dao = (Dao) actual.getPojoClass().getDeclaredField("dao").get(null);
            if (actual.isIndividual()) {
                dao.insert(actual.getIndividualPojo());
                continue;
            }

            for (DbPojo pojo : (List<DbPojo>) actual.getPojoList()) {
                dao.insert(pojo);
            }
        }

        // Fetch back out of the persistence layer.
        // First create a NessusResponse container to hold the fetched data ...
        persistedContainer = params.response.getClass().getDeclaredConstructor().newInstance();

        // Set the timestamp and id the same.  Normally this would be handled by the NessusClient
        persistedContainer.setId(deserialized.getId());
        persistedContainer.setTimestamp(deserialized.getTimestamp());

        for (NessusResponse.PojoData actual : actualData) {
            Class<? extends DbPojo> pojoClass = actual.getPojoClass();
            Dao dao = (Dao) pojoClass.getDeclaredField("dao").get(null);

            if (actual.isIndividual()) {
                DbPojo pojo = dao.getById(actual.getIndividualPojo().getId());

                persistedContainer.setData(new NessusResponse.PojoData(
                        actual.getFieldName(),
                        pojo != null ? pojo.getClass() : pojoClass,
                        pojo
                    )   );
                continue;
            }

            // ... else, if actual.isList() ....

            pojoClass = null;
            List<DbPojo> list = new ArrayList();

            // Sort the arrays to have the same order as the original input

            for (DbPojo orig : (List<DbPojo>) actual.getPojoList()) {
                DbPojo pojo = dao.getById(orig.getId());
                if (pojoClass == null && pojo != null) {
                    pojoClass = pojo.getClass();
                }
                list.add(pojo);
            }

            if (pojoClass == null) {
                // prefer to take from the first non-null instance in the list...
                // but failing that, take from the original
                pojoClass = actual.getPojoClass();
            }

            persistedContainer.setData(new NessusResponse.PojoData(
                    actual.getFieldName(),
                    pojoClass,
                    list
                ));
        }



        // Now re-serialize then deserialize into a JsonNode, and compare against the original
        // NOTE: JsonNode equality does not care about the order of properties.  They will almost
        // certainly appear in a different order than the original

        // https://stackoverflow.com/questions/2253750/testing-two-json-objects-for-equality-ignoring-child-order-in-java
        try {
            ObjectMapper mapper = new CustomObjectMapper();
            String reserialized = mapper.writeValueAsString(persistedContainer);
            JsonNode reserializedNode = mapper.readValue(reserialized, JsonNode.class);
            assertEquals(origNode, reserializedNode);

        } catch (JsonProcessingException e) {
            fail(e);
        }
    }





    /**************************************
     *  Test Params inner class
     **************************************/

    static class TestParams {
        private final String filename;
        private final NessusResponse response;
        private final List<ExpectedData> expectedData;
        private final Class<? extends NessusResponse> responseClass;

        private TestDeserializationPersistence lastInstance;

        private TestParams(
                Class<? extends NessusResponse> responseClass,
                String filename,
                List<ExpectedData> expectedData)
            throws NoSuchMethodException, InvocationTargetException,
                InstantiationException, IllegalAccessException {

            this.responseClass = responseClass;

            if (filename.substring(0, 1).equals("/")) {
                this.filename = filename;

            } else {
                this.filename = PARAMS_DIR + filename;
            }

            this.expectedData = expectedData;

            this.response = responseClass.getDeclaredConstructor().newInstance();
        }
    }


    /**************************************
     *  Expected Data inner class
     * @param <POJO> pojo type for the expected data
     **************************************/


    static class ExpectedData<POJO extends DbPojo> {
        private final String fieldname;
        private final Class<POJO> pojoClass;
        private final Integer listCount; //null indicates individual PojoData, not list PojoData

        private ExpectedData(
                String fieldname,
                Class<POJO> pojoClass,
                Integer listCount
        )  {

            this.fieldname = fieldname;
            this.pojoClass = pojoClass;
            this.listCount = listCount;
        }

        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) return true;

            if (o instanceof NessusResponse.PojoData) {
                NessusResponse.PojoData data = (NessusResponse.PojoData) o;

                if (!this.fieldname.equals(data.getFieldName())) return false;
                if (!this.pojoClass.equals(data.getPojoClass())) return false;

                if (this.listCount == null) {
                    return !data.isList();

                } else {
                    List list = data.getPojoList();
                    if (this.listCount > 0) {
                        return list != null && list.size() == this.listCount.intValue();

                    } else {
                        return list == null || list.size() == 0;
                    }
                }


            } else if (o.getClass() == this.getClass()) {
                ExpectedData other = (ExpectedData) o;
                return this.fieldname.equals(other.fieldname)
                        && this.pojoClass.equals(other.pojoClass)
                        && (Objects.equals(this.listCount, other.listCount));

            }
            return false;
        }
    }
}
