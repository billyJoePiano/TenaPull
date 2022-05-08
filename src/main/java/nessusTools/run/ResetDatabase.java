package nessusTools.run;

import nessusTools.util.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

public class ResetDatabase extends Job {
    private static final Logger logger = LogManager.getLogger(ResetDatabase.class);

    private final boolean runReset;

    public ResetDatabase() {
        System.err.println("WARNING: THIS WILL COMPLETELY RESET THE DATABASE AT ADDRESS '"
                + Main.getConfig().getProperty("db.url") + "'  TYPE 'YES' TO PROCEED");

        String in;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            in = br.readLine();

        } catch (IOException e) {
            logger.error("I/O error reading response, exiting without DB reset", e);
            Main.markErrorStatus();
            this.runReset = false;
            return;
        }

        runReset = Objects.equals("YES", in);
        if (!runReset) {
            System.err.println("DB reset cancelled");
        }
    }

    @Override
    protected boolean isReady() {
        if (!runReset) {
            this.failed();
            return false;
        }
        try {
            Database.connect();
            return Database.getConnection() != null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void fetch() {

    }

    @Override
    protected void process() {
        if (this.runReset) { // shouldn't be needed with this.failed(), but just as a safety measure...
            Database.hardReset();
        }
    }

    @Override
    protected void output() { }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        e.printStackTrace();
        this.failed();
        return false;
    }
}
