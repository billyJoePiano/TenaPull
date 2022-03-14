package nessusData.entity;

import com.fasterxml.jackson.annotation.*;

import java.sql.Timestamp;
import java.util.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nessusData.entity.template.*;
import nessusData.persistence.*;
import nessusData.serialize.*;

import javax.persistence.*;

@Entity(name = "ScanInfo")
@Table(name = "scan_info")
public class ScanInfo extends NaturalIdPojo {
	public static final Dao<ScanInfo> dao
			= new Dao<ScanInfo>(ScanInfo.class);

	@OneToOne
	@JoinColumn(name = "id")
	@JsonIgnore
	private ScanInfo scanInfo;

	private String name;

	private String uuid;

	@Column(name = "edit_allowed")
	@JsonProperty("edit_allowed")
	private boolean editAllowed;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
			name = "scan_info_acl",
			joinColumns = { @JoinColumn(name = "scan_id") },
			inverseJoinColumns = { @JoinColumn(name = "acl_id") }
	)
	@JsonProperty("acls")
	private List<Acl> acls;

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	@JoinColumn(name="current_severity_base_id")
	@JsonProperty("current_severity_base")
	@JsonDeserialize(using = IdReference.Deserializer.class)
	@JsonSerialize(using = IdReference.Serializer.class)
	private SeverityBase currentSeverityBase;

	@Transient
	@JsonProperty("current_severity_base_display")
	String getCurrentSeverityBaseDisplay() {
		return this.getCurrentSeverityBase().getDisplay();
	}
	// TODO handle deserialization/persistence for current_severity_base_display

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

	@JsonProperty("selected_severity_base")
	private String selectedSeverityBase;

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

	private Timestamp timestamp;

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	@JoinColumn(name="policy_id")
	@JsonDeserialize(using = Lookup.Deserializer.class)
	@JsonSerialize(using = Lookup.Serializer.class)
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
	@JsonDeserialize(using = Lookup.Deserializer.class)
	@JsonSerialize(using = Lookup.Serializer.class)
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
	private License licenseInfo;

	@Column(name = "no_target")
	@JsonProperty("no_target")
	private Boolean noTarget;

	// TODO determine data type in API
	@Column(name = "node_host")
	@JsonProperty("node_host")
	private String nodeHost; //??? not sure about data type of this API field

	@Column(name = "severity_processed")
	@JsonProperty("severity_processed")
	private String severityProcessed;

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

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	@JoinColumn(name="type_id")
	@JsonDeserialize(using = Lookup.Deserializer.class)
	@JsonSerialize(using = Lookup.Serializer.class)
	private ScanType scanType;


	@JsonProperty("offline")
	private Boolean offline;


	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
			name = "scan_info_severity_base_selection",
			joinColumns = { @JoinColumn(name = "scan_id") },
			inverseJoinColumns = { @JoinColumn(name = "severity_base_selection_id") }
	)
	@JsonProperty("severity_base_selections")
	private List<SeverityBase> severityBaseSelections;



	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	@JoinColumn(name="status_id")
	@JsonProperty("status")
	@JsonDeserialize(using = Lookup.Deserializer.class)
	@JsonSerialize(using = Lookup.Serializer.class)
	private ScanStatus status;






	/******************************************
	 * Standard getters/setters below
	 *
	 ******************************************/



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

	public List<Acl> getAcls() {
		return acls;
	}

	public void setAcls(List<Acl> acls) {
		this.acls = acls;
	}

	public Boolean getKnownAccounts() {
		return knownAccounts;
	}

	public void setKnownAccounts(Boolean knownAccounts) {
		this.knownAccounts = knownAccounts;
	}

	public ScanType getScanType() {
		return scanType;
	}

	public void setScanType(ScanType scanType) {
		this.scanType = scanType;
	}

	public SeverityBase getCurrentSeverityBase() {
		return currentSeverityBase;
	}

	public void setCurrentSeverityBase(SeverityBase currentSeverityBase) {
		this.currentSeverityBase = currentSeverityBase;
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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	public Boolean getOffline() {
		return offline;
	}

	public void setOffline(Boolean offline) {
		this.offline = offline;
	}

	public String getSelectedSeverityBase() {
		return selectedSeverityBase;
	}

	public void setSelectedSeverityBase(String selectedSeverityBase) {
		this.selectedSeverityBase = selectedSeverityBase;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEditAllowed() {
		return editAllowed;
	}

	public void setEditAllowed(boolean editAllowed) {
		this.editAllowed = editAllowed;
	}

	public List<SeverityBase> getSeverityBaseSelections() {
		return severityBaseSelections;
	}

	public void setSeverityBaseSelections(List<SeverityBase> severityBaseSelections) {
		this.severityBaseSelections = severityBaseSelections;
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

	public ScanStatus getStatus() {
		return status;
	}

	public void setStatus(ScanStatus status) {
		this.status = status;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
}
