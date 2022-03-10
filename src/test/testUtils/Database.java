package test.testUtils;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

// https://ibatis.apache.org/docs/java/dev/com/ibatis/common/jdbc/ScriptRunner.html
// https://mkyong.com/jdbc/how-to-run-a-mysql-script-using-java/
// https://mvnrepository.com/artifact/org.apache.ibatis/ibatis-core/3.0
import org.apache.ibatis.jdbc.ScriptRunner;

/**
 * Provides access the database
 * Created on 8/31/16.
 *
 * @author pwaite
 */

public class Database {
    public static final String DB_RESET = "dbSoftReset.sql";
    public static final String DB_HARD_RESET = "dbHardReset.sql";

    private final Logger logger = LogManager.getLogger(this.getClass());
    private static Database instance = new Database();

    private Properties properties;
    private Connection connection;

    private static final String DATABASE_PROPERTIES_FILE = "/database.properties";

    /**
     * Create the database class
     */
    private Database() {
        loadProperties();

    }

    /**
     * Load up properties for connection info
     */

    private void loadProperties() {
        properties = new Properties();
        try {
            properties.load (this.getClass().getResourceAsStream(DATABASE_PROPERTIES_FILE));
        } catch (IOException ioe) {
            logger.error("Database.loadProperties()...Cannot load the properties file", ioe);
        } catch (Exception e) {
            logger.error("Database.loadProperties()...", e);
        }

    }

    /**
     * Gets instance - singleton pattern usage.
     *
     * @return the instance
     */
    public static Database getInstance() {
        return instance;
    }

    /**
     * Gets connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Connect.
     *
     * @throws Exception the exception
     */
    public void connect() throws Exception {
        if (connection != null)
            return;

        try {
            Class.forName(properties.getProperty("driver"));
        } catch (ClassNotFoundException e) {
            throw new Exception("Database.connect()... Error: MySQL Driver not found");
        }

        String url = properties.getProperty("url");
        connection = DriverManager.getConnection(url, properties.getProperty("username"),  properties.getProperty("password"));
    }

    /**
     * Disconnect.
     */
    public void disconnect() {
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
     */
    public void runSQL(String sqlFile) {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(sqlFile);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            Class.forName(properties.getProperty("driver"));
            connect();

            ScriptRunner runner = new ScriptRunner(this.getConnection());
            runner.runScript(br);


        } catch (SQLException se) {
            logger.error(se);

        } catch (Exception e) {
            logger.error(e);

        } finally {
            disconnect();
        }

    }

    public static void reset() {
        getInstance().runSQL(DB_RESET);
    }

    public static void hardReset() {
        getInstance().runSQL(DB_HARD_RESET);
    }
}