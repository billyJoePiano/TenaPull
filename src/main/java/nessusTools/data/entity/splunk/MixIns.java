package nessusTools.data.entity.splunk;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.scan.*;

import java.sql.*;

public class MixIns {
    private MixIns() { }

    public abstract class NessusResponseWithTimestamp {
        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp timestamp;
    }

    public abstract class Scan {
        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp creationDate;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp lastModificationDate;
    }

    public abstract class ScanInfo {
        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp scannerStart;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp scannerEnd;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp timestamp;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp scanStart;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp scanEnd;
    }

    public abstract class ScanHost {
        @JsonSerialize(using = SeverityCountsReducer.class)
        private SeverityCount severitycount;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp creationDate;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp lastModificationDate;
    }

    public abstract class ScanHistory {
        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp lastModificationDate;

        @JsonSerialize(using = FriendlyTimestamp.class)
        private Timestamp creationDate;
    }
}
