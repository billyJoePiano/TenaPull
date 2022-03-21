package nessusTools.data.entity;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.data.deserialize.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.apache.logging.log4j.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.*;


@Entity(name = "ScanInfo")
@Table(name = "scan_info")
@JsonIgnoreProperties({"id"})
public class ScanInfo extends NaturalIdPojo {
    public static final Dao<ScanInfo> dao
            = new Dao<ScanInfo>(ScanInfo.class);

    public static final Logger logger = LogManager.getLogger(ScanInfo.class);


    @OneToOne
    @JoinColumn(name = "id")
    @JsonIgnore
    private Scan scan;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="folder_id")
    @JsonProperty("folder_id")
    @JsonDeserialize(using = IdReference.Deserializer.class)
    @JsonSerialize(using = IdReference.Serializer.class)
    private Folder folder;

    private String name;

    private String uuid;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_type_id")
    @JsonProperty("scan_type")
    private ScanType scanType;

    @Column(name = "edit_allowed")
    @JsonProperty("edit_allowed")
    private boolean editAllowed;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_info_acl",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "acl_id") }
    )
    @OrderColumn(name = "__order_for_scan_info", nullable = false)
    @JsonDeserialize(contentAs = Acl.class, contentUsing = ObjectLookup.Deserializer.class)
    private List<Acl> acls;


    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_group_id")
    @JsonProperty("scan_group")
    @JsonDeserialize(using = IdReference.Deserializer.class)
    @JsonSerialize(using = IdReference.Serializer.class)
    private ScanGroup scanGroup;

    private String targets;

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
    @JsonProperty("license_info")
    @JsonDeserialize(using = ObjectLookup.Deserializer.class)
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

    @Column(name = "policy_template_uuid")
    @JsonProperty("policy_template_uuid")
    private String policyTemplateUuid;

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
    @JoinTable(
            name = "scan_info_severity_base_selection",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "severity_base_id") }
    )
    @OrderColumn(name = "__order_for_scan_info", nullable = false)
    @JsonProperty("severity_base_selections")
    @JsonDeserialize(contentAs = SeverityBase.class, contentUsing = ObjectLookup.Deserializer.class)
    private List<SeverityBase> severityBaseSelections;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="current_severity_base_id")
    @JsonIgnore // see get/setCurrentSeverityBaseValue() and get/setCurrentSeverityBaseDisplay()
    private SeverityBase currentSeverityBase;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="selected_severity_base_id")
    @JsonIgnore
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

    public void setSelectedSeverityBase(SeverityBase selectedSeverityBase) {
        this.selectedSeverityBase = checkForSeverityBase(selectedSeverityBase);
    }

    public void setSeverityBaseSelections(List<SeverityBase> selections) {
        List<SeverityBase> checked = new ArrayList(selections.size());
        for (SeverityBase selection : selections) {
            checked.add(checkForSeverityBase(selection));
        }
        this.severityBaseSelections = checked;
    }

    public void setCurrentSeverityBase(SeverityBase currentSeverityBase) {
        this.currentSeverityBase = this.checkForSeverityBase(currentSeverityBase);
    }

    @Transient
    @JsonSetter("current_severity_base")
    public void setCurrentSeverityBaseValue(String value) {
        SeverityBase current = this.getCurrentSeverityBase();
        boolean isNew;
        if (isNew = current == null) {
            current = new SeverityBase();
        }

        current.setValue(value);
        this.currentSeverityBase = checkForSeverityBase(current, isNew);
    }

    @Transient
    @JsonSetter("current_severity_base_display")
    public void setCurrentSeverityBaseDisplay(String display) {
        SeverityBase current = this.getCurrentSeverityBase();
        boolean isNew;
        if (isNew = current == null) {
            current = new SeverityBase();
        }

        current.setDisplay(display);
        this.currentSeverityBase = checkForSeverityBase(current, isNew);
    }

    @Transient
    @JsonSetter("selected_severity_base")
    public void setSelectedSeverityBaseValue(String value) {
        SeverityBase selected = new SeverityBase();
        selected.setValue(value);
        this.selectedSeverityBase = checkForSeverityBase(selected, true, true);
    }


    /*
     * Hibernate was throwing exceptions about "non-unique object" at insert, if it wasn't using the same object
     * to represent the same record, when it appeared multiple time.  Therefore, we need to check against every
     * instance of SeverityBase already in this ScanInfo instance before inserting the ScanInfo instance.
     *
     * See Also:
     * https://stackoverflow.com/questions/1074081/hibernate-error-org-hibernate-nonuniqueobjectexception-a-different-object-with
     * https://stackoverflow.com/questions/16246675/hibernate-error-a-different-object-with-the-same-identifier-value-was-already-a
     *
     */
    private SeverityBase checkForSeverityBase(SeverityBase checkAgainst) {
        return checkForSeverityBase(checkAgainst, false, false);
    }

    private SeverityBase checkForSeverityBase(SeverityBase checkAgainst, boolean skipInsert) {
        return checkForSeverityBase(checkAgainst, skipInsert, false);
    }

    private SeverityBase checkForSeverityBase(SeverityBase checkAgainst,
                                              boolean skipInsert,
                                              boolean matchOnValueOnly) {

        if (checkAgainst == null) return null;

        List<SeverityBase> all = this.getSeverityBaseSelections();
        if (all != null) {
            all = new ArrayList(all);
        } else {
            all = new ArrayList();
        }

        SeverityBase current = this.getCurrentSeverityBase();
        if (current != null) {
            all.add(current);
        }

        // Only value is set for selected_current_severity,
        // so we need to check if it is a new instance and set the display if neccessary
        SeverityBase selected = this.getSelectedSeverityBase();
        if (selected != null) {
            if (        checkAgainst != selected
                    &&  selected.getId() == 0
                    &&  selected.getDisplay() == null
                    &&  checkAgainst.getValue() != null
                    &&  checkAgainst.getDisplay() != null
                    &&  Objects.equals(
                                selected.getValue(),
                                checkAgainst.getValue())
                ) {

                selected.setDisplay(checkAgainst.getDisplay());
                return selected;

            } else {
                all.add(selected);
            }
        }

        for (SeverityBase other : all) {
            if (other == checkAgainst) continue;
            if (Objects.equals(other.getValue(), checkAgainst.getValue())
                    && (matchOnValueOnly
                        || Objects.equals(other.getDisplay(), checkAgainst.getDisplay()))
                ) {

                return other;
            }
        }

        SeverityBase persisted = SeverityBase.dao.findByExactPojo(checkAgainst);

        if (persisted != null) {
            return persisted;

        } else if (!skipInsert) {
            SeverityBase.dao.insert(checkAgainst);
        }

        return checkAgainst;
    }





    /******************************************
     * Standard getters/setters below
     *
     ******************************************/

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }


    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
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
        this.acls = acls;
    }


    public ScanGroup getScanGroup() {
        return scanGroup;
    }

    public void setScanGroup(ScanGroup scanGroup) {
        this.scanGroup = scanGroup;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
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
        this.licenseInfo = licenseInfo;
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

    public String getPolicyTemplateUuid() {
        return policyTemplateUuid;
    }

    public void setPolicyTemplateUuid(String policyTemplateUuid) {
        this.policyTemplateUuid = policyTemplateUuid;
    }

    public Boolean getKnownAccounts() {
        return knownAccounts;
    }

    public void setKnownAccounts(Boolean knownAccounts) {
        this.knownAccounts = knownAccounts;
    }

}
