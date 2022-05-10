package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;

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
        this.addMixIn(Scan.class, OutputMixIns.Scan.class);
        this.addMixIn(ScanInfo.class, OutputMixIns.ScanInfo.class);
        this.addMixIn(ScanHost.class, OutputMixIns.ScanHost.class);
        this.addMixIn(ScanHistory.class, OutputMixIns.ScanHistory.class);
        this.addMixIn(ScanPlugin.class, OutputMixIns.ScanPlugin.class);
        this.addMixIn(PluginHost.class, OutputMixIns.PluginHost.class);
    }
}
