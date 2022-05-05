package nessusTools.run;

import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.sync.*;
import nessusTools.util.*;
import org.apache.logging.log4j.*;
import org.hibernate.jdbc.*;

import java.io.*;
import java.util.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Thread main = Thread.currentThread();


    private Main() { } //never instantiated ... static only

    public static void main(String[] args) {
        new Thread(Main::checkHashes).start();

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

    private static void checkHashes() {
        try {
            Thread.sleep(20 * 60 * 1000);
        } catch (InterruptedException e) { }

        List<Class<? extends HashLookupPojo>> hashTypes = List.of(ExtraJson.class, PluginRefInformation.class, PluginDescription.class, PluginSolution.class);
        for (Class<? extends HashLookupPojo> type : hashTypes) {
            List<HashLookupPojo> list;
            StringHashLookupDao strDao = (StringHashLookupDao) StringHashLookupDao.get(type);

            if (strDao != null) {
                list = strDao.getAll();
            } else {
                HashLookupDao dao = (HashLookupDao) HashLookupDao.get(type);
                list = dao.getAll();
            }

            System.out.println();
            System.out.println("############################################################");
            System.out.println(type);
            System.out.println("############################################################");
            System.out.println();

            checkHashes(list);
        }

    }

    private static void checkHashes(List<HashLookupPojo> list) {
        for (HashLookupPojo ej : list) {
            String str = ej.toString();
            Hash hash = ej.get_hash();
            Hash hash2 = new Hash(str);
            PrintStream stream = Objects.equals(hash, hash2) ? System.out : System.err;

            stream.println(ej.getId() + "\t'" + str + "'  (" + str.length() + ")");
            stream.println(hash);
            stream.println(hash2);
            stream.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

}
