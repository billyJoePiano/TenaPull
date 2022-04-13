package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.client.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import java.util.*;



import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanHost")
@Table(name = "scan_host")
public class ScanHost extends ScanResponse.ChildListTemplate {
    public static final Dao<ScanHost> dao = new Dao(ScanHost.class);

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

    @OneToOne(mappedBy= "scanHost") //, cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonProperty("severitycount")
    private SeverityCountContainer severityCount;

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



    @Table(name = "scan_host_severity_count_container")
    @Entity(name = "ScanHost.SeverityCountCounter")
    @JsonIgnoreProperties({"id"})
    public static class SeverityCountContainer extends NaturalIdPojo {
        @OneToOne
        @JoinColumn(name = "id")
        @JsonIgnore
        private ScanHost scanHost;

        @OneToMany(mappedBy = "scanHost", targetEntity = SeverityCount.class)
        @OrderColumn(name = "severity_level")
        private List<SeverityCount> item;

        public ScanHost getScanHost() {
            return scanHost;
        }

        public void setScanHost(ScanHost scanHost) {
            this.scanHost = scanHost;
        }

        public List<SeverityCount> getItem() {
            return item;
        }

        public void setItem(List<SeverityCount> item) {
            this.item = item;
        }
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

    public SeverityCountContainer getSeverityCount() {
        return severityCount;
    }

    public void setSeverityCount(SeverityCountContainer severityCount) {
        this.severityCount = severityCount;
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