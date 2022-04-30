package nessusTools.data.entity.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.host.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.splunk.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;
import org.hibernate.*;
import org.hibernate.annotations.*;
import org.hibernate.proxy.*;

import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;

@Entity(name = "ScanHostResponse")
@Table(name = "scan_host_response")
public class ScanHostResponse extends NessusResponseGenerateTimestamp {
    public static final Logger logger = LogManager.getLogger(ScanHostResponse.class);
    public static final Dao<ScanHostResponse> dao = new Dao(ScanHostResponse.class);

    public static String getUrlPath(int scanId, int scanHostId) throws IllegalArgumentException {
        if (scanId == 0 || scanHostId == 0) {
            throw new IllegalArgumentException("ScanHostResponse must have a non-zero id "
                    + "for scan and scanHost to construct the URL");
        }
        return "/scans/" + scanId + "/hosts/" + scanHostId;
    }

    @Transient
    @JsonIgnore
    public String getUrlPath() throws IllegalStateException {
        Integer hostId;
        if (this.host == null || (hostId = this.host.getHostId()) == null || hostId == 0) {
            throw new IllegalStateException(
                    "ScanHostResponse must have a Scanhost with a non-zero hostId to construct the URL");
        }

        int scanId = 0;
        ScanResponse scanResponse = this.host.getResponse();
        if (scanResponse == null || (scanId = scanResponse.getId()) == 0) {
            if (scanResponse != null) {
                Scan scan = scanResponse.getScan();
                if (scan != null) {
                    scanId = scan.getId();
                }
            }
            if (scanId == 0) {
                throw new IllegalStateException("ScanHostResponse must have a scanResponse with non-zero id, "
                            + "or it must contain a scan with non-zero id");
            }
        }

        return getUrlPath(scanId, hostId);
    }

    @Transient
    @JsonIgnore
    ScanResponse scanResponse; //for transient use by SplunkOutput

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @JsonIgnore
    private ScanHost host;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_host_vulnerability",
            joinColumns = { @JoinColumn(name = "host_id") },
            inverseJoinColumns = { @JoinColumn(name = "vulnerability_id") }
    )
    @OrderColumn(name = "__order_for_scan_host_vulnerability", nullable = false)
    @JsonSerialize(using = AddHostId.class)
    private List<Vulnerability> vulnerabilities;

    @OneToOne(mappedBy = "response", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private ScanHostInfo info;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
        this.vulnerabilities = Vulnerability.dao.getOrCreate(vulnerabilities);
        if (this.info != null) {
            this.info.setResponse(this);
            this.info._prepare();
            this.info.setResponse(this);
        }
    }

    //Transient.  For use by SplunkOutput
    public ScanResponse getScanResponse() {
        if (this.scanResponse == null || this.scanResponse instanceof HibernateProxy) {
            if (this.host == null || this.host instanceof HibernateProxy) {
                ScanHost.dao.holdSession();
                try {
                    this.host = ScanHost.dao.getById(this.getId());
                    if (this.host != null) {
                        this.scanResponse = host.getResponse();
                        if (this.scanResponse instanceof HibernateProxy) {
                            Hibernate.initialize(this.scanResponse);
                        }
                    }

                } catch (HibernateException e) {
                    logger.warn(e);

                } finally {
                    ScanHost.dao.releaseSession();
                }

            } else {
                if (this.scanResponse == null) {
                    this.scanResponse = this.host.getResponse();
                }
                if (scanResponse instanceof HibernateProxy) {
                    try {
                        this.scanResponse =
                                ScanResponse.dao.getById(this.scanResponse.getId());

                    } catch (HibernateException e) {
                        this.scanResponse = null;
                        logger.warn(e);
                    }
                }
            }
        }
        return this.scanResponse;
    }

    public void setScanResponse(ScanResponse scanResponse) {
        this.scanResponse = scanResponse;
    }

    public ScanHost getHost() {
        return host;
    }

    public void setHost(ScanHost host) {
        this.host = host;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public ScanHostInfo getInfo() {
        return info;
    }

    public void setInfo(ScanHostInfo info) {
        this.info = info;
    }
}
