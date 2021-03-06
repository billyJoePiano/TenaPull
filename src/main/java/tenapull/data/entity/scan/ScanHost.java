package tenapull.data.entity.scan;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import tenapull.util.*;
import org.hibernate.annotations.*;

import java.util.*;



import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents an object from the hosts array returned by the Nessus API in /scans/&lt;scan-id&gt;
 */
@Entity(name = "ScanHost")
@Table(name = "scan_host")
public class ScanHost extends ScanResponse.MultiChildLookup<ScanHost>
        implements MapLookupPojo<ScanHost> {

    /**
     * The dao for ScanHost
     */
    public static final MapLookupDao<ScanHost>
            dao = new MapLookupDao<ScanHost>(ScanHost.class);

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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "hostname_id")
    private Hostname hostname;

    /**
     * Gets severitycount.
     *
     * @return the severitycount
     */
    public SeverityCount getSeveritycount() {
        if (this.severitycount == null) {
            this.severitycount = new SeverityCount();
            this.severitycount.takeFieldsFromParent(this);
        }
        return this.severitycount;
    }

    /**
     * Sets severitycount.
     *
     * @param severitycount the severitycount
     */
    public void setSeveritycount(SeverityCount severitycount) {
        if (this.severitycount != null && this.severitycount != severitycount) {
            this.severitycount.clearParent();
        }
        this.severitycount = severitycount;
        severitycount.putFieldsIntoParent(this);
    }

    /**
     * Gets severity counts.
     *
     * @return the severity counts
     */
    public List<SeverityLevelCount> getSeverityCounts() {
        return this.severityCounts;
    }

    /**
     * Sets severity counts.
     *
     * @param severityCounts the severity counts
     */
    public void setSeverityCounts(List<SeverityLevelCount> severityCounts) {
        if (this.severityCounts == severityCounts) return;
        this.severityCounts = severityCounts;
        if (this.severitycount != null) this.severitycount.setItem(this.severityCounts);
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
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
    public boolean _match(ScanHost o) {
        if (o == this) return true;
        return  this.__match(o)
                && this.hostId != null && o.hostId != null
                && this.hostId.intValue() == o.hostId.intValue();
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[]
                { "response", this.getResponse(), "hostId", this.hostId });
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

    /**
     * Gets host id.
     *
     * @return the host id
     */
    public Integer getHostId() {
        return hostId;
    }

    /**
     * Sets host id.
     *
     * @param hostId the host id
     */
    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    /**
     * Gets total checks considered.
     *
     * @return the total checks considered
     */
    public Integer getTotalChecksConsidered() {
        return totalChecksConsidered;
    }

    /**
     * Sets total checks considered.
     *
     * @param totalChecksConsidered the total checks considered
     */
    public void setTotalChecksConsidered(Integer totalChecksConsidered) {
        this.totalChecksConsidered = totalChecksConsidered;
    }

    /**
     * Gets num checks considered.
     *
     * @return the num checks considered
     */
    public Integer getNumChecksConsidered() {
        return numChecksConsidered;
    }

    /**
     * Sets num checks considered.
     *
     * @param numChecksConsidered the num checks considered
     */
    public void setNumChecksConsidered(Integer numChecksConsidered) {
        this.numChecksConsidered = numChecksConsidered;
    }

    /**
     * Gets scan progress total.
     *
     * @return the scan progress total
     */
    public Integer getScanProgressTotal() {
        return scanProgressTotal;
    }

    /**
     * Sets scan progress total.
     *
     * @param scanProgressTotal the scan progress total
     */
    public void setScanProgressTotal(Integer scanProgressTotal) {
        this.scanProgressTotal = scanProgressTotal;
    }

    /**
     * Gets scan progress current.
     *
     * @return the scan progress current
     */
    public Integer getScanProgressCurrent() {
        return scanProgressCurrent;
    }

    /**
     * Sets scan progress current.
     *
     * @param scanProgressCurrent the scan progress current
     */
    public void setScanProgressCurrent(Integer scanProgressCurrent) {
        this.scanProgressCurrent = scanProgressCurrent;
    }

    /**
     * Gets host index.
     *
     * @return the host index
     */
    public Integer getHostIndex() {
        return hostIndex;
    }

    /**
     * Sets host index.
     *
     * @param hostIndex the host index
     */
    public void setHostIndex(Integer hostIndex) {
        this.hostIndex = hostIndex;
    }

    /**
     * Gets score.
     *
     * @return the score
     */
    public Integer getScore() {
        return score;
    }

    /**
     * Sets score.
     *
     * @param score the score
     */
    public void setScore(Integer score) {
        this.score = score;
    }

    /**
     * Gets progress.
     *
     * @return the progress
     */
    public String getProgress() {
        return progress;
    }

    /**
     * Sets progress.
     *
     * @param progress the progress
     */
    public void setProgress(String progress) {
        this.progress = progress;
    }

    /**
     * Gets offline critical.
     *
     * @return the offline critical
     */
    public Integer getOfflineCritical() {
        return offlineCritical;
    }

    /**
     * Sets offline critical.
     *
     * @param offlineCritical the offline critical
     */
    public void setOfflineCritical(Integer offlineCritical) {
        this.offlineCritical = offlineCritical;
    }

    /**
     * Gets offline high.
     *
     * @return the offline high
     */
    public Integer getOfflineHigh() {
        return offlineHigh;
    }

    /**
     * Sets offline high.
     *
     * @param offlineHigh the offline high
     */
    public void setOfflineHigh(Integer offlineHigh) {
        this.offlineHigh = offlineHigh;
    }

    /**
     * Gets offline medium.
     *
     * @return the offline medium
     */
    public Integer getOfflineMedium() {
        return offlineMedium;
    }

    /**
     * Sets offline medium.
     *
     * @param offlineMedium the offline medium
     */
    public void setOfflineMedium(Integer offlineMedium) {
        this.offlineMedium = offlineMedium;
    }

    /**
     * Gets offline low.
     *
     * @return the offline low
     */
    public Integer getOfflineLow() {
        return offlineLow;
    }

    /**
     * Sets offline low.
     *
     * @param offlineLow the offline low
     */
    public void setOfflineLow(Integer offlineLow) {
        this.offlineLow = offlineLow;
    }

    /**
     * Gets offline info.
     *
     * @return the offline info
     */
    public Integer getOfflineInfo() {
        return offlineInfo;
    }

    /**
     * Sets offline info.
     *
     * @param offlineInfo the offline info
     */
    public void setOfflineInfo(Integer offlineInfo) {
        this.offlineInfo = offlineInfo;
    }

    /**
     * Gets critical.
     *
     * @return the critical
     */
    public Integer getCritical() {
        return critical;
    }

    /**
     * Sets critical.
     *
     * @param critical the critical
     */
    public void setCritical(Integer critical) {
        this.critical = critical;
    }

    /**
     * Gets high.
     *
     * @return the high
     */
    public Integer getHigh() {
        return high;
    }

    /**
     * Sets high.
     *
     * @param high the high
     */
    public void setHigh(Integer high) {
        this.high = high;
    }

    /**
     * Gets medium.
     *
     * @return the medium
     */
    public Integer getMedium() {
        return medium;
    }

    /**
     * Sets medium.
     *
     * @param medium the medium
     */
    public void setMedium(Integer medium) {
        this.medium = medium;
    }

    /**
     * Gets low.
     *
     * @return the low
     */
    public Integer getLow() {
        return low;
    }

    /**
     * Sets low.
     *
     * @param low the low
     */
    public void setLow(Integer low) {
        this.low = low;
    }

    /**
     * Gets info.
     *
     * @return the info
     */
    public Integer getInfo() {
        return info;
    }

    /**
     * Sets info.
     *
     * @param info the info
     */
    public void setInfo(Integer info) {
        this.info = info;
    }

    /**
     * Gets severity.
     *
     * @return the severity
     */
    public Integer getSeverity() {
        return severity;
    }

    /**
     * Sets severity.
     *
     * @param severity the severity
     */
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * Gets hostname.
     *
     * @return the hostname
     */
    public Hostname getHostname() {
        return hostname;
    }

    /**
     * Sets hostname.
     *
     * @param hostname the hostname
     */
    public void setHostname(Hostname hostname) {
        this.hostname = hostname;
    }
}