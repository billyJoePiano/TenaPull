package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;

import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.splunk.*;

public class SplunkOutputSerializer extends ObjectMapper {
    public SplunkOutputSerializer() {
        this.addMixIn(NessusResponseWithTimestamp.class, MixIns.NessusResponseWithTimestamp.class);
        this.addMixIn(Scan.class, MixIns.Scan.class);
        this.addMixIn(ScanInfo.class, MixIns.ScanInfo.class);
        this.addMixIn(ScanHost.class, MixIns.ScanHost.class);
        this.addMixIn(ScanHistory.class, MixIns.ScanHistory.class);
    }
}
