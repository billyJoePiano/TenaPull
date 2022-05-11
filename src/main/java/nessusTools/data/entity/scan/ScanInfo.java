package nessusTools.data.entity.scan;


import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.lookup.Scanner;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import nessusTools.data.deserialize.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.apache.logging.log4j.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.*;

/**
 *
 */
@Entity(name = "ScanInfo")
@Table(name = "scan_info")
@JsonIgnoreProperties({"id"})
public class ScanInfo extends ScanResponse.SingleChild<ScanInfo> {

    public static final Dao<ScanInfo> dao
            = new Dao<ScanInfo>(ScanInfo.class);

    public static final Logger logger = LogManager.getLogger(ScanInfo.class);

    @Column(name = "folder_id")
    private Integer folder;

    private String name;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="uuid_id")
    private ScanUuid uuid;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_type_id")
    @JsonProperty("scan_type")
    private ScanType scanType;

    @Column(name = "edit_allowed")
    @JsonProperty("edit_allowed")
    private boolean editAllowed;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @JoinTable(
            name = "scan_info_acl",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "acl_id") }
    )
    @OrderColumn(name = "__order_for_scan_info", nullable = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Acl> acls;


    @Column(name = "scan_group")
    @JsonProperty("scan_group")
    private Integer scanGroup;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_targets_id")
    private ScanTargets targets;

    @Column(name = "scanner_start")
    @JsonProperty("scanner_start")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp scannerStart;

    @Column(name = "scanner_end")
    @JsonProperty("scanner_end")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp scannerEnd;

    @Column(name = "oses_found")
    @JsonProperty("oses_found")
    private Boolean osesFound;

    @Column(name = "host_count")
    @JsonProperty("hostcount")
    private int hostCount;

    private Boolean haskb;

    @Column(name = "exploitable_vulns")
    @JsonProperty("exploitable_vulns")
    private Boolean exploitableVulns;

    @Column(name = "hosts_vulns")
    @JsonProperty("hosts_vulns")
    private Boolean hostsVulns;

    private Integer migrated;

    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp timestamp;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="policy_id")
    private ScanPolicy policy;

    @Column(name = "year_old_vulns")
    @JsonProperty("year_old_vulns")
    private Boolean yearOldVulns;

    private Boolean top10;

    @Column(name = "pci_can_upload")
    @JsonProperty("pci-can-upload")
    private Boolean pciCanUpload;


    // TODO determine actual data type of this in API
    @Column(name = "node_name")
    @JsonProperty("node_name")
    private String nodeName;

    @Column(name = "scan_start")
    @JsonProperty("scan_start")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp scanStart;

    @Column(name = "scan_end")
    @JsonProperty("scan_end")
    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp scanEnd;

    @Column(name = "has_audit_trail")
    @JsonProperty("hasaudittrail")
    private Boolean hasAuditTrail;

    private Boolean control;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scanner_id")
    @JsonProperty("scanner_name")
    private Scanner scanner;

    @Column(name = "unsupported_software")
    @JsonProperty("unsupported_software")
    private Boolean unsupportedSoftware;

    @Column(name = "object_id")
    @JsonProperty("object_id")
    private Integer objectId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="license_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("license_info")
    @JsonInclude(Include.NON_NULL)
    private License licenseInfo;

    @Column(name = "no_target")
    @JsonProperty("no_target")
    private Boolean noTarget;

    // TODO determine data type in API
    @Column(name = "node_host")
    @JsonProperty("node_host")
    private String nodeHost; //??? not sure about data type of this API field

    @Column(name = "node_id")
    @JsonProperty("node_id")
    private Integer nodeId;

    @Column(name = "alt_targets_used")
    @JsonProperty("alt_targets_used")
    private Boolean altTargetsUsed;

    @Column(name = "user_permissions")
    @JsonProperty("user_permissions")
    private Integer userPermissions;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="policy_template_uuid_id")
    @JsonProperty("policy_template_uuid")
    private PolicyTemplateUuid policyTemplateUuid;

    @Column(name = "known_accounts")
    @JsonProperty("known_accounts")
    private Boolean knownAccounts;

    @JsonProperty("offline")
    private Boolean offline;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="status_id")
    @JsonProperty("status")
    private ScanStatus status;

    @Column(name = "severity_processed")
    @JsonProperty("severity_processed")
    private String severityProcessed;




    /**********************************
     * Special getters/setters for severity base fields
     * Getters first
     **********************************/

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @JoinTable(
            name = "scan_info_severity_base_selection",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "severity_base_id") }
    )
    @OrderColumn(name = "__order_for_scan_info", nullable = false)
    @JsonProperty("severity_base_selections")
    private List<SeverityBase> severityBaseSelections;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="current_severity_base_id")
    @Access(AccessType.PROPERTY)
    //@JsonProperty("current_severity_base")
    private SeverityBase currentSeverityBase;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="selected_severity_base_id")
    @Access(AccessType.PROPERTY)
    //@JsonProperty("selected_severity_base")
    private SeverityBase selectedSeverityBase;


    public List<SeverityBase> getSeverityBaseSelections() {
        return severityBaseSelections;
    }

    public SeverityBase getCurrentSeverityBase() {
        return currentSeverityBase;
    }

    public SeverityBase getSelectedSeverityBase() {
        return selectedSeverityBase;
    }



    //4-24-2022 WJA, substituting new ObjectLookup methods with InstancesTracker ... should make this code unneccessary
    @Transient
    @JsonGetter("current_severity_base")
    public String getCurrentSeverityBaseValue() {
        SeverityBase current = this.getCurrentSeverityBase();
        return current != null ? current.getValue() : null;
    }


    @Transient
    @JsonGetter("current_severity_base_display")
    String getCurrentSeverityBaseDisplay() {
        SeverityBase current = this.getCurrentSeverityBase();
        return current != null ? current.getDisplay() : null;
    }

    @Transient
    @JsonGetter("selected_severity_base")
    public String getSelectedSeverityBaseValue() {
        SeverityBase selected = this.getSelectedSeverityBase();
        return selected != null ? selected.getValue() : null;
    }


    /***********
     * Severity base setters
     ***********/

    @JsonIgnore
    public void setSelectedSeverityBase(SeverityBase selectedSeverityBase) {
        this.selectedSeverityBase = selectedSeverityBase;
    }

    public void setSeverityBaseSelections(List<SeverityBase> selections) {
        this.severityBaseSelections = selections;
    }

    @JsonIgnore
    public void setCurrentSeverityBase(SeverityBase currentSeverityBase) {
        this.currentSeverityBase = currentSeverityBase;
    }

    @Transient
    @JsonSetter("current_severity_base")
    public void setCurrentSeverityBaseValue(String value) {
        if (this.currentSeverityBase == null) {
            this.currentSeverityBase = new SeverityBase();
            this.currentSeverityBase.setValue(value);

        } else if (this.currentSeverityBase.getId() == 0) {
            this.currentSeverityBase.setValue(value);

        } else if (!Objects.equals(value, this.currentSeverityBase.getValue())) {
            SeverityBase copy = new SeverityBase();
            copy.setDisplay(this.currentSeverityBase.getDisplay());
            copy.setValue(value);
            this.currentSeverityBase = copy;
        }
    }

    @Transient
    @JsonSetter("current_severity_base_display")
    public void setCurrentSeverityBaseDisplay(String display) {
        if (this.currentSeverityBase == null) {
            this.currentSeverityBase = new SeverityBase();
            this.currentSeverityBase.setDisplay(display);

        } else if (this.currentSeverityBase.getId() == 0) {
            this.currentSeverityBase.setDisplay(display);

        } else if (!Objects.equals(display, this.currentSeverityBase.getDisplay())) {
            SeverityBase copy = new SeverityBase();
            copy.setValue(this.currentSeverityBase.getValue());
            copy.setDisplay(display);
            this.currentSeverityBase = copy;
        }
    }


    @Transient
    @JsonSetter("selected_severity_base")
    public void setSelectedSeverityBaseValue(String value) {
        if (this.selectedSeverityBase == null
                || !Objects.equals(value, this.selectedSeverityBase.getValue())) {

            this.selectedSeverityBase = new SeverityBase();
            this.selectedSeverityBase.setValue(value);
        }
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
        this.severityBaseSelections = SeverityBase.dao.getOrCreate(this.severityBaseSelections);
        this.currentSeverityBase = SeverityBase.dao.getOrCreate(this.currentSeverityBase);

        if (this.selectedSeverityBase == null || this.selectedSeverityBase.getId() != 0) return;

        String value = this.selectedSeverityBase.getValue();
        List<SeverityBase> selected
                = SeverityBase.dao.findByPropertyEqual("value", value);

        for (SeverityBase sb : selected) {
            if (sb != null && Objects.equals(sb.getValue(), value)) {
                this.selectedSeverityBase = sb;
                break;
            }
        }
        this.selectedSeverityBase = SeverityBase.dao.getOrCreate(this.selectedSeverityBase);
    }



    /******************************************
     * Standard getters/setters below
     *
     ******************************************/


    public Integer getFolder() {
        return folder;
    }

    public void setFolder(Integer folder) {
        this.folder = folder;
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

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public Boolean getOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public boolean isEditAllowed() {
        return editAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        this.editAllowed = editAllowed;
    }

    public List<Acl> getAcls() {
        return acls;
    }

    public void setAcls(List<Acl> acls) {
        this.acls = Acl.dao.getOrCreate(acls);
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

    public ScanTargets getTargets() {
        return targets;
    }

    public void setTargets(ScanTargets targets) {
        this.targets = targets;
    }

    public Timestamp getScannerStart() {
        return scannerStart;
    }

    public void setScannerStart(Timestamp scannerStart) {
        this.scannerStart = scannerStart;
    }

    public Timestamp getScannerEnd() {
        return scannerEnd;
    }

    public void setScannerEnd(Timestamp scannerEnd) {
        this.scannerEnd = scannerEnd;
    }

    public Boolean getOsesFound() {
        return osesFound;
    }

    public void setOsesFound(Boolean osesFound) {
        this.osesFound = osesFound;
    }

    public int getHostCount() {
        return hostCount;
    }

    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    public Boolean getHaskb() {
        return haskb;
    }

    public void setHaskb(Boolean haskb) {
        this.haskb = haskb;
    }

    public Boolean getExploitableVulns() {
        return exploitableVulns;
    }

    public void setExploitableVulns(Boolean exploitableVulns) {
        this.exploitableVulns = exploitableVulns;
    }

    public Boolean getHostsVulns() {
        return hostsVulns;
    }

    public void setHostsVulns(Boolean hostsVulns) {
        this.hostsVulns = hostsVulns;
    }

    public Integer getMigrated() {
        return migrated;
    }

    public void setMigrated(Integer migrated) {
        this.migrated = migrated;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ScanPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(ScanPolicy policy) {
        this.policy = policy;
    }

    public Boolean getYearOldVulns() {
        return yearOldVulns;
    }

    public void setYearOldVulns(Boolean yearOldVulns) {
        this.yearOldVulns = yearOldVulns;
    }

    public Boolean getTop10() {
        return top10;
    }

    public void setTop10(Boolean top10) {
        this.top10 = top10;
    }

    public Boolean getPciCanUpload() {
        return pciCanUpload;
    }

    public void setPciCanUpload(Boolean pciCanUpload) {
        this.pciCanUpload = pciCanUpload;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Timestamp getScanStart() {
        return scanStart;
    }

    public void setScanStart(Timestamp scanStart) {
        this.scanStart = scanStart;
    }

    public Timestamp getScanEnd() {
        return scanEnd;
    }

    public void setScanEnd(Timestamp scanEnd) {
        this.scanEnd = scanEnd;
    }

    public Boolean getHasAuditTrail() {
        return hasAuditTrail;
    }

    public void setHasAuditTrail(Boolean hasAuditTrail) {
        this.hasAuditTrail = hasAuditTrail;
    }

    public Boolean getControl() {
        return control;
    }

    public void setControl(Boolean control) {
        this.control = control;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public Boolean getUnsupportedSoftware() {
        return unsupportedSoftware;
    }

    public void setUnsupportedSoftware(Boolean unsupportedSoftware) {
        this.unsupportedSoftware = unsupportedSoftware;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public License getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(License licenseInfo) {
        this.licenseInfo = License.dao.getOrCreate(licenseInfo);
    }

    public Boolean getNoTarget() {
        return noTarget;
    }

    public void setNoTarget(Boolean noTarget) {
        this.noTarget = noTarget;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public String getSeverityProcessed() {
        return severityProcessed;
    }

    public void setSeverityProcessed(String severityProcessed) {
        this.severityProcessed = severityProcessed;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Boolean getAltTargetsUsed() {
        return altTargetsUsed;
    }

    public void setAltTargetsUsed(Boolean altTargetsUsed) {
        this.altTargetsUsed = altTargetsUsed;
    }

    public Integer getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Integer userPermissions) {
        this.userPermissions = userPermissions;
    }

    public PolicyTemplateUuid getPolicyTemplateUuid() {
        return policyTemplateUuid;
    }

    public void setPolicyTemplateUuid(PolicyTemplateUuid policyTemplateUuid) {
        this.policyTemplateUuid = policyTemplateUuid;
    }

    public Boolean getKnownAccounts() {
        return knownAccounts;
    }

    public void setKnownAccounts(Boolean knownAccounts) {
        this.knownAccounts = knownAccounts;
    }

}
