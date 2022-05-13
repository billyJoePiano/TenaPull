package tenapull.util;

import tenapull.run.*;
import org.apache.ibatis.jdbc.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Provides access to the database
 * Created on 8/31/16.
 * <p>
 * 3/10/22 WJA -- changed from singleton class to static (no instance) utility class
 *
 * @author pwaite
 */
public class Database {
    /**
     * The name of the sql script which performs a full reset of the database
     */
    public static final String DB_HARD_RESET = "dbHardReset.sql";

    private static final Logger logger = LogManager.getLogger(Database.class);

    private static Properties config = Main.getConfig();
    private static Connection connection;

    /**
     * Never used
     */
    private Database() {
        throw new IllegalStateException();
    }

    /**
     * Gets connection.
     *
     * @return the connection
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Connect.
     *
     * @throws Exception the exception
     */
    public static void connect() throws Exception {
        if (connection != null)
            return;

        try {
            Class.forName(config.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new Exception("Database.connect()... Error: MySQL Driver not found");
        }

        String url = config.getProperty("db.url");
        connection = DriverManager.getConnection(url, config.getProperty("db.username"),  config.getProperty("db.password"));
    }

    /**
     * Disconnect.
     */
    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Cannot close connection", e);
            }
        }

        connection = null;
    }

    /**
     * Run the sql.
     *
     * @param sqlFile the sql file to be read and executed line by line
     * @throws FileNotFoundException the file not found exception
     */
    public static void runSQL(String sqlFile) throws FileNotFoundException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(sqlFile);

        if (inputStream == null) {
            throw new FileNotFoundException("Could not find dbHardReset.sql script");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            Class.forName(config.getProperty("db.driver"));
            connect();

            ScriptRunner runner = new ScriptRunner(getConnection());
            runner.runScript(br);


        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);

        } finally {
            disconnect();
        }

    }

    /**
     * Reset the database
     *
     * @throws FileNotFoundException if the hard reset sql script can't be found
     */
    public static void reset() throws FileNotFoundException {
        hardReset();
    }

    /**
     * Reset the database
     *
     * @throws FileNotFoundException if the hard reset sql script can't be found
     */
    public static void hardReset() throws FileNotFoundException {
        logger.info("RUNNING HARD RESET ON DATABASE");
        runSQL(DB_HARD_RESET);
        logger.info("FINISHED HARD RESET ON DATABASE");
    }
}