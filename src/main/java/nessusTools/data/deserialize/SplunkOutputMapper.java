package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;

import java.util.*;


/**
 * Custom Jackson object mapper for serializing the outputs to be ingested by splunk.
 */
public class SplunkOutputMapper extends ObjectMapper {
    private static final Map<Thread, SplunkOutputMapper> mappers = new WeakHashMap<>();

    /**
     * Get the Splunk output mapper for the current thread
     *
     * @return the splunk output mapper for the current thread
     */
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


    /**
     * Instantiates a new Splunk output mapper.
     */
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
