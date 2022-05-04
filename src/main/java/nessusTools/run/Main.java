package nessusTools.run;

import nessusTools.sync.*;
import nessusTools.util.*;
import org.apache.logging.log4j.*;
import org.hibernate.jdbc.*;

import java.util.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Thread main = Thread.currentThread();


    private Main() { } //never instantiated ... static only

    public static void main(String[] args) {
        Var<JobFactory.Init> init = new Var();
        JobFactory.init(init);
        init.value.runJobsLoop(new IndexJob());
    }

    public static boolean isMain() {
        return isMain(Thread.currentThread());
    }

    public static boolean isMain(Thread thread) {
        return Objects.equals(main, thread);
    }



}
