package nessusData.entity;

import com.fasterxml.jackson.annotation.*;
import java.util.*;
import nessusData.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanInfo")
@Table(name = "scan_info")
public class ScanInfo implements Pojo {
	public static final Dao<ScanInfo> dao = new Dao<ScanInfo>(ScanInfo.class);

	@Id
	private int id;
/*
	@JsonProperty("alt_targets_used")
	private boolean altTargetsUsed;

	@JsonProperty("user_permissions")
	private int userPermissions;

	@JsonProperty("policy_template_uuid")
	private String policyTemplateUuid;

	@JsonProperty("acls")
	private List<AclsItem> acls;

	@JsonProperty("known_accounts")
	private boolean knownAccounts;

	@JsonProperty("scan_type")
	private String scanType;

	@JsonProperty("current_severity_base")
	private String currentSeverityBase;

	@JsonProperty("scan_group")
	private Object scanGroup;

	@JsonProperty("targets")
	private String targets;

	@JsonProperty("uuid")
	private String uuid;

	@JsonProperty("scanner_start")
	private int scannerStart;

	@JsonProperty("offline")
	private boolean offline;

	@JsonProperty("selected_severity_base")
	private String selectedSeverityBase;

	@JsonProperty("oses_found")
	private boolean osesFound;

	@JsonProperty("hostcount")
	private int hostcount;

	@JsonProperty("scanner_end")
	private int scannerEnd;

	@JsonProperty("haskb")
	private boolean haskb;

	@JsonProperty("exploitable_vulns")
	private boolean exploitableVulns;

	@JsonProperty("hosts_vulns")
	private boolean hostsVulns;

	@JsonProperty("migrated")
	private int migrated;

	@JsonProperty("timestamp")
	private int timestamp;

	@JsonProperty("policy")
	private String policy;

	@JsonProperty("year_old_vulns")
	private boolean yearOldVulns;

	@JsonProperty("top10")
	private boolean top10;

	@JsonProperty("pci-can-upload")
	private boolean pciCanUpload;

	@JsonProperty("node_name")
	private Object nodeName;

	@JsonProperty("scan_end")
	private int scanEnd;

	@JsonProperty("hasaudittrail")
	private boolean hasaudittrail;

	@JsonProperty("control")
	private boolean control;

	@JsonProperty("scanner_name")
	private String scannerName;

	@JsonProperty("unsupported_software")
	private boolean unsupportedSoftware;

	@JsonProperty("current_severity_base_display")
	private String currentSeverityBaseDisplay;

	@JsonProperty("object_id")
	private int objectId;

	@JsonProperty("license_info")
	private LicenseInfo licenseInfo;

	@JsonProperty("no_target")
	private boolean noTarget;

	@JsonProperty("name")
	private String name;

	@JsonProperty("edit_allowed")
	private boolean editAllowed;

	@JsonProperty("severity_base_selections")
	private List<SeverityBaseSelectionsItem> severityBaseSelections;

	@JsonProperty("node_host")
	private Object nodeHost;

	@JsonProperty("scan_start")
	private int scanStart;

	@JsonProperty("folder_id")
	private int folderId;

	@JsonProperty("severity_processed")
	private String severityProcessed;

	@JsonProperty("status")
	private String status;

	@JsonProperty("node_id")
	private Object nodeId;
*/

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
/*
	public boolean isAltTargetsUsed(){
		return altTargetsUsed;
	}

	public int getUserPermissions(){
		return userPermissions;
	}

	public String getPolicyTemplateUuid(){
		return policyTemplateUuid;
	}

	public List<AclsItem> getAcls(){
		return acls;
	}

	public boolean isKnownAccounts(){
		return knownAccounts;
	}

	public String getScanType(){
		return scanType;
	}

	public String getCurrentSeverityBase(){
		return currentSeverityBase;
	}

	public Object getScanGroup(){
		return scanGroup;
	}

	public String getTargets(){
		return targets;
	}

	public String getUuid(){
		return uuid;
	}

	public int getScannerStart(){
		return scannerStart;
	}

	public boolean isOffline(){
		return offline;
	}

	public String getSelectedSeverityBase(){
		return selectedSeverityBase;
	}

	public boolean isOsesFound(){
		return osesFound;
	}

	public int getHostcount(){
		return hostcount;
	}

	public int getScannerEnd(){
		return scannerEnd;
	}

	public boolean isHaskb(){
		return haskb;
	}

	public boolean isExploitableVulns(){
		return exploitableVulns;
	}

	public boolean isHostsVulns(){
		return hostsVulns;
	}

	public int getMigrated(){
		return migrated;
	}

	public int getTimestamp(){
		return timestamp;
	}

	public String getPolicy(){
		return policy;
	}

	public boolean isYearOldVulns(){
		return yearOldVulns;
	}

	public boolean isTop10(){
		return top10;
	}

	public boolean isPciCanUpload(){
		return pciCanUpload;
	}

	public Object getNodeName(){
		return nodeName;
	}

	public int getScanEnd(){
		return scanEnd;
	}

	public boolean isHasaudittrail(){
		return hasaudittrail;
	}

	public boolean isControl(){
		return control;
	}

	public String getScannerName(){
		return scannerName;
	}

	public boolean isUnsupportedSoftware(){
		return unsupportedSoftware;
	}

	public String getCurrentSeverityBaseDisplay(){
		return currentSeverityBaseDisplay;
	}

	public int getObjectId(){
		return objectId;
	}

	public LicenseInfo getLicenseInfo(){
		return licenseInfo;
	}

	public boolean isNoTarget(){
		return noTarget;
	}

	public String getName(){
		return name;
	}

	public boolean isEditAllowed(){
		return editAllowed;
	}

	public List<SeverityBaseSelectionsItem> getSeverityBaseSelections(){
		return severityBaseSelections;
	}

	public Object getNodeHost(){
		return nodeHost;
	}

	public int getScanStart(){
		return scanStart;
	}

	public int getFolderId(){
		return folderId;
	}

	public String getSeverityProcessed(){
		return severityProcessed;
	}

	public String getStatus(){
		return status;
	}

	public Object getNodeId(){
		return nodeId;
	}
*/

}
