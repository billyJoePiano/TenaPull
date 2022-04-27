package nessusTools.data.entity.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ScanResponse")
@Table(name = "scan_response")
public class ScanResponse extends NessusResponseGenerateTimestamp {
    public static final Logger logger = LogManager.getLogger(ScanResponse.class);
    public static final Dao<ScanResponse> dao = new Dao(ScanResponse.class);

    public static String getUrlPath(int scanId) throws IllegalArgumentException {
        if (scanId == 0) {
            throw new IllegalArgumentException("ScanResponse must have a non-zero id to construct the URL");
        }
        return "/scans/" + scanId;
    }

    public String getUrlPath() throws IllegalStateException {
        int id = this.getId() ;
        if (id != 0) {
            return getUrlPath(id);

        } else if (this.scan != null) {
            return getUrlPath(this.scan.getId());

        } else if (this.info != null) {
            return getUrlPath(this.info.getId());

        } else {
            throw new IllegalStateException(
                    "ScanResponse must have a non-zero id or a scan or info (ScanInfo) with a non-zero id to construct the URL");
        }
    }

    @OneToOne
    @JoinColumn(name = "id")
    @JsonIgnore
    private Scan scan;

    @OneToOne(mappedBy = "response", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JsonDeserialize(using = ResponseChildDeserializer.class)
    private ScanInfo info;

    @OneToMany(mappedBy = "response") //, cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @OrderBy("hostId")
    @JsonDeserialize(contentUsing = ResponseChildDeserializer.class)
    private List<ScanHost> hosts;

    @OneToOne(mappedBy = "response", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JsonDeserialize(using = ResponseChildDeserializer.class)
    private ScanRemediationsSummary remediations;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @JoinTable(
            name = "scan_vulnerability",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "vulnerability_id") }
    )
    @OrderColumn(name = "__order_for_scan_response_vulnerability", nullable = false)
    private List<Vulnerability> vulnerabilities;

    @OneToMany(mappedBy = "response")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @OrderBy("historyId ASC")
    @JsonDeserialize(contentUsing = ResponseChildDeserializer.class)
    private List<ScanHistory> history;

    @OneToMany(mappedBy = "response")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @OrderColumn(name = "__order_for_scan_plugin")
    @JsonIgnore
    private List<ScanPlugin> plugins;

    @Transient
    ScanPrioritization prioritization;

    @Column(name = "threat_level")
    @Access(AccessType.PROPERTY) //force hibernate to use the getter/setter, which puts this value into the ScanPrioritization wrapper
    @JsonIgnore
    private Integer threatLevel;
    // threat_level is actually the one additional JSON property of the 'prioritization' container which wraps the array of
    // of plugins.  There is no DB entity for prioritization, because the Plugins are joined directly back to the response
    // so this property gets stored here instead, but (de)serialized in the ScanPrioritization wrapper for the array

    /**
     * For implementing default methods getScan() and _getResponseType()
     * on the various inheritance branches of NessusResponse.ResponseChild
     */
    public interface ScanResponseChild<POJO extends ScanResponseChild<POJO>>
            extends ResponseChild<POJO, ScanResponse> {

        default public Class<ScanResponse> _getResponseType() {
            return ScanResponse.class;
        }
    }


    @MappedSuperclass
    public abstract static class SingleChild<POJO extends SingleChild<POJO>>
            extends SingleChildTemplate<POJO, ScanResponse>
            implements ScanResponseChild<POJO> {

    }


    @MappedSuperclass
    @AssociationOverride(
            name = "response",
            joinColumns = @JoinColumn(name = "scan_id")
    )
    public abstract static class MultiChild<POJO extends MultiChild<POJO>>
            extends MultiChildTemplate<POJO, ScanResponse>
            implements ScanResponseChild<POJO> {

    }


    @MappedSuperclass
    public abstract static class SingleChildLookup<POJO extends SingleChildLookup<POJO>>
            extends SingleChild<POJO>
            implements ObjectLookupPojo<POJO> {

    }

    @MappedSuperclass
    public abstract static class MultiChildLookup<POJO extends MultiChildLookup<POJO>>
            extends MultiChild<POJO>
            implements LookupSearchMapProvider<POJO> {

    }


    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
        if (scan == null) super.setId(0);
        else super.setId(scan.getId());
    }

    public ScanInfo getInfo() {
        return info;
    }

    public void setInfo(ScanInfo info) {
        this.info = info;
    }

    public List<ScanHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<ScanHost> hosts) {
        this.hosts = ScanHost.dao.getOrCreate(hosts);
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = Vulnerability.dao.getOrCreate(vulnerabilities);
    }

    public ScanRemediationsSummary getRemediations() {
        return remediations;
    }

    public void setRemediations(ScanRemediationsSummary remediations) {
        this.remediations = remediations;
    }

    public List<ScanHistory> getHistory() {
        return history;
    }

    public void setHistory(List<ScanHistory> history) {
        this.history = ScanHistory.dao.getOrCreate(history);
    }

    public List<ScanPlugin> getPlugins() {
        return this.plugins;
    }

    public void setPlugins(List<ScanPlugin> plugins) {
        if (this.plugins == plugins) return;
        this.plugins = ScanPlugin.dao.getOrCreate(plugins);
        if (this.prioritization != null) this.prioritization.setPlugins(this.plugins);
    }

    public void setPrioritization(ScanPrioritization prioritization) {
        this.prioritization = prioritization;
        if (prioritization != null) {
            prioritization.putFieldsIntoParent(this);
        }
    }

    public ScanPrioritization getPrioritization() {
        if (prioritization == null) {
            this.prioritization = new ScanPrioritization();
            this.prioritization.takeFieldsFromParent(this);
        }
        return this.prioritization;
    }

    public Integer getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(Integer threatLevel) {
        this.threatLevel = threatLevel;
        if (this.prioritization != null) {
            this.prioritization.setThreatLevel(threatLevel);
        }
    }

    @Transient
    @JsonAnySetter
    @Override
    public void putExtraJson(String key, Object value) {
        super.putExtraJson(key, value);
        if (this.prioritization != null) this.prioritization.checkExtraJsonPut(key, value);
    }

    @Transient
    @JsonAnyGetter
    @Override
    public Map<String, JsonNode> getExtraJsonMap() {
        return this.getPrioritization().jsonAnyGetterForParent();
    }
}
