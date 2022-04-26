package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;

import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.sql.*;
import java.util.*;

@Table(name = "scan_history")
@Entity(name = "ScanHistory")
public class ScanHistory extends ScanResponse.MultiChildLookup<ScanHistory>
        implements LookupSearchMapProvider<ScanHistory> {

    public static final ObjectLookupDao<ScanHistory>
            dao = new ObjectLookupDao<>(ScanHistory.class);

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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_group_id")
    @JsonProperty("scan_group")
    @JsonDeserialize(using = IdReference.Deserializer.class)
    @JsonSerialize(using = IdReference.Serializer.class)
    private ScanGroup scanGroup;

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

    //@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    //@JoinColumn(name="owner_id")
    //@JsonProperty("owner_id")
    //@JsonDeserialize(using = IdReference.Deserializer.class) //TODO POSSIBLY CHANGE TO ObjectLookup.Deserializer ???
    //@JsonSerialize(using = IdReference.EmptyToNullSerializer.class)
    //private ScanOwnerNessusId ownerId;

    @Column(name = "owner_id")
    @JsonProperty("owner_id")
    private Integer ownerId;

    @Column(name = "history_id")
    @JsonProperty("history_id")
    private Integer historyId;

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
    public boolean _lookupMatch(ScanHistory other) {
        return this.__lookupMatch(other)
                && this.historyId != null && other.historyId != null
                && this.historyId.intValue() == other.historyId.intValue();
    }

    @Transient
    @JsonIgnore
    @Override
    public Map<String, Object> _getSearchMap() {
        return Map.of("response", this.getResponse(), "historyId", this.historyId);
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

    public ScanGroup getScanGroup() {
        return scanGroup;
    }

    public void setScanGroup(ScanGroup scanGroup) {
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
