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
 * Represents the info object returned by the Nessus API in /scans/&lt;scan-id&gt;
 */
@Entity(name = "ScanInfo")
@Table(name = "scan_info")
@JsonIgnoreProperties({"id"})
public class ScanInfo extends ScanResponse.SingleChild<ScanInfo> {

    /**
     * The dao for ScanInfo
     */
    public static final Dao<ScanInfo> dao
            = new Dao<ScanInfo>(ScanInfo.class);

    /**
     * The logger for ScanInfo
     */
    public static final Logger logger = LogManager.getLogger(ScanInfo.class);

    @Column(name = "folder_id")
    @JsonProperty("folder_id")
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


    /**
     * Gets severity base selections.
     *
     * @return the severity base selections
     */
    public List<SeverityBase> getSeverityBaseSelections() {
        return severityBaseSelections;
    }

    /**
     * Gets current severity base.
     *
     * @return the current severity base
     */
    public SeverityBase getCurrentSeverityBase() {
        return currentSeverityBase;
    }

    /**
     * Gets selected severity base.
     *
     * @return the selected severity base
     */
    public SeverityBase getSelectedSeverityBase() {
        return selectedSeverityBase;
    }


    /**
     * Gets current severity base value.
     *
     * @return the current severity base value
     */
//4-24-2022 WJA, substituting new ObjectLookup methods with InstancesTracker ... should make this code unneccessary
    @Transient
    @JsonGetter("current_severity_base")
    public String getCurrentSeverityBaseValue() {
        SeverityBase current = this.getCurrentSeverityBase();
        return current != null ? current.getValue() : null;
    }


    /**
     * Gets current severity base display.
     *
     * @return the current severity base display
     */
    @Transient
    @JsonGetter("current_severity_base_display")
    String getCurrentSeverityBaseDisplay() {
        SeverityBase current = this.getCurrentSeverityBase();
        return current != null ? current.getDisplay() : null;
    }

    /**
     * Gets selected severity base value.
     *
     * @return the selected severity base value
     */
    @Transient
    @JsonGetter("selected_severity_base")
    public String getSelectedSeverityBaseValue() {
        SeverityBase selected = this.getSelectedSeverityBase();
        return selected != null ? selected.getValue() : null;
    }


    /***********
     * Severity base setters
     * @param selectedSeverityBase the selected severity base
     */
    @JsonIgnore
    public void setSelectedSeverityBase(SeverityBase selectedSeverityBase) {
        this.selectedSeverityBase = selectedSeverityBase;
    }

    /**
     * Sets severity base selections.
     *
     * @param selections the selections
     */
    public void setSeverityBaseSelections(List<SeverityBase> selections) {
        this.severityBaseSelections = selections;
    }

    /**
     * Sets current severity base.
     *
     * @param currentSeverityBase the current severity base
     */
    @JsonIgnore
    public void setCurrentSeverityBase(SeverityBase currentSeverityBase) {
        this.currentSeverityBase = currentSeverityBase;
    }

    /**
     * Sets current severity base value.
     *
     * @param value the value
     */
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

    /**
     * Sets current severity base display.
     *
     * @param display the display
     */
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


    /**
     * Sets selected severity base value.
     *
     * @param value the value
     */
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

        this.licenseInfo = License.dao.getOrCreate(this.licenseInfo);

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
     * @return the folder
     */
    public Integer getFolder() {
        return folder;
    }

