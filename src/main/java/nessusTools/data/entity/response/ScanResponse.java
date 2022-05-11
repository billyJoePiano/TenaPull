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

/**
 * Represents a response from the Nessus API at the url path /scans/&lt;scan-id&gt;
 * with details about a specific scan
 */
@Entity(name = "ScanResponse")
@Table(name = "scan_response")
@JsonIgnoreProperties({"id", "filters"})
public class ScanResponse extends NessusResponseGenerateTimestamp {
    /**
     * The logger for ScanResponse
     */
    public static final Logger logger = LogManager.getLogger(ScanResponse.class);

    /**
     * The dao for ScanResponse
     */
    public static final Dao<ScanResponse> dao = new Dao(ScanResponse.class);

    /**
     * Gets url path to obtain the scan response
     *
     * @param scanId the scan id
     * @return the url path
     * @throws IllegalArgumentException if the scanId is invalid
     */
    public static String getUrlPath(int scanId) throws IllegalArgumentException {
        if (scanId <= 0) {
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

        } else {
            throw new IllegalStateException(
                    "ScanResponse must have a non-zero id or a scan with a non-zero id to construct the URL");
        }
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @JsonIgnore
    private Scan scan;

    @OneToOne(mappedBy = "response", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private ScanInfo info;

    @OneToMany(mappedBy = "response") //, cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("hostId")
    private List<ScanHost> hosts;

    @OneToOne(mappedBy = "response", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @Access(AccessType.PROPERTY)
    private ScanRemediationsSummary remediations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "response")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @OrderColumn(name = "__order_for_scan_remediation", nullable = false)
    @JsonIgnore
    private List<ScanRemediation> remediationsList;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_vulnerability",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "vulnerability_id") }
    )
    @OrderColumn(name = "__order_for_scan_response_vulnerability", nullable = false)
    private List<Vulnerability> vulnerabilities;

    @OneToMany(mappedBy = "response")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("historyId ASC")
    private List<ScanHistory> history;

    @OneToMany(mappedBy = "response")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Access(AccessType.PROPERTY)
    @OrderColumn(name = "__order_for_scan_plugin", nullable = false)
    @JsonIgnore
    private List<ScanPlugin> plugins;

    @Transient
    private ScanPrioritization prioritization;

    @Column(name = "threat_level")
    @Access(AccessType.PROPERTY) //force hibernate to use the getter/setter, which puts this value into the ScanPrioritization wrapper
    @JsonIgnore
    private Integer threatLevel;
    // threat_level is actually the one additional JSON property of the 'prioritization' container which wraps the array of
    // of plugins.  There is no DB entity for prioritization, because the Plugins are joined directly back to the response
    // so this property gets stored here instead, but (de)serialized in the ScanPrioritization wrapper for the array


    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
        if (this.scan != null) {
            this.scan._prepare();
            this.scan.setScanResponse(this);
        }
        if (this.info != null) {
            this.info._prepare();
            this.info.setResponse(this);

        }
        if (this.remediations != null) {
            this.remediations._prepare();
            this.remediations.setResponse(this);
        }

        this.setChildren();

        this.hosts = ScanHost.dao.getOrCreate(hosts);
        this.vulnerabilities = Vulnerability.dao.getOrCreate(vulnerabilities);
        this.history = ScanHistory.dao.getOrCreate(history);
        this.setPlugins(ScanPlugin.dao.getOrCreate(plugins));
        this.setRemediationsList(ScanRemediation.dao.getOrCreate(this.remediationsList));

        this.setChildren();
    }

    private void setChildren() {
        List<ScanResponseChild>[] children = new List[]
                {this.hosts, this.history, this.plugins, this.remediationsList};

        for (List<ScanResponseChild> list : children) {
            if (list == null) continue;
            for (ScanResponseChild child : list) {
                if (child == null) continue;
                child.setResponse(this);
            }
        }
    }

    /**
     * The interface Scan response child.
     *
     * @param <POJO> the type parameter
     */
    public interface ScanResponseChild<POJO extends ScanResponseChild<POJO>>
            extends ResponseChild<POJO, ScanResponse> {

    }


    /**
     * The type Single child.
     *
     * @param <POJO> the type parameter
     */
    @MappedSuperclass
    public abstract static class SingleChild<POJO extends SingleChild<POJO>>
            extends SingleChildTemplate<POJO, ScanResponse>
            implements ScanResponseChild<POJO> {

    }


    /**
     * The type Multi child.
     *
     * @param <POJO> the type parameter
     */
    @MappedSuperclass
    @AssociationOverride(
            name = "response",
            joinColumns = @JoinColumn(name = "scan_id")
    )
    public abstract static class MultiChild<POJO extends MultiChild<POJO>>
            extends MultiChildTemplate<POJO, ScanResponse>
            implements ScanResponseChild<POJO> {

    }


