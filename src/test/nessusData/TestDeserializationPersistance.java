package nessusData;

import nessusData.entity.*;
import nessusData.persistence.Dao;
import nessusData.serialize.*;
import test.testUtils.Database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


import org.junit.Test;
import org.junit.FixMethodOrder;
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
public class TestDeserializationPersistance {



    /******************************
     * Parameters for each test
     *
     * Different Response types and Expected Data responses will go into these parameters
     *
     ******************************/

    @Parameterized.Parameters
    public static Collection responseTypes() {
        Database.hardReset();

        return Arrays.asList(new Object[][] { {
            new TestParams(
                "indexResponse.json",
                new IndexResponse(),
                List.of(
                        new ExpectedData<Folder>("folders", Folder.class, 2),
                        new ExpectedData<Scan>("scans", Scan.class, 5)
                )
            )
        }});
    }

    /*******************************
     * Instance variables/methods
     *******************************/

    private final Logger logger;
    private final TestParams params;

    private String origJson = "";
    private JsonNode origNode = null;
    private Response deserialized = null;
    private List<Response.PojoData> actualData = null;
    private Response persisted = null;

    public TestDeserializationPersistance(TestParams params) {
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
            this.persisted = params.lastInstance.persisted;
        }

        params.lastInstance = this;

    }

    @Test
    public void _0_dbReset() {
        Database.reset();
    }


    /**
     * Checks a dummy json API response from the 'index' level.  Dummy response is in
     * test/resource/indexResponse.json
     *
     * @throws JsonProcessingException
     * @throws IOException
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
    public void _2_testDeserialize() throws JsonProcessingException, IOException {
        // To standardize the json string's format, first put into a JsonNode
        // then re-stringify to remove "prettifying" whitespace.
        // With the standardized/minified json, serialize it into the appropriate
        // Pojo objects

        try {
            ObjectMapper mapper = new ObjectMapper();

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

        for (Response.PojoData actual : actualData) {
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

        for (Response.PojoData actual : actualData) {
            Dao dao = (Dao) actual.getPojoClass().getDeclaredField("dao").get(null);
            if (actual.isIndividual()) {
                dao.insert(actual.getIndividualPojo());
                continue;
            }

            for (Pojo pojo : (List<Pojo>) actual.getPojoList()) {
                dao.insert(pojo);
            }
        }

        // Fetch back out of the persistence layer

        persisted = params.response.getClass().getDeclaredConstructor().newInstance();

        for (Response.PojoData actual : actualData) {
            Dao dao = (Dao) actual.getPojoClass().getDeclaredField("dao").get(null);

            if (actual.isIndividual()) {
                Pojo pojo = dao.getById(actual.getIndividualPojo().getId());

                persisted.setData(new Response.PojoData(
                        actual.getFieldName(),
                        pojo.getClass(),
                        pojo
                    )   );
                continue;
            }

            Class<? extends Pojo> pojoClass = null;
            List<Pojo> list = new ArrayList();

            // Sort the arrays to have the same order as the original input

            for (Pojo orig : (List<Pojo>) actual.getPojoList()) {
                Pojo pojo = dao.getById(orig.getId());
                if (pojoClass == null && pojo != null) {
                    pojoClass = pojo.getClass();
                }
                list.add(pojo);
            }

            /*
            List<Pojo> origList = actual.getPojoList();
            list.sort(Comparator.comparingInt(origList::indexOf));
            // not sure if this will work with different instances of same data record,
            // especially with the "dummy" placeholders for one-to-many relationships...
            // depends on implementation of equals method???

            // ... alternative is to iterate over the original list, and fetch each
            // persisted record by id, so they are placed in the list in order
            // ... WHICH IS EXACTLY WHAT I DID! ;-)
             */

            if (pojoClass == null) {
                // prefer to take from the first non-null instance in the list...
                // but failing that, take from the original
                pojoClass = actual.getPojoClass();
            }

            persisted.setData(new Response.PojoData(
                    actual.getFieldName(),
                    pojoClass,
                    list
                ));
        }


        // Set the timestamp the same
        persisted.setTimestamp(deserialized.getTimestamp());

        // Now re-serialize then deserialize into a JsonNode, and compare against the original
        // NOTE: JsonNode equality does not care about the order of properties.  They will almost
        // certainly appear in a different order than the original

        // https://stackoverflow.com/questions/2253750/testing-two-json-objects-for-equality-ignoring-child-order-in-java
        try {
            ObjectMapper mapper = new ObjectMapper();
            String reserialized = mapper.writeValueAsString(persisted);
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
        private final Response response;
        private final List<ExpectedData> expectedData;
        private final Class<? extends Response> responseClass;

        private TestDeserializationPersistance lastInstance;

        private TestParams(
                String filename,
                final Response response,
                List<ExpectedData> expectedData) {

            this.filename = filename;
            this.response = response;
            this.expectedData = expectedData;
            this.responseClass = response.getClass();
        }
    }


    /**************************************
     *  Expected Data inner class
     * @param <POJO> pojo type for the expected data
     **************************************/


    static class ExpectedData<POJO extends Pojo> {
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

            if (o instanceof Response.PojoData) {
                Response.PojoData data = (Response.PojoData) o;

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
