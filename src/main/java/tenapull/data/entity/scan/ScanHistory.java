package tenapull.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;

import tenapull.data.deserialize.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import tenapull.util.*;

import javax.persistence.*;
import java.sql.*;
import java.util.*;

/**
 * Represents an object from the history array returned by the Nessus API in /scans/&lt;scan-id&gt;
 */
@Table(name = "scan_history")
@Entity(name = "ScanHistory")
public class ScanHistory extends ScanResponse.MultiChildLookup<ScanHistory>
        implements MapLookupPojo<ScanHistory> {

    /**
     * The dao for ScanHistory
     */
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
    private Integer scanGroup;

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

    /**
     * Gets alt targets used.
     *
     * @return the alt targets used
     */
    public Boolean getAltTargetsUsed() {
        return altTargetsUsed;
    }

    /**
     * Sets alt targets used.
     *
     * @param altTargetsUsed the alt targets used
     */
    public void setAltTargetsUsed(Boolean altTargetsUsed) {
        this.altTargetsUsed = altTargetsUsed;
    }

    /**
     * Gets scheduler.
     *
     * @return the scheduler
     */
    public Integer getScheduler() {
        return scheduler;
    }

    /**
     * Sets scheduler.
     *
     * @param scheduler the scheduler
     */
    public void setScheduler(Integer scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Gets node name.
     *
     * @return the node name
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets node name.
     *
     * @param nodeName the node name
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Gets node host.
     *
     * @return the node host
     */
    public String getNodeHost() {
        return nodeHost;
    }

    /**
     * Sets node host.
     *
     * @param nodeHost the node host
     */
    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    /**
     * Gets scan group.
     *
     * @return the scan group
     */
    public Integer getScanGroup() {
        return scanGroup;
    }

    /**
     * Sets scan group.
     *
     * @param scanGroup the scan group
     */
    public void setScanGroup(Integer scanGroup) {
        this.scanGroup = scanGroup;
    }

    /**
     * Gets node id.
     *
     * @return the node id
     */
    public Integer getNodeId() {
        return nodeId;
    }

    /**
     * Sets node id.
     *
     * @param nodeId the node id
     */
    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Gets schedule type.
     *
     * @return the schedule type
     */
    public ScanScheduleType getScheduleType() {
        return scheduleType;
    }

    /**
     * Sets schedule type.
     *
     * @param scheduleType the schedule type
     */
    public void setScheduleType(ScanScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public ScanStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public ScanType getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(ScanType type) {
        this.type = type;
    }

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public ScanUuid getUuid() {
        return uuid;
    }

    /**
     * Sets uuid.
     *
     * @param uuid the uuid
     */
    public void setUuid(ScanUuid uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets last modification date.
     *
     * @return the last modification date
     */
    public Timestamp getLastModificationDate() {
        return lastModificationDate;
    }

    /**
     * Sets last modification date.
     *
     * @param lastModificationDate the last modification date
     */
    public void setLastModificationDate(Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * Gets creation date.
     *
     * @return the creation date
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * Sets creation date.
     *
     * @param creationDate the creation date
     */
    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets owner id.
     *
     * @return the owner id
     */
    public Integer getOwnerId() {
        return ownerId;
    }

    /**
     * Sets owner id.
     *
     * @param ownerId the owner id
     */
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Gets history id.
     *
     * @return the history id
     */
    public Integer getHistoryId() {
        return historyId;
    }

    /**
     * Sets history id.
     *
     * @param historyId the history id
     */
    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }
}