    /**
     * The type Single child lookup.
     *
     * @param <POJO> the type parameter
     */
    @MappedSuperclass
    public abstract static class SingleChildLookup<POJO extends SingleChildLookup<POJO>>
            extends SingleChild<POJO>
            implements MapLookupPojo<POJO> {

    }

    /**
     * The type Multi child lookup.
     *
     * @param <POJO> the type parameter
     */
    @MappedSuperclass
    public abstract static class MultiChildLookup<POJO extends MultiChildLookup<POJO>>
            extends MultiChild<POJO>
            implements MapLookupPojo<POJO> {

    }


    /**
     * Gets scan.
     *
     * @return the scan
     */
    public Scan getScan() {
        return scan;
    }

    /**
     * Sets scan.
     *
     * @param scan the scan
     */
    public void setScan(Scan scan) {
        this.scan = scan;
    }

    /**
     * Gets info.
     *
     * @return the info
     */
    public ScanInfo getInfo() {
        return info;
    }

    /**
     * Sets info.
     *
     * @param info the info
     */
    public void setInfo(ScanInfo info) {
        this.info = info;
    }

    /**
     * Gets hosts.
     *
     * @return the hosts
     */
    public List<ScanHost> getHosts() {
        return hosts = hosts;
    }

    /**
     * Sets hosts.
     *
     * @param hosts the hosts
     */
    public void setHosts(List<ScanHost> hosts) {
        this.hosts = hosts;
    }

    /**
     * Gets vulnerabilities.
     *
     * @return the vulnerabilities
     */
    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    /**
     * Sets vulnerabilities.
     *
     * @param vulnerabilities the vulnerabilities
     */
    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    /**
     * Gets remediations.
     *
     * @return the remediations
     */
    public ScanRemediationsSummary getRemediations() {
        return remediations;
    }

    /**
     * Sets remediations.
     *
     * @param remediations the remediations
     */
    public void setRemediations(ScanRemediationsSummary remediations) {
        if (remediations == this.remediations) return;

        if (this.remediations != null && this.remediationsList != null
                && this.remediations.getRemediations() == this.remediationsList) {

            //detach the two instances so they no longer share a list
            this.remediations.setRemediations(new ArrayList(this.remediationsList));
        }

        if (remediations != null) {
            if (this.remediationsList == null) {
                this.remediationsList = remediations.getRemediations();

            } else if (remediations.getRemediations() == null) {
                remediations.setRemediations(this.remediationsList);
            }
        }

        this.remediations = remediations;
    }

    /**
     * Gets remediations list.
     *
     * @return the remediations list
     */
    public List<ScanRemediation> getRemediationsList() {
        return remediationsList;
    }

    /**
     * Sets remediations list.
     *
     * @param remediationsList the remediations list
     */
    public void setRemediationsList(List<ScanRemediation> remediationsList) {
        if (this.remediationsList == remediationsList) return;
        this.remediationsList = remediationsList;

        if (this.remediations != null) {
            this.remediations.setRemediations(remediationsList);
        }
    }

    /**
     * Gets history.
     *
     * @return the history
     */
    public List<ScanHistory> getHistory() {
        return history;
    }

    /**
     * Sets history.
     *
     * @param history the history
     */
    public void setHistory(List<ScanHistory> history) {
        this.history = history;
    }

    /**
     * Gets plugins.
     *
     * @return the plugins
     */
    public List<ScanPlugin> getPlugins() {
        return this.plugins;
    }

    /**
     * Sets plugins.
     *
     * @param plugins the plugins
     */
    public void setPlugins(List<ScanPlugin> plugins) {
        if (this.plugins == plugins) return;
        this.plugins = plugins;
        if (this.prioritization != null) this.prioritization.setPlugins(this.plugins);
    }

    /**
     * Sets prioritization.
     *
     * @param prioritization the prioritization
     */
    public void setPrioritization(ScanPrioritization prioritization) {
        this.prioritization = prioritization;
        if (prioritization != null) {
            prioritization.putFieldsIntoParent(this);
        }
    }

    /**
     * Gets prioritization.
     *
     * @return the prioritization
     */
    public ScanPrioritization getPrioritization() {
        if (prioritization == null) {
            this.prioritization = new ScanPrioritization();
            this.prioritization.takeFieldsFromParent(this);
        }
        return this.prioritization;
    }

    /**
     * Gets threat level.
     *
     * @return the threat level
     */
    public Integer getThreatLevel() {
        return threatLevel;
    }

    /**
     * Sets threat level.
     *
     * @param threatLevel the threat level
     */
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
