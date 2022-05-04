package nessusTools.data.deserialize;

import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.splunk.*;

public class SplunkOutputMapper extends CachingMapper {
    public static final SplunkOutputMapper mapper = new SplunkOutputMapper();
    public static final SplunkOutputMapper resetCaches = new SplunkOutputMapper(null);
    protected SplunkOutputMapper() {
        super();
        this.addMixins();
    }

    protected SplunkOutputMapper(Void resetCaches) {
        super(resetCaches);
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
