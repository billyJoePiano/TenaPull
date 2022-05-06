package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.splunk.*;

import java.util.*;

public class SplunkOutputMapper extends ObjectMapper {
    private static final Map<Thread, SplunkOutputMapper> mappers = new WeakHashMap<>();

    public static SplunkOutputMapper get() {
        Thread current = Thread.currentThread();
        SplunkOutputMapper mapper;
        synchronized (mappers) {
            mapper = mappers.get(current);
        }
        if (mapper == null) {
            mapper = new SplunkOutputMapper();
            synchronized (mappers) {
                mappers.put(current, mapper);
            }
        }
        return mapper;
    }


    protected SplunkOutputMapper() {
        super();
        this.addMixins();
    }

    private void addMixins() {
        this.addMixIn(Scan.class, SplunkMixIns.Scan.class);
        this.addMixIn(ScanInfo.class, SplunkMixIns.ScanInfo.class);
        this.addMixIn(ScanHost.class, SplunkMixIns.ScanHost.class);
        this.addMixIn(ScanHistory.class, SplunkMixIns.ScanHistory.class);
        this.addMixIn(ScanPlugin.class, SplunkMixIns.ScanPlugin.class);
        this.addMixIn(PluginHost.class, SplunkMixIns.PluginHost.class);
    }
}
