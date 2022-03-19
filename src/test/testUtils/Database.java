package testUtils;

import java.io.*;
import java.sql.*;
import java.util.*;

import nessusTools.data.persistence.Dao;
import org.apache.logging.log4j.*;

// https://ibatis.apache.org/docs/java/dev/com/ibatis/common/jdbc/ScriptRunner.html
// https://mkyong.com/jdbc/how-to-run-a-mysql-script-using-java/
// https://mvnrepository.com/artifact/org.apache.ibatis/ibatis-core/3.0
import org.apache.ibatis.jdbc.ScriptRunner;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Provides access the database
 * Created on 8/31/16.
 *
 * 3/10/22 WJA -- changed from singleton class to static (no instance) utility class
 *
 * @author pwaite
 */

public class Database {
    public static final String DB_SOFT_RESET = "dbSoftReset.sql";
    public static final String DB_HARD_RESET = "dbHardReset.sql";

    private static final Logger logger = LogManager.getLogger(Database.class);

    private static Properties properties = loadProperties();
    private static Connection connection;

    private static final String DATABASE_PROPERTIES_FILE = "/database.properties";

    /**
     * Never used
     */
    private Database() {
        throw new IllegalStateException();
    }

    /**
     * Load up properties for connection info
     */

    private static Properties loadProperties() {
        properties = new Properties();
        try {
            properties.load (Database.class.getResourceAsStream(DATABASE_PROPERTIES_FILE));

        } catch (IOException ioe) {
            logger.error("Database.loadProperties()...Cannot load the properties file", ioe);
        } catch (Exception e) {
            logger.error("Database.loadProperties()...", e);
        }

        return properties;
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
     */
    public static void runSQL(String sqlFile) {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(sqlFile);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            Class.forName(properties.getProperty("driver"));
            connect();

            ScriptRunner runner = new ScriptRunner(getConnection());
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
        softReset();
    }

    public static void softReset() {
        //closeSessions();
        runSQL(DB_SOFT_RESET);
    }

    public static void hardReset() {
        //closeSessions();
        runSQL(DB_HARD_RESET);
    }

    public static void closeSessions() {
        try {
            Session session = null;
            while (session != (session = Dao.sessionFactory.getCurrentSession()) && session != null) {
                session.close();
            }

            Dao.sessionFactory.close();

        } catch (HibernateException e) {
            if (!Objects.equals("No CurrentSessionContext configured!", e.getMessage())) {
                logger.error(e);
            }

        } catch (Throwable e) {
            logger.error(e);
        }
    }
}