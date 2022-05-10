package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;

import java.sql.*;
import java.util.*;

/**
 * A non-instantiable class containing a series of inner classes (also non-instantiable
 * by extension, since they are not *static* inner classes) to be used as ObjectMapper mix-ins
 * when serializing the output.  Each inner class is named after the entity it will be used
 * as a mix-in for
 */
public class OutputMixIns {
    private OutputMixIns() { }

    /**
     * Output mixin for the Scan class
     */
    public abstract class Scan {
        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp creationDate;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp lastModificationDate;

        @JsonSerialize(using = FriendlyTimestamp.JsString.class)
        private String startTime;
    }

    /**
     * Output mixin for the ScanInfo class
     */
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

    /**
     * Output mixin for the ScanHost class
     */
    public abstract class ScanHost {
        @JsonSerialize(using = SeverityCountsReducer.class)
        private SeverityCount severitycount;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp creationDate;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp lastModificationDate;
    }

    /**
     * Output mixin for the ScanHistory class
     */
    public abstract class ScanHistory {
        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp lastModificationDate;

        @JsonSerialize(using = FriendlyTimestamp.Sql.class)
        private Timestamp creationDate;
    }

    /**
     * Output mixin for the ScanPlugin class
     */
    public abstract class ScanPlugin {
        @JsonIgnore
        private List<PluginHost> hosts; //renders the below unnecessary...
    }

    /**
     * Output mixin for the PluginHost class
     */
    public abstract class PluginHost {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private HostFqdn hostFqdn;
    }
}
