package tenapull.data.entity.scan;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import tenapull.data.deserialize.*;
import org.apache.logging.log4j.*;

import javax.persistence.*;

/**
 * Represents a scan object returned by the Nessus API in /scans
 */
@Entity(name = "Scan")
@Table(name = "scan")
public class Scan extends NaturalIdPojo {
    /**
     * The dao for Scan
     */
    public static final Dao<Scan> dao
            = new Dao<Scan>(Scan.class);

    /**
     * The logger for Scan
     */
    public static final Logger logger = LogManager.getLogger(Scan.class);

    @OneToOne(mappedBy = "scan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private ScanResponse scanResponse;

    @Column
    private String name;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="uuid_id")
    private ScanUuid uuid;

    @Column(name = "folder_id")
    @JsonProperty("folder_id")
    private Integer folderId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="owner_id")
    private ScanOwner owner;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_type_id")
    private ScanType type;

    @Column
    private String rrules;

    @Column(name = "`read`")
    private boolean read;

    @Column
    private boolean shared;

    @Column
    private boolean enabled;

    @Column
    private boolean control;

    @Column(name = "user_permissions")
    @JsonProperty("user_permissions")
    private Integer userPermissions;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="status_id")
    @JsonProperty("status")
    private ScanStatus status;

    @Column(name = "creation_date")
    @JsonProperty("creation_date")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp creationDate;

    @Column(name = "start_time")
    @JsonProperty("starttime")
    private String startTime;

    @Column(name = "last_modification_date")
    @JsonProperty("last_modification_date")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp lastModificationDate;

    @ManyToOne
    @JoinColumn(
            name="timezone_id",
            foreignKey = @ForeignKey(name = "scan_timezone_id_fk")
    )
    private Timezone timezone;

    @Column(name = "live_results")
    @JsonProperty("live_results")
    private Integer liveResults;


    /**
     * Instantiates a new Scan.
     */
    public Scan() { }


    /**********************************************
     *Standard getters/setters below
     * @return the scan response
     */
    public ScanResponse getScanResponse() {
        return scanResponse;
    }

    /**
     * Sets scan response.
     *
     * @param scanResponse the scan response
     */
    public void setScanResponse(ScanResponse scanResponse) {
        this.scanResponse = scanResponse;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
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

    /*
    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
     */

    /**
     * Gets folder id.
     *
     * @return the folder id
     */
    public Integer getFolderId() {
        return this.folderId;
    }

    /**
     * Sets folder id.
     *
     * @param folderId the folder id
     */
    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public ScanOwner getOwner() {
        return owner;
    }

    /**
     * Sets owner.
     *
     * @param owner the owner
     */
    public void setOwner(ScanOwner owner) {
        this.owner = owner;
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
     * Gets rrules.
     *
     * @return the rrules
     */
    public String getRrules() {
        return rrules;
    }

    /**
     * Sets rrules.
     *
     * @param rrules the rrules
     */
    public void setRrules(String rrules) {
        this.rrules = rrules;
    }

    /**
     * Is read boolean.
     *
     * @return the boolean
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Sets read.
     *
     * @param read the read
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Is shared boolean.
     *
     * @return the boolean
     */
    public boolean isShared() {
        return shared;
    }

    /**
     * Sets shared.
     *
     * @param shared the shared
     */
    public void setShared(boolean shared) {
        this.shared = shared;
    }

    /**
     * Is enabled boolean.
     *
     * @return the boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets enabled.
     *
     * @param enabled the enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Is control boolean.
     *
     * @return the boolean
     */
    public boolean isControl() {
        return control;
    }

    /**
     * Sets control.
     *
     * @param control the control
     */
    public void setControl(boolean control) {
        this.control = control;
    }

    /**
     * Gets user permissions.
     *
     * @return the user permissions
     */
    public Integer getUserPermissions() {
        return userPermissions;
    }

    /**
     * Sets user permissions.
     *
     * @param userPermissions the user permissions
     */
    public void setUserPermissions(Integer userPermissions) {
        this.userPermissions = userPermissions;
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
     * Gets start time.
     *
     * @return the start time
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
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
     * Gets timezone.
     *
     * @return the timezone
     */
    public Timezone getTimezone() {
        return timezone;
    }

    /**
     * Sets timezone.
     *
     * @param timezone the timezone
     */
    public void setTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    /**
     * Gets live results.
     *
     * @return the live results
     */
    public Integer getLiveResults() {
        return liveResults;
    }

    /**
     * Sets live results.
     *
     * @param liveResults the live results
     */
    public void setLiveResults(Integer liveResults) {
        this.liveResults = liveResults;
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }
}