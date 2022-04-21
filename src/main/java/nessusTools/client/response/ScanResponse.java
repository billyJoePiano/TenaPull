package nessusTools.client.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.lang.reflect.*;
import java.util.*;

@Entity(name = "ScanInfoResponse")
@Table(name = "scan_info_response")
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

    @OneToOne
    @JoinColumn(name = "id")
    private ScanInfo info;

    @OneToMany(mappedBy = "scanResponse") //, cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "host_id")
    private List<ScanHost> hosts;

    @OneToOne
    @JoinColumn(name = "id")
    private ScanRemediationsSummary remediations;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_vulerability",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "vulnerability_id") }
    )
    @OrderColumn(name = "__order_for_scan_response_vulnerability", nullable = false)
    @JsonDeserialize(contentAs = Vulnerability.class, contentUsing = ObjectLookup.Deserializer.class)
    private List<Vulnerability> vulnerabilities;

    @OneToMany(mappedBy = "scanResponse")
    @OrderColumn(name = "__order_for_scan_response")
    private List<ScanHistory> history;

    @OneToOne
    @JoinColumn(name = "id")
    private ScanPrioritization prioritization;



    @MappedSuperclass
    private static abstract class Child extends ChildTemplate<ScanResponse> {
        @Transient
        @JsonIgnore
        public Class<ScanResponse> _getResponseType() {
            return ScanResponse.class;
        }

        @Transient
        @JsonIgnore
        public Scan getScan() {
            ScanResponse response = this._getResponse();
            if (response == null) return null;
            return response.getScan();
        }
    }

    @MappedSuperclass
    public static abstract class SingleChild extends Child {
        @OneToOne(mappedBy = "id")
        @JoinColumn(name = "id")
        @JsonIgnore
        public ScanResponse getScanResponse() {
            return this._getResponse();
        }

        @JsonIgnore
        public void setScanResponse(ScanResponse scanResponse) {
            this._setResponse(scanResponse);
        }
    }

    @MappedSuperclass
    public static abstract class MultiChild extends Child {
        @ManyToOne
        @JoinColumn(name = "scan_id")
        @JsonIgnore
        public ScanResponse getScanResponse() {
            return this._getResponse();
        }

        @JsonIgnore
        public void setScanResponse(ScanResponse scanResponse) {
            this._setResponse(scanResponse);
        }
    }

    private static abstract class ChildLookup<POJO extends ChildLookup>
            extends ChildLookupTemplate<POJO, ScanResponse> {

        @Transient
        @JsonIgnore
        public Class<ScanResponse> _getResponseType() {
            return ScanResponse.class;
        }

        @Transient
        @JsonIgnore
        public Scan getScan() {
            ScanResponse response = this._getResponse();
            if (response == null) return null;
            return response.getScan();
        }
    }

    @MappedSuperclass
    public static abstract class SingleChildLookup<POJO extends SingleChildLookup>
            extends ChildLookup<POJO> {

        @OneToOne(mappedBy = "id")
        @JoinColumn(name = "id")
        @JsonIgnore
        public ScanResponse getScanResponse() {
            return this._getResponse();
        }

        @JsonIgnore
        public void setScanResponse(ScanResponse scanResponse) {
            this._setResponse(scanResponse);
        }
    }

    @MappedSuperclass
    public static abstract class MultiChildLookup<POJO extends MultiChildLookup>
            extends ChildLookup<POJO> {

        @ManyToOne
        @JoinColumn(name = "scan_id")
        @JsonIgnore
        public ScanResponse getScanResponse() {
            return this._getResponse();
        }

        @JsonIgnore
        public void setScanResponse(ScanResponse scanResponse) {
            this._setResponse(scanResponse);
        }
    }


    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
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
}
