package main.nessusData.entity;

import javax.persistence.*;
import java.util.*;
import java.sql.Timestamp;
import com.fasterxml.jackson.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;
import main.nessusData.persistence.*;
import org.apache.logging.log4j.*;

@Entity(name = "Scan")
@Table(name = "scan")
public class Scan {
    public static final Dao<Scan> dao = new Dao<Scan>(Scan.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(Scan.class);

    @Id
    @JsonProperty
    private int id;

    @Column
    private String name;

    @Column
    @JsonProperty
    private String uuid;

    @ManyToOne
    @JoinColumn(
            name="folder_id",
            foreignKey = @ForeignKey(name = "")
    )
    @JsonProperty("folder_id")
    private Folder folder;

    @ManyToOne
    @JoinColumn(
            name="owner_id",
            foreignKey = @ForeignKey(name = "")
    )
    private ScanOwner owner;

    @ManyToOne
    @JoinColumn(
            name="type_id",
            foreignKey = @ForeignKey(name = "")
    )
    private ScanType type;

    @Column
    private String rrules;

    @Column
    private boolean read;

    @Column
    private boolean shared;

    @Column
    private boolean enabled;

    @Column
    private boolean control;

    @Column(name = "user_permissions")
    @JsonProperty("user_permissions")
    private int userPermissions;

    @Column
    private String status;

    @Column(name = "creation_date")
    @JsonProperty("creation_date")
    private Timestamp creationDate;

    @Column(name = "start_time")
    @JsonProperty("starttime")
    private String startTime;

    @Column(name = "last_modification_date")
    @JsonProperty("last_modification_date")
    private Timestamp lastModificationDate;

    @ManyToOne
    @JoinColumn(
            name="timezone_id",
            foreignKey = @ForeignKey(name = "")
    )
    private Timezone timezone;

    @Column(name = "live_results")
    @JsonProperty("live_results")
    private int liveResults;


    public Scan() { }

    public static Scan fromJSON(String json) throws JsonProcessingException {
        return mapper.readValue(json, Scan.class);
    }

    public String toJSON() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    @JsonProperty("folder_id")
    public void setFolderId(int id) {
        this.setFolder(Folder.dao.getById(id));
    }

    @JsonProperty("folder_id")
    public int getFolderId() {
        return this.getFolder().getId();
    }

    @JsonProperty("timezone")
    public void setTimezone(String timezone) {
        try {
            this.setTimezone(Timezone.dao.getOrCreate(timezone));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @JsonProperty("scan")
    public void setType(String scanType) {
        try {
            this.setType(ScanType.dao.getOrCreate(scanType));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/**********************************************
      Standard getters/setters below
***********************************************/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
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

    public int getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(int userPermissions) {
        this.userPermissions = userPermissions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public int getLiveResults() {
        return liveResults;
    }

    public void setLiveResults(int liveResults) {
        this.liveResults = liveResults;
    }
}