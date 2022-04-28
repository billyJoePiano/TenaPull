package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import java.util.*;



import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanHost")
@Table(name = "scan_host")
public class ScanHost extends ScanResponse.MultiChildLookup<ScanHost>
        implements LookupSearchMapProvider<ScanHost> {

    public static final ObjectLookupDao<ScanHost>
            dao = new ObjectLookupDao<ScanHost>(ScanHost.class);

    @Column(name = "host_id")
    @JsonProperty("host_id")
    private Integer hostId;

    @Column(name = "total_checks_considered")
    @JsonProperty("totalchecksconsidered")
    private Integer totalChecksConsidered;

    @Column(name = "num_checks_considered")
    @JsonProperty("numchecksconsidered")
    private Integer numChecksConsidered;

    @Column(name = "scan_progress_total")
    @JsonProperty("scanprogresstotal")
    private Integer scanProgressTotal;

    @Column(name = "scan_progress_current")
    @JsonProperty("scanprogresscurrent")
    private Integer scanProgressCurrent;

    @Column(name = "host_index")
    @JsonProperty("host_index")
    private Integer hostIndex;

    private Integer score;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @JoinTable(
            name = "scan_host_severity_level_count",
            joinColumns = { @JoinColumn(name = "scan_host_id") },
            inverseJoinColumns = { @JoinColumn(name = "severity_id") }
    )
    //@OrderBy("severityLevel ASC")
    @JsonIgnore
    private List<SeverityLevelCount> severityCounts;

    @Transient
    private SeverityCount severitycount;

    private String progress;

    @Column(name = "offline_critical")
    @JsonProperty("offline_critical")
    private Integer offlineCritical;

    @Column(name = "offline_high")
    @JsonProperty("offline_high")
    private Integer offlineHigh;

    @Column(name = "offline_medium")
    @JsonProperty("offline_medium")
    private Integer offlineMedium;

    @Column(name = "offline_low")
    @JsonProperty("offline_low")
    private Integer offlineLow;

    @Column(name = "offline_info")
    @JsonProperty("offline_info")
    private Integer offlineInfo;

    private Integer critical;

    private Integer high;

    private Integer medium;

    private Integer low;

    private Integer info;

    private Integer severity;

    private String hostname;

    public SeverityCount getSeveritycount() {
        if (this.severitycount == null) {
            this.severitycount = new SeverityCount();
            this.severitycount.takeFieldsFromParent(this);
        }
        return this.severitycount;
    }

    public void setSeveritycount(SeverityCount severitycount) {
        if (this.severitycount != null && this.severitycount != severitycount) {
            this.severitycount.clearParent();
        }
        this.severitycount = severitycount;
        severitycount.putFieldsIntoParent(this);
    }

    public List<SeverityLevelCount> getSeverityCounts() {
        return this.severityCounts;
    }

    public void setSeverityCounts(List<SeverityLevelCount> severityCounts) {
        if (this.severityCounts == severityCounts) return;
        this.severityCounts = severityCounts;
        if (this.severitycount != null) this.severitycount.setItem(this.severityCounts);
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.severityCounts = SeverityLevelCount.dao.getOrCreate(this.severityCounts);
    }

    @Override
    @Transient
    @JsonIgnore
    public void _set(ScanHost o) {
        this.__set(o);
        this.hostId = o.hostId;
        this.totalChecksConsidered = o.totalChecksConsidered;
        this.numChecksConsidered = o.numChecksConsidered;
        this.scanProgressTotal = o.scanProgressTotal;
        this.scanProgressCurrent = o.scanProgressCurrent;
        this.hostIndex = o.hostIndex;
        this.score = o.score;
        this.severityCounts = o.severityCounts;
        this.progress = o.progress;
        this.offlineCritical = o.offlineCritical;
        this.offlineHigh = o.offlineHigh;
        this.offlineMedium = o.offlineMedium;
        this.offlineLow = o.offlineLow;
        this.offlineInfo = o.offlineInfo;
        this.critical = o.critical;
        this.high = o.high;
        this.medium = o.medium;
        this.low = o.low;
        this.info = o.info;
        this.severity = o.severity;
        this.hostname = o.hostname;

        if (this.severitycount != null) {
            this.severitycount.clearParent();
            this.severitycount = null;
        }
    }

    @Override
    @Transient
    @JsonIgnore
    public boolean _match(ScanHost other) {
        return  this.__match(other)
                && this.hostId != null && other.hostId != null
                && this.hostId.intValue() == other.hostId.intValue();
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap() {
        return Map.of("response", this.getResponse(), "hostId", this.hostId);
    }

    @Override
    @JsonAnyGetter
    public Map<String, JsonNode> getExtraJsonMap() {
        return this.getSeveritycount().jsonAnyGetterForParent();
    }

    @Override
    @JsonAnySetter
    public void putExtraJson(String key, Object value) {
        super.putExtraJson(key, value);
        if (this.severitycount != null) this.severitycount.checkExtraJsonPut(key, value);
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public Integer getTotalChecksConsidered() {
        return totalChecksConsidered;
    }

    public void setTotalChecksConsidered(Integer totalChecksConsidered) {
        this.totalChecksConsidered = totalChecksConsidered;
    }

    public Integer getNumChecksConsidered() {
        return numChecksConsidered;
    }

    public void setNumChecksConsidered(Integer numChecksConsidered) {
        this.numChecksConsidered = numChecksConsidered;
    }

    public Integer getScanProgressTotal() {
        return scanProgressTotal;
    }

    public void setScanProgressTotal(Integer scanProgressTotal) {
        this.scanProgressTotal = scanProgressTotal;
    }

    public Integer getScanProgressCurrent() {
        return scanProgressCurrent;
    }

    public void setScanProgressCurrent(Integer scanProgressCurrent) {
        this.scanProgressCurrent = scanProgressCurrent;
    }

    public Integer getHostIndex() {
        return hostIndex;
    }

    public void setHostIndex(Integer hostIndex) {
        this.hostIndex = hostIndex;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public Integer getOfflineCritical() {
        return offlineCritical;
    }

    public void setOfflineCritical(Integer offlineCritical) {
        this.offlineCritical = offlineCritical;
    }

    public Integer getOfflineHigh() {
        return offlineHigh;
    }

    public void setOfflineHigh(Integer offlineHigh) {
        this.offlineHigh = offlineHigh;
    }

    public Integer getOfflineMedium() {
        return offlineMedium;
    }

    public void setOfflineMedium(Integer offlineMedium) {
        this.offlineMedium = offlineMedium;
    }

    public Integer getOfflineLow() {
        return offlineLow;
    }

    public void setOfflineLow(Integer offlineLow) {
        this.offlineLow = offlineLow;
    }

    public Integer getOfflineInfo() {
        return offlineInfo;
    }

    public void setOfflineInfo(Integer offlineInfo) {
        this.offlineInfo = offlineInfo;
    }

    public Integer getCritical() {
        return critical;
    }

    public void setCritical(Integer critical) {
        this.critical = critical;
    }

    public Integer getHigh() {
        return high;
    }

    public void setHigh(Integer high) {
        this.high = high;
    }

    public Integer getMedium() {
        return medium;
    }

    public void setMedium(Integer medium) {
        this.medium = medium;
    }

    public Integer getLow() {
        return low;
    }

    public void setLow(Integer low) {
        this.low = low;
    }

    public Integer getInfo() {
        return info;
    }

    public void setInfo(Integer info) {
        this.info = info;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}