package tenapull.data.deserialize;

import com.fasterxml.jackson.databind.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.scan.*;
import tenapull.data.entity.template.*;
import tenapull.run.*;
import org.apache.logging.log4j.*;

import java.util.*;


/**
 * Custom Jackson object mapper for serializing the outputs to be ingested by splunk.
 */
public class SplunkOutputMapper extends ObjectMapper {
    private static final Map<Thread, SplunkOutputMapper> mappers = new WeakHashMap<>();
    private static final Logger logger = LogManager.getLogger(SplunkOutputMapper.class);

    public static final Integer TRUNCATE = Main.parseTruncate();


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

        if  (TRUNCATE != null) {
            this.addMixIn(SimpleStringLookupPojo.class, OutputMixIns.StringLookupPojo.class);
            this.addMixIn(StringHashLookupPojo.class, OutputMixIns.StringLookupPojo.class);
        }
    }
}
