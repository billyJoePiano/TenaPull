package nessusTools.run;


import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;

public class MakeJobs extends Job {
    public static final String PARAMS_DIR = "deserializationPersistence-params/";
    private static final Logger logger = LogManager.getLogger(MakeJobs.class);

    ResetDatabase reset = new ResetDatabase();

    Map<Integer, Set<Integer>> ids;

    List<Job> jobs;

    @Override
    protected boolean isReady() {
        return true;
    }

    @Override
    protected void fetch() {
        this.addJob(reset);

        System.out.println("Make jobs, parse filenames");
        ids = parseFilenames();
        System.out.println("Finished make jobs, parse filenames");
    }

    @Override
    protected void process() {
        System.out.println("Make jobs, constructing jobs");
        this.jobs = makeJobs(ids);
        System.out.println("Finished make jobs, constructing jobs");
    }

    @Override
    protected void output() {
        System.out.println("Waiting for DB reset to finish...");
        reset.waitForExit();
        System.out.println("DB reset exited ... MakeJobs is providing jobs");
        this.addJobs(this.jobs);
        System.out.println("Finished make jobs");
    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        return false;
    }

    private Map<Integer, Set<Integer>> parseFilenames() {
        File[] files = new File(PARAMS_DIR).listFiles();
        Map<Integer, Set<Integer>> map = new TreeMap<>();

        for (File file : files) {
            String name = file.getName();
            if (name == null || name.length() <= 11
                    || !(".json".equals(name.substring(name.length() - 5)))) {

                if ("index.json".equals(name)) {
                    map.put(-1, null);

                } else {
                    logger.warn("Invalid filename for test params: " + name);
                }

                continue;
            }

            if ("scan.".equals(name.substring(0, 5))) {
                String idStr = name.substring(5, name.length() - 5);
                int id;
                try {
                    id = Integer.parseInt(idStr);

                } catch (NumberFormatException e) {
                    logger.warn("Invalid filename for test params: " + name, e);
                    continue;
                }

                if (!map.containsKey(id)) {
                    map.put(id, new TreeSet<>());
                }
                continue;

            } else if (!("host.".equals(name.substring(0, 5)))) {
                logger.warn("Invalid filename for test params: " + name);
                continue;
            }

            String[] idStr = name.split("\\.");

            if (idStr.length != 4) {
                logger.warn("Invalid filename for test params: " + name);
                continue;
            }

            int scanId;
            int hostId;
            try {
                scanId = Integer.parseInt(idStr[1]);
                hostId = Integer.parseInt(idStr[2]);

            } catch (NumberFormatException e) {
                logger.warn("Invalid filename for test params: " + name, e);
                continue;
            }

            Set<Integer> hosts = map.get(scanId);
            if (hosts == null) {
                hosts = new TreeSet<>();
                map.put(scanId, hosts);
            }
            hosts.add(hostId);
        }
        return map;
    }

    private List<Job> makeJobs(Map<Integer, Set<Integer>> ids) {
        List<Job> list = new LinkedList<>();
        GetFileInputJob<IndexResponse> indexJob = null;
        Map<Integer, Job> scans = new LinkedHashMap<>();

        if (ids.containsKey(-1)) {
            ids.remove(-1);
            indexJob = new GetFileInputJob<>("index.json", IndexResponse.class, null, null);
            list.add(indexJob);
        }


        for (Map.Entry<Integer, Set<Integer>> entry : ids.entrySet()) {
            int scanId = entry.getKey();

            GetFileInputJob<ScanResponse> scanJob = new GetFileInputJob<>(
                    "scan." + scanId + ".json",
                    ScanResponse.class,
                    node -> {
                        node.remove("filters");
                        ScanResponse scan = new ScanResponse();
                        scan.setId(scanId);
                        ScanResponse.dao.insert(scan);
                        return scanId;
                    },
                    null);

            if (indexJob != null) {
                indexJob.addJobForAfter(scanJob);
            } else {
                list.add(scanJob);
            }


            for (int hostId : entry.getValue()) {
                scanJob.addJobForAfter(new GetFileInputJob<ScanHostResponse>(
                        "host." + scanId + "." + hostId + ".json",
                        ScanHostResponse.class,
                        node -> {
                            ScanHost host = new ScanHost();
                            host.setHostId(hostId);
                            ScanResponse response = ScanResponse.dao.getById(scanId);
                            if (response == null) {

                                fail("Unable to find scan response matching id: " + scanId);
                            }
                            host.setResponse(response);

                            ScanHost actual = ScanHost.dao.getOrCreate(host);
                            ScanHostResponse hostRes = new ScanHostResponse();

                            hostRes.setId(actual.getId());
                            ScanHostResponse.dao.insert(hostRes);
                            return actual.getId();
                        },
                        null
                ));
            }
        }

        return list;
    }
}
