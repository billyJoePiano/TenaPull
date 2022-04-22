package data;


import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import testUtils.*;


import org.junit.runner.*;

import com.fasterxml.jackson.databind.*;
import org.junit.*;
import org.junit.runners.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

/**
 * There were some issues with the special methods for JsonGetter/JsonSetter of SeverityBase classes
 * owned by ScanInfo.  Unwanted partial-duplicates and records with null fields were being inserted.
 * This test confirms that each SeverityBase record is complete and unique after inserting a new ScanInfo.
 */
@RunWith(Parameterized.class)
public class TestScanInfoSpecialSeverityBaseMethods {
    public static final String JSON_FILE = "deserializationPersistence-params/ScanInfo.json";

    @Parameterized.Parameters
    public static Collection loadJSON() throws IOException {
        Database.hardReset();

        BufferedReader reader = new BufferedReader(new FileReader(JSON_FILE));
        ObjectMapper mapper = new CustomObjectMapper();
        JsonNode response = mapper.readValue(reader, JsonNode.class);

        return List.of(new Object[][] { { response } });
    }

    private final JsonNode response;

    public TestScanInfoSpecialSeverityBaseMethods(JsonNode response) {
        this.response = response;

    }

    @Test
    public void insertScanInfoAndVerifySeverityBaseEntries() {
        ObjectMapper mapper = new CustomObjectMapper();
        ScanInfo deserialized = mapper.convertValue(response, ScanResponse.class).getInfo();

        // Make sure foreign key constraints will be satisfied before inserting ScanInfo
        Folder.dao.insert(deserialized.getFolder());

        Scan placeholder = new Scan();
        placeholder.setId(deserialized.getId());
        Scan.dao.insert(placeholder);

        int id = ScanInfo.dao.insert(deserialized);

        assertNotEquals(0, id);
        assertNotEquals(-1, id);
        assertTrue(id > 0);

        ScanInfo persisted = ScanInfo.dao.getById(id);

        assertEquals(deserialized, persisted);

        List<SeverityBase> severityBases = SeverityBase.dao.getAll();

        for (int i = 0; i < severityBases.size(); i++) {
            SeverityBase severityBase = severityBases.get(i);
            assertNotNull(severityBase.getValue());
            assertNotNull(severityBase.getDisplay());

            for (int j = i + 1; j < severityBases.size(); j++) {
                SeverityBase other = severityBases.get(j);
                assertNotEquals(other.getId()       , severityBase.getId());
                assertNotEquals(other.getValue()    , severityBase.getValue());
                assertNotEquals(other.getDisplay()  , severityBase.getDisplay());
            }
        }
    }
}
