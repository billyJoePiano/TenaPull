package tenapull.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.scan.*;

import javax.persistence.*;
import java.sql.*;
import java.util.*;

/**
 * A non-instantiable class containing a series of inner classes (also non-instantiable
 * by extension, since they are not *static* inner classes) to be used as ObjectMapper mix-ins
 * when serializing the output.  Each inner class is named after the entity for which it will be
 * used as a mix-in
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
    @JsonPropertyOrder({"a_best_guess", "host_count"})
    public abstract class ScanPlugin {
        public static final String BEST_GUESS_MSG =
                "A matching plugin could not be found in the scan's data, "
                        + "however TenaPull found this plugin in its database which may (or may not) be correct.";

        public static final String BEST_GUESS_FIELD = "a_best_guess";

        @JsonGetter("a_best_guess")
        @JsonIgnore(false)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getBestGuess() {
            return null;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Integer hostCount;

        @JsonIgnore
        private List<PluginHost> hosts; //renders the below class unnecessary...
    }

    /**
     * Output mixin for the PluginHost class
     */
    public abstract class PluginHost {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private HostFqdn hostFqdn;
    }

    /**
     * Output mixin for the two StringLookupPojo branches (SimpleStringLookupPojo and StringHashLookupPojo)
     * only used if the output.truncate configuration is set.
     */
    @JsonSerialize(using = Truncater.class)
    public abstract class StringLookupPojo {

    }
}
