package nessusTools.data.entity.response;

import com.fasterxml.jackson.annotation.*;
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
    @Fetch(value = FetchMode.SUBSELECT)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("hostId")
    @JsonDeserialize(contentUsing = ObjectLookup.ResponseChild.class)
    private List<ScanHost> hosts;

    @OneToOne(mappedBy = "response", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JsonDeserialize(using = ResponseChildDeserializer.class)
    private ScanRemediationsSummary remediations;

    @ManyToMany(cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_vulnerability",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "vulnerability_id") }
    )
    @OrderColumn(name = "__order_for_scan_response_vulnerability", nullable = false)
    @JsonDeserialize(contentUsing = ObjectLookup.Deserializer.class)
    private List<Vulnerability> vulnerabilities;

    @OneToMany(mappedBy = "response")
    @Fetch(value = FetchMode.SUBSELECT)
    @LazyCollection(LazyCollectionOption.FALSE)
    //@OrderColumn(name = "__order_for_scan_response", nullable = false)
    @OrderBy("historyId ASC")
    @JsonDeserialize(contentUsing = ObjectLookup.ResponseChild.class)
    private List<ScanHistory> history;

    @OneToMany(mappedBy = "response")
    @Fetch(value = FetchMode.SUBSELECT)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Access(AccessType.PROPERTY) //force hibernate to use the getter/setter, which wraps the list using the ScanPrioritization deserialization entity
    @JsonDeserialize(using = ScanPrioritization.Deserializer.class)
    @JsonSerialize(using = ScanPrioritization.Serializer.class)
    private List<ScanPlugin> prioritization;

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

    public synchronized void setInfo(ScanInfo info) {
        this.info = info;
    }

    public List<ScanHost> getHosts() {
        return hosts;
    }

    public synchronized void setHosts(List<ScanHost> hosts) {
        this.hosts = hosts;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public synchronized void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public ScanRemediationsSummary getRemediations() {
        return remediations;
    }

    public synchronized void setRemediations(ScanRemediationsSummary remediations) {
        this.remediations = remediations;
    }

    public List<ScanHistory> getHistory() {
        return history;
    }

    public void setHistory(List<ScanHistory> history) {
        this.history = history;
    }

    public List<ScanPlugin> getPrioritization() {
        return prioritization;
    }

    public void setPrioritization(List<ScanPlugin> prioritization) {
        this.prioritization = ScanPrioritization.wrapIfNeeded(this, prioritization);
    }

    public Integer getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(Integer threatLevel) {
        this.threatLevel = threatLevel;
        if (this.prioritization != null) {
            ((ScanPrioritization)this.prioritization).setThreatLevel(threatLevel);
        }
    }
}