    /**
     * Sets folder.
     *
     * @param folder the folder
     */
    public void setFolder(Integer folder) {
        this.folder = folder;
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

    /**
     * Gets scan type.
     *
     * @return the scan type
     */
    public ScanType getScanType() {
        return scanType;
    }

    /**
     * Sets scan type.
     *
     * @param scanType the scan type
     */
    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    /**
     * Gets offline.
     *
     * @return the offline
     */
    public Boolean getOffline() {
        return offline;
    }

    /**
     * Sets offline.
     *
     * @param offline the offline
     */
    public void setOffline(Boolean offline) {
        this.offline = offline;
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
     * Is edit allowed boolean.
     *
     * @return the boolean
     */
    public boolean isEditAllowed() {
        return editAllowed;
    }

    /**
     * Sets edit allowed.
     *
     * @param editAllowed the edit allowed
     */
    public void setEditAllowed(boolean editAllowed) {
        this.editAllowed = editAllowed;
    }

    /**
     * Gets acls.
     *
     * @return the acls
     */
    public List<Acl> getAcls() {
        return acls;
    }

    /**
     * Sets acls.
     *
     * @param acls the acls
     */
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
     * Gets targets.
     *
     * @return the targets
     */
    public ScanTargets getTargets() {
        return targets;
    }

    /**
     * Sets targets.
     *
     * @param targets the targets
     */
    public void setTargets(ScanTargets targets) {
        this.targets = targets;
    }

    /**
     * Gets scanner start.
     *
     * @return the scanner start
     */
    public Timestamp getScannerStart() {
        return scannerStart;
    }

    /**
     * Sets scanner start.
     *
     * @param scannerStart the scanner start
     */
    public void setScannerStart(Timestamp scannerStart) {
        this.scannerStart = scannerStart;
    }

    /**
     * Gets scanner end.
     *
     * @return the scanner end
     */
    public Timestamp getScannerEnd() {
        return scannerEnd;
    }

    /**
     * Sets scanner end.
     *
     * @param scannerEnd the scanner end
     */
    public void setScannerEnd(Timestamp scannerEnd) {
        this.scannerEnd = scannerEnd;
    }

    /**
     * Gets oses found.
     *
     * @return the oses found
     */
    public Boolean getOsesFound() {
        return osesFound;
    }

    /**
     * Sets oses found.
     *
     * @param osesFound the oses found
     */
    public void setOsesFound(Boolean osesFound) {
        this.osesFound = osesFound;
    }

    /**
     * Gets host count.
     *
     * @return the host count
     */
    public int getHostCount() {
        return hostCount;
    }

    /**
     * Sets host count.
     *
     * @param hostCount the host count
     */
    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    /**
     * Gets haskb.
     *
     * @return the haskb
     */
    public Boolean getHaskb() {
        return haskb;
    }

    /**
     * Sets haskb.
     *
     * @param haskb the haskb
     */
    public void setHaskb(Boolean haskb) {
        this.haskb = haskb;
    }

    /**
     * Gets exploitable vulns.
     *
     * @return the exploitable vulns
     */
    public Boolean getExploitableVulns() {
        return exploitableVulns;
    }

    /**
     * Sets exploitable vulns.
     *
     * @param exploitableVulns the exploitable vulns
     */
    public void setExploitableVulns(Boolean exploitableVulns) {
        this.exploitableVulns = exploitableVulns;
    }

    /**
     * Gets hosts vulns.
     *
     * @return the hosts vulns
     */
    public Boolean getHostsVulns() {
        return hostsVulns;
    }

    /**
     * Sets hosts vulns.
     *
     * @param hostsVulns the hosts vulns
     */
    public void setHostsVulns(Boolean hostsVulns) {
        this.hostsVulns = hostsVulns;
    }

    /**
     * Gets migrated.
     *
     * @return the migrated
     */
    public Integer getMigrated() {
        return migrated;
    }

    /**
     * Sets migrated.
     *
     * @param migrated the migrated
     */
    public void setMigrated(Integer migrated) {
        this.migrated = migrated;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets policy.
     *
     * @return the policy
     */
    public ScanPolicy getPolicy() {
        return policy;
    }

    /**
     * Sets policy.
     *
     * @param policy the policy
     */
    public void setPolicy(ScanPolicy policy) {
        this.policy = policy;
    }

    /**
     * Gets year old vulns.
     *
     * @return the year old vulns
     */
    public Boolean getYearOldVulns() {
        return yearOldVulns;
    }

    /**
     * Sets year old vulns.
     *
     * @param yearOldVulns the year old vulns
     */
    public void setYearOldVulns(Boolean yearOldVulns) {
        this.yearOldVulns = yearOldVulns;
    }

    /**
     * Gets top 10.
     *
     * @return the top 10
     */
    public Boolean getTop10() {
        return top10;
    }

    /**
     * Sets top 10.
     *
     * @param top10 the top 10
     */
    public void setTop10(Boolean top10) {
        this.top10 = top10;
    }

    /**
     * Gets pci can upload.
     *
     * @return the pci can upload
     */
    public Boolean getPciCanUpload() {
        return pciCanUpload;
    }

    /**
     * Sets pci can upload.
     *
     * @param pciCanUpload the pci can upload
     */
    public void setPciCanUpload(Boolean pciCanUpload) {
        this.pciCanUpload = pciCanUpload;
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
     * Gets scan start.
     *
     * @return the scan start
     */
    public Timestamp getScanStart() {
        return scanStart;
    }

    /**
     * Sets scan start.
     *
     * @param scanStart the scan start
     */
    public void setScanStart(Timestamp scanStart) {
        this.scanStart = scanStart;
    }

    /**
     * Gets scan end.
     *
     * @return the scan end
     */
    public Timestamp getScanEnd() {
        return scanEnd;
    }

    /**
     * Sets scan end.
     *
     * @param scanEnd the scan end
     */
    public void setScanEnd(Timestamp scanEnd) {
        this.scanEnd = scanEnd;
    }

    /**
     * Gets has audit trail.
     *
     * @return the has audit trail
     */
    public Boolean getHasAuditTrail() {
        return hasAuditTrail;
    }

    /**
     * Sets has audit trail.
     *
     * @param hasAuditTrail the has audit trail
     */
    public void setHasAuditTrail(Boolean hasAuditTrail) {
        this.hasAuditTrail = hasAuditTrail;
    }

    /**
     * Gets control.
     *
     * @return the control
     */
    public Boolean getControl() {
        return control;
    }

    /**
     * Sets control.
     *
     * @param control the control
     */
    public void setControl(Boolean control) {
        this.control = control;
    }

    /**
     * Gets scanner.
     *
     * @return the scanner
     */
    public Scanner getScanner() {
        return scanner;
    }

    /**
     * Sets scanner.
     *
     * @param scanner the scanner
     */
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Gets unsupported software.
     *
     * @return the unsupported software
     */
    public Boolean getUnsupportedSoftware() {
        return unsupportedSoftware;
    }

    /**
     * Sets unsupported software.
     *
     * @param unsupportedSoftware the unsupported software
     */
    public void setUnsupportedSoftware(Boolean unsupportedSoftware) {
        this.unsupportedSoftware = unsupportedSoftware;
    }

    /**
     * Gets object id.
     *
     * @return the object id
     */
    public Integer getObjectId() {
        return objectId;
    }

    /**
     * Sets object id.
     *
     * @param objectId the object id
     */
    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    /**
     * Gets license info.
     *
     * @return the license info
     */
    public License getLicenseInfo() {
        return licenseInfo;
    }

    /**
     * Sets license info.
     *
     * @param licenseInfo the license info
     */
    public void setLicenseInfo(License licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    /**
     * Gets no target.
     *
     * @return the no target
     */
    public Boolean getNoTarget() {
        return noTarget;
    }

    /**
     * Sets no target.
     *
     * @param noTarget the no target
     */
    public void setNoTarget(Boolean noTarget) {
        this.noTarget = noTarget;
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
     * Gets severity processed.
     *
     * @return the severity processed
     */
    public String getSeverityProcessed() {
        return severityProcessed;
    }

    /**
     * Sets severity processed.
     *
     * @param severityProcessed the severity processed
     */
    public void setSeverityProcessed(String severityProcessed) {
        this.severityProcessed = severityProcessed;
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
     * Gets policy template uuid.
     *
     * @return the policy template uuid
     */
    public PolicyTemplateUuid getPolicyTemplateUuid() {
        return policyTemplateUuid;
    }

    /**
     * Sets policy template uuid.
     *
     * @param policyTemplateUuid the policy template uuid
     */
    public void setPolicyTemplateUuid(PolicyTemplateUuid policyTemplateUuid) {
        this.policyTemplateUuid = policyTemplateUuid;
    }

    /**
     * Gets known accounts.
     *
     * @return the known accounts
     */
    public Boolean getKnownAccounts() {
        return knownAccounts;
    }

    /**
     * Sets known accounts.
     *
     * @param knownAccounts the known accounts
     */
    public void setKnownAccounts(Boolean knownAccounts) {
        this.knownAccounts = knownAccounts;
    }

}
