package nessusTools.run;

import nessusTools.client.*;
import nessusTools.util.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

/**
 * A job that resets the database.  Typically, this would be the only job
 * run by the application if it were being run.  When this job is constructed
 * with the default constructor, the user will be prompted for confirmation
 * before proceeding with the reset.  Because it is done in the constructor,
 * rather than in one of the job run methods, this will happen on the thread
 * constructing the job (typically the main thread) not the thread running it.
 *
 * If the non-default constructor is used (with a single null argument), the
 * user will NOT be prompted for confirmation.
 */
public class ResetDatabase extends Job {
    private static final Logger logger = LogManager.getLogger(ResetDatabase.class);

    private final boolean runReset;

    /**
     * Instantiates a new Reset database job, prompting the user for confirmation first.
     * If the user does not confirm the reset, a flag will be set which tells the job
     * to mark itself as failed once it starts running.
     */
    public ResetDatabase() {
        //NOTE to Paula:  I used the stdout/stderr stream here (instead of log4j)
        // due to need for command-line interaction with the user

        System.err.println("WARNING: THIS WILL COMPLETELY RESET THE DATABASE AT ADDRESS '"
                + Main.getConfig().getProperty("db.url") + "'   ALL DATA WILL BE LOST");
        System.err.println("TYPE 'YES' TO PROCEED (case-sensitive)");

        String in;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            in = br.readLine();

        } catch (IOException e) {
            logger.error("I/O error reading response, exiting without DB reset", e);
            Main.markErrorStatus();
            this.runReset = false;
            return;
        }

        this.runReset = Objects.equals("YES", in);
        if (!this.runReset) {
            System.err.println("DB reset cancelled");
        }
    }

    /**
     * Instantiates a new Reset database job WITHOUT prompting the user for confirmation.
     * Use with extreme caution.
     *
     * @param skipConfirm null parameter, used to differentiate this constructor
     */
    public ResetDatabase(Void skipConfirm) {
        this.runReset = true;
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
    protected void fetch(NessusClient client) {

    }

    @Override
    protected void process() throws FileNotFoundException {
        if (this.runReset) { // shouldn't be needed with this.failed(), but just as a safety measure...
            Database.hardReset();
        }
    }

    @Override
    protected void output() { }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        logger.error("Exception while resetting database.  Aborting the job", e);
        this.failed();
        return false;
    }
}
