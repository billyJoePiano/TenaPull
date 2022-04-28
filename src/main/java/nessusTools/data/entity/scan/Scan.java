package nessusTools.data.entity.scan;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.data.deserialize.*;
import org.apache.logging.log4j.*;

import javax.persistence.*;

@Entity(name = "Scan")
@Table(name = "scan")
public class Scan extends NaturalIdPojo {
    public static final Dao<Scan> dao
            = new Dao<Scan>(Scan.class);

    public static final Logger logger = LogManager.getLogger(Scan.class);

    @OneToOne(mappedBy = "scan", cascade = CascadeType.ALL)
    @JsonIgnore
    private ScanResponse scanResponse;

    @Column
    private String name;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="uuid_id")
    private ScanUuid uuid;

    /*
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="folder_id")
    @JsonProperty("folder_id")
    @JsonDeserialize(using = IdReference.Deserializer.class)
    @JsonSerialize(using = IdReference.EmptyToNullSerializer.class)
    private Folder folder;
     */

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


    public Scan() { }


/**********************************************
      Standard getters/setters below
***********************************************/


    public ScanResponse getScanResponse() {
        return scanResponse;
    }

    public void setScanResponse(ScanResponse scanResponse) {
        this.scanResponse = scanResponse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScanUuid getUuid() {
        return uuid;
    }

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

    public Integer getFolderId() {
        return this.folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public ScanOwner getOwner() {
        return owner;
    }

    public void setOwner(ScanOwner owner) {
        this.owner = owner;
    }

    public ScanType getType() {
        return type;
    }

    public void setType(ScanType type) {
        this.type = type;
    }

    public String getRrules() {
        return rrules;
    }

    public void setRrules(String rrules) {
        this.rrules = rrules;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    public Integer getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Integer userPermissions) {
        this.userPermissions = userPermissions;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Timestamp getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Timezone getTimezone() {
        return timezone;
    }

    public void setTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    public Integer getLiveResults() {
        return liveResults;
    }

    public void setLiveResults(Integer liveResults) {
        this.liveResults = liveResults;
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() { }
}