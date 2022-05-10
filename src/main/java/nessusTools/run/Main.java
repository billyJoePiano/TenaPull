package nessusTools.run;

import nessusTools.util.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Thread main = Thread.currentThread();

    private static boolean error = false;

    private static final Properties config = new Properties();

    private static final List<String> EXPECTED_CONFIGS = List.of("output.dir",
            "api.url.protocol", "api.url.host", "api.key.access", "api.key.secret",
            "db.url.protocol", "db.url.host", "db.url.name", "db.driver", "db.dialect",
            "db.username", "db.password");

    private static final Map<String, Class<? extends Job>> ACTION_ARGS =
            Map.of("resetDb", ResetDatabase.class,
                    "dbReset", ResetDatabase.class,
                    "reset", ResetDatabase.class);

    private Main() { } //never instantiated ... static only

    public static void main(String[] args) {
        Var<Job> seed = new Var<>(loadConfig(args));
        //Var is used so that JobFactory init can remove the strong reference to the job after adding it to the
        // list of jobs, therefore allowing it to be GC'd after completion
        if (seed.value != null) {
            Var<JobFactory.Init> init = new Var();
            JobFactory.init(init);
            init.value.runJobsLoop(seed);
        }
        System.exit(error ? -1 : 0);
    }

    public static boolean isMain() {
        return isMain(Thread.currentThread());
    }

    public static boolean isMain(Thread thread) {
        return Objects.equals(main, thread);
    }

    public static void markErrorStatus() {
        error = true;
    }

    public static boolean getErrorStatus() {
        return error;
    }

    public static Properties getConfig() {
        return (Properties) config.clone();
    }

    private static Job loadConfig(String[] args) {
        if (args == null || args.length < 1 || args[0] == null || args[0].length() < 1) {
            logger.error("No configuration file specified on command line argument");
            error = true;
            return null;
        }

        if (args.length > 1 && !ACTION_ARGS.containsKey(args[1])) {
            logger.error("Unrecognized action argument: '" + args[1] + "'");
            error = true;
            return null;
        }

        if (args[0].length() <= 11
                || !Objects.equals(args[0].substring(args[0].length() - 11), ".properties")) {

            args[0] += ".properties";
        }

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(args[0]);
        if (inputStream == null) {
            logger.error("Could not find configuration file: '" + args[0] + "'");
            error = true;
            return null;
        }

        try {
            config.load(inputStream);

        } catch (Exception e) {
            logger.error("Error loading configurations from file " + args[0], e);
            error = true;
            return null;
        }

        List<String> missing = new ArrayList();
        for (String key : EXPECTED_CONFIGS) {
            if (!config.containsKey(key)) {
                missing.add(key);
            }
        }

        if (missing.size() > 0) {
            logger.error("Your configuration file '" + args[0]
                    + "' is missing the following required propert"
                    + (missing.size() > 1 ? "ies: " : "y: ")
                    + missing.stream().reduce("",
                            (str, str2) -> str + ", '" + str2 + "'")
                            .substring(2));
            error = true;
            return null;
        }

        config.put("db.url", buildUrl(config, "db") + "/" + config.getProperty("db.url.name"));
        config.put("api.url", buildUrl(config, "api"));

        if (config.containsKey("output.scanner.omit")) {
            config.put("output.scanner", null);

        } else if (!config.containsKey("output.scanner")) {
            config.put("output.scanner", config.get("api.url"));
        }

        String dir = config.getProperty("output.dir");

        int length;
        if (dir == null || (length = dir.length()) <= 0) {
            logger.error("Configuration key 'output.dir' cannot be empty");
            error = true;
            return null;

        } else if (!Objects.equals(dir.substring(length - 1, length), "/")) {
            config.put("output.dir", dir += "/");
        }


        if (args.length == 1) {
            return new IndexJob();
        }

        Class<? extends Job> jobType = ACTION_ARGS.get(args[1]);

        try {
            return jobType.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            logger.error("Error while trying to construct job of type " + jobType, e);
            error = true;
            return null;
        }
    }

    public static String buildUrl(Properties config, String prefix) {
        String url = config.getProperty(prefix + ".url.protocol") + "://"
                        + config.getProperty(prefix + ".url.host");

        if (config.containsKey(prefix + ".url.port")) {
            url += ":" + config.getProperty(prefix + ".url.port");
        }
        return url;
    }
}
