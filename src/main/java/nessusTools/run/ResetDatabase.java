package nessusTools.run;

import nessusTools.util.*;

public class ResetDatabase extends Job {

    @Override
    protected boolean isReady() {
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
        Database.hardReset();
    }

    @Override
    protected void output() { }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        e.printStackTrace();
        return false;
    }
}
