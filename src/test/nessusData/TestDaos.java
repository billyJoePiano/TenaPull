package test.nessusData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.apache.logging.log4j.*;




import test.testUtils.Database;

public class TestDaos {
    private static final Logger logger = LogManager.getLogger(TestDaos.class);


    @BeforeEach
    public void setUp() {
        cleanUp();
    }



    public static void cleanUp() {
        Database database = Database.getInstance();
        database.runSQL("dbReset.sql");

    }
}
