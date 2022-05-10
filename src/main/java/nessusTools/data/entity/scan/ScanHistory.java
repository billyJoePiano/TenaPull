package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;

import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;

import javax.persistence.*;
import java.sql.*;
import java.util.*;

@Table(name = "scan_history")
@Entity(name = "ScanHistory")
public class ScanHistory extends ScanResponse.MultiChildLookup<ScanHistory>
        implements MapLookupPojo<ScanHistory> {

    public static final MapLookupDao<ScanHistory>
            dao = new MapLookupDao<>(ScanHistory.class);

    @Column(name = "alt_targets_used")
    @JsonProperty("alt_targets_used")
    private Boolean altTargetsUsed;

    private Integer scheduler;

    @Column(name = "node_name")
    @JsonProperty("node_name")
    private String nodeName;

    @Column(name = "node_host")
    @JsonProperty("node_host")
    private String nodeHost;

    @Column(name = "scan_group")
    @JsonProperty("scan_group")
    Integer scanGroup;

    @Column(name = "node_id")
    @JsonProperty("node_id")
    private Integer nodeId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="schedule_type_id")
    @JsonProperty("schedule_type")
    private ScanScheduleType scheduleType;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="status_id")
    @JsonProperty("status")
    private ScanStatus status;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_type_id")
    private ScanType type;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="uuid_id")
    private ScanUuid uuid;

    @Column(name = "last_modification_date")
    @JsonProperty("last_modification_date")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp lastModificationDate;

    @Column(name = "creation_date")
    @JsonProperty("creation_date")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp creationDate;

    @Column(name = "owner_id")
    @JsonProperty("owner_id")
    private Integer ownerId;

    @Column(name = "history_id")
    @JsonProperty("history_id")
    private Integer historyId;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Transient
    @JsonIgnore
    @Override
    public void _set(ScanHistory o) {
        this.__set(o);
        this.altTargetsUsed = o.altTargetsUsed;
        this.scheduler = o.scheduler;
        this.nodeName = o.nodeName;
        this.nodeHost = o.nodeHost;
        this.scanGroup = o.scanGroup;
        this.nodeId = o.nodeId;
        this.scheduleType = o.scheduleType;
        this.status = o.status;
        this.type = o.type;
        this.uuid = o.uuid;
        this.lastModificationDate = o.lastModificationDate;
        this.creationDate = o.creationDate;
        this.ownerId = o.ownerId;
        this.historyId = o.historyId;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(ScanHistory o) {
        if (o == this) return true;
        return this.__match(o)
                && this.historyId != null && o.historyId != null
                && this.historyId.intValue() == o.historyId.intValue();
    }

    @Transient
    @JsonIgnore
    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[]
                { "response", this.getResponse(), "historyId", this.historyId });
    }

    public Boolean getAltTargetsUsed() {
        return altTargetsUsed;
    }

    public void setAltTargetsUsed(Boolean altTargetsUsed) {
        this.altTargetsUsed = altTargetsUsed;
    }

    public Integer getScheduler() {
        return scheduler;
    }

    public void setScheduler(Integer scheduler) {
        this.scheduler = scheduler;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    /*
    public ScanGroup getScanGroup() {
        return scanGroup;
    }

    public void setScanGroup(ScanGroup scanGroup) {
        this.scanGroup = scanGroup;
    }
    */

    public Integer getScanGroup() {
        return scanGroup;
    }

    public void setScanGroup(Integer scanGroup) {
        this.scanGroup = scanGroup;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public ScanScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScanScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public ScanType getType() {
        return type;
    }

    public void setType(ScanType type) {
        this.type = type;
    }

    public ScanUuid getUuid() {
        return uuid;
    }

    public void setUuid(ScanUuid uuid) {
        this.uuid = uuid;
    }

    public Timestamp getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    /*
    public ScanOwnerNessusId getOwner() {
        return owner;
    }

    public void setOwner(ScanOwnerNessusId owner) {
        this.owner = owner;
    }
     */

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }
}
