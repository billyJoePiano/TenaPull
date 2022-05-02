package nessusTools.data.entity.splunk;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;

import java.sql.*;
import java.util.*;

public class MixIns {
    private MixIns() { }

    public abstract class Scan {
        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp creationDate;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp lastModificationDate;

        @JsonSerialize(using = FriendlyTimestamp.JsString.class)
        private String startTime;
    }

    public abstract class ScanInfo {
        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp scannerStart;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp scannerEnd;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp timestamp;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp scanStart;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp scanEnd;

        @JsonSerialize(using = FriendlyTimestamp.NumericString.class)
        private String severityProcessed;

        @JsonSerialize(contentUsing = SeverityBaseReducer.class)
        private List<SeverityBase> severityBaseSelections;
    }

    public abstract class ScanHost {
        @JsonSerialize(using = SeverityCountsReducer.class)
        private SeverityCount severitycount;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp creationDate;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp lastModificationDate;
    }

    public abstract class ScanHistory {
        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp lastModificationDate;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp creationDate;
    }

    public abstract class ScanPlugin {
        @JsonIgnore
        private List<PluginHost> hosts; //renders the below unnecessary...
    }

    public abstract class PluginHost {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private HostFqdn hostFqdn;
    }
}
