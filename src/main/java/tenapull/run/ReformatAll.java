package tenapull.run;

import com.fasterxml.jackson.databind.*;
import tenapull.client.*;
import tenapull.util.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Quick and dirty utility job for reformatting splunk outputs from the old format into the new format (5-12-2022)
 */
public class ReformatAll extends Job {
    private static final Logger logger = LogManager.getLogger(ReformatAll.class);

    private final boolean ready;
    private final File[] files;

    /**
     * Instantiates a new Reset database job, prompting the user for confirmation first.
     * If the user does not confirm the reset, a flag will be set which tells the job
     * to mark itself as failed once it starts running.
     */
    public ReformatAll() {
        String inputDir = Main.getConfig().getProperty("output.dir");
        inputDir = inputDir.substring(0, inputDir.length() - 1) + ".old/";

        boolean ready = false;
        File[] files = null;

        try {
            File directory = new File(inputDir);
            ready = directory.exists();
            files = directory.listFiles();

        } catch (Exception e) {
            logger.error(e);
        }

        this.files = files;

        if (!ready || files == null) {
            Main.markErrorStatus();
            logger.error("ERROR Couldn't find input directory " + inputDir);
            this.ready = false;
            return;

        }

        Main.initOutputDir();


        this.ready = ready && Main.confirmJob(
                "Found " + this.files.length + " files to reformat\n"
                + " WARNING: THIS WILL OVERWRITE ANY FILES IN THE OUTPUT DIRECTORY "
                + Main.getConfig("output.dir") + " WHERE THERE IS A FILE IN THE OLD DIRECTORY "
                + inputDir +  " WITH THE SAME NAME");

        if (!this.ready) {
            System.err.println("Reformat job cancelled");
            return;
        }
    }

    @Override
    protected boolean isReady() {
        if (!ready) {
            this.failed();
            return false;
        }
        return true;
    }

    @Override
    protected void fetch(NessusClient client) {
        // no need for the client in this job
    }

    @Override
    protected void process() throws IOException {
        /*Files.list(new File(inputDir).toPath()).forEach(path -> {
            this.addJob(new ReformatOutput(path.toFile()));
        });*/

        for (File file : this.files) {
            this.addJob(new ReformatOutput(file));
        }
    }

    @Override
    protected void output() { }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        logger.error("Exception while generating reformat jobs.  Aborting", e);
        this.failed();
        return false;
    }
}
