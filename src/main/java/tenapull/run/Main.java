package tenapull.run;

import tenapull.util.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

/**
 * The entry point to the TenaPull application.  Processes the config file provided in
 * command line arg, and then enters the jobs loop if the config is valid.  If config
 * is invalid, this will return a descriptive error message and then exit.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Thread main = Thread.currentThread();

    /**
     * The configuration file to use for Unit tests, since they do NOT enter the application
     * through the public static void main(String[] args) method of Main.
     */
    public static final String TESTS_CONFIG = "tests.properties";

    /**
     * Flag to indicate there was an error in the application.  If marked as true,
     * the application will exist with status code of -1
     */
    private static boolean error = false;
    private static final Properties config = new Properties();
    private static Class<? extends Job> mainJobType;

    private static final List<String> EXPECTED_CONFIGS = List.of("output.dir",
            "api.url.protocol", "api.url.host", "api.key.access", "api.key.secret",
            "db.url.protocol", "db.url.host", "db.url.name", "db.driver", "db.dialect",
            "db.username", "db.password");

    private static final Map<String, Class<? extends Job>> ACTION_ARGS =
            Map.of("resetDb", ResetDatabase.class,
                    "dbReset", ResetDatabase.class,
                    "reset", ResetDatabase.class,
                    "reformat", ReformatAll.class);

    private Main() { } //never instantiated ... static only


    /**
     * The entry point of the application.  There should be at least one arg
     * which indicates the configuration file to use for this run of TenaPull.
     * The config file itself should have a .properties extension, but the extension may
     * be left out of the command line argument (e.g. myscanner1.properties
     * could be myscanner1)
     *
     * There may be an optional second argument specifying to perform a database
     * reset instead of the normal Nessus API tasks.  This argument may be "reset",
     * "dbReset", or "resetDb."  The database reset will first prompt for a confirmation
     * before performing the reset.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws IOException {
        System.out.println(new java.io.File(".").getCanonicalPath());

        if (validateArgs(args) && loadConfig(args[0])) {
            Var<Job> seed = new Var<>(makeSeedJob(args));
            //Var is used so that JobFactory init can remove the strong reference to the job after adding it to the
            // list of jobs, therefore allowing it to be GC'd after completion

            if (seed.value != null) {
                Var<JobFactory.Init> init = new Var();
                JobFactory.init(init);
                init.value.runJobsLoop(seed);
            }
        }
        System.exit(error ? -1 : 0);
    }

    /**
     * Returns whether the current thread is the main thread or not
     *
     * @return true if the invoking thread is the main thread, false is not.
     */
    public static boolean isMain() {
        return isMain(Thread.currentThread());
    }

    /**
     * Returns whether the provided thread is the main thread or not
     *
     * @return true if the provided thread is the main thread, false is not.
     */
    public static boolean isMain(Thread thread) {
        return Objects.equals(main, thread);
    }

    /**
     * Marks the application as having an error, so that it will exit with status code -1
     */
    public static void markErrorStatus() {
        error = true;
    }

    /**
     * Gets whether the error status flag has been marked.
     *
     * @return true if the error status flag has been marked, false if not
     */
    public static boolean getErrorStatus() {
        return error;
    }

    /**
     * Gets a copy of the application config, as parsed from the filename provided
     * in the command line arg[0]
     *
     * @return a copy of the config
     */
    public static Properties getConfig() {
        return (Properties) config.clone();
    }

    public static String getConfig(String property) {
        return config.getProperty(property);
    }

    /**
     * Loads the default config for unit tests, since they do not enter the application
     * through the usual public static main(String[] args) of this class
     *
     * @return true if the configuration was valid, false if not
     * @throws IllegalStateException if another config was already loaded
     */
    public static boolean loadTestConfig() throws IllegalStateException {
        if (config.size() > 0) {
            throw new IllegalStateException(
                    "You can only call Main.loadConfigForTest when no other config has been processed");
        }
        return loadConfig(TESTS_CONFIG);
    }


    private static boolean validateArgs(String[] args) {
        if (args == null || args.length < 1 || args[0] == null || args[0].length() < 1) {
            logger.error("No configuration file specified on command line argument");
            error = true;
            return false;
        }

        if (args.length > 1 && !ACTION_ARGS.containsKey(args[1])) {
            logger.error("Unrecognized action argument: '" + args[1] + "'");
            error = true;
            return false;
        }

        if (args[0].length() <= 11
                || !Objects.equals(args[0].substring(args[0].length() - 11), ".properties")) {

            args[0] += ".properties";
        }
        return true;
    }

    private static boolean loadConfig(String file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);

        } catch (FileNotFoundException e) { }

        if (inputStream == null) {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            inputStream = classloader.getResourceAsStream(file);

            if (inputStream == null) {
                logger.error("Could not find configuration file: '" + file + "'");
                error = true;
                return false;
            }
        }

        try {
            config.load(inputStream);

        } catch (Exception e) {
            logger.error("Error loading configurations from file " + file, e);
            error = true;
            return false;
        }

        List<String> missing = new ArrayList();
        for (String key : EXPECTED_CONFIGS) {
            if (!config.containsKey(key)) {
                missing.add(key);
            }
        }

        if (missing.size() > 0) {
            logger.error("Your configuration file '" + file
                    + "' is missing the following required propert"
                    + (missing.size() > 1 ? "ies: " : "y: ")
                    + missing.stream().reduce("",
                            (str, str2) -> str + ", '" + str2 + "'")
                            .substring(2));
            error = true;
            return false;
        }

        config.put("db.url", buildUrl(config, "db") + "/" + config.getProperty("db.url.name"));
        config.put("api.url", buildUrl(config, "api"));

        if (config.containsKey("output.scanner.omit")) {
            config.put("output.scanner", null);

        } else if (!config.containsKey("output.scanner")) {
            config.put("output.scanner", config.get("api.url.host"));
        }

        String dir = config.getProperty("output.dir");

        int length;
        if (dir == null || (length = dir.length()) <= 0) {
            logger.error("Configuration key 'output.dir' cannot be empty");
            error = true;
            return false;

        } else if (!Objects.equals(dir.substring(length - 1, length), "/")) {
            config.put("output.dir", dir += "/");
        }

        return true;
    }

    private static Job makeSeedJob(String[] args) {
        System.out.println();
        System.out.println("ARGS:");
        for (String arg :args) {
            System.out.println("'" + arg + "'");
        }

        System.out.println("ARGS DONE");
        System.out.println();

        if (args.length == 1) {
            mainJobType = IndexJob.class;
            return new IndexJob();
        }

        mainJobType = ACTION_ARGS.get(args[1]);

        try {
            return mainJobType.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            logger.error("Error while trying to construct job of type " + mainJobType, e);
            error = true;
            return null;
        }
    }

    /**
     * Used to build a url string from the provided config and prefix.  Used by both
     * "db" prefix and "api" prefix.
     *
     * @param config the config to parse for a url
     * @param prefix the prefix to obtain the url components from (typically either "db" or "api")
     * @return the string
     */
    public static String buildUrl(Properties config, String prefix) {
        String url = config.getProperty(prefix + ".url.protocol") + "://"
                        + config.getProperty(prefix + ".url.host");

        if (config.containsKey(prefix + ".url.port")) {
            url += ":" + config.getProperty(prefix + ".url.port");
        }
        return url;
    }

    public static Class<? extends Job> getMainJobType() {
        return mainJobType;
    }

    public static boolean initOutputDir() {
        String dir = config.getProperty("output.dir");
        logger.info("Checking output directory: " + dir);
        try {
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdir();
            }
            return true;

        } catch (Exception e) {
            logger.error("Error while trying to make output directory '" + dir + "'", e);
            return false;
        }
    }

    public static Integer getTruncate() {
        String str = config.getProperty("output.truncate");
        if (str == null) return null;
        try {
            Integer result = Integer.parseInt(str);
            if (result == null || result <= 0) {
                logger.warn("Invalid output.truncate configuration: " + result
                        + "\nDefaulting to no field truncation");
                Thread.sleep(3000);
                return null;
            }

            return result;

        } catch (NumberFormatException e) {
            logger.error("Error parsing output.truncate, must be a valid integer: " + str
                    + "\nDefaulting to no field truncation", e);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) { }

            return null;

        } catch (InterruptedException e) {
            return null;
        }

    }

    public static boolean confirmJob(String msg) {
        System.err.println(msg);
        System.err.println("TYPE 'YES' TO PROCEED (case-sensitive)");
        String in = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            in = br.readLine();

        } catch (IOException e) {
            logger.error("I/O error reading your response", e);
            return false;
        }

        return Objects.equals("YES", in);
    }
}
