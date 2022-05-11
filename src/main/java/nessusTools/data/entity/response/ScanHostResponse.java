package nessusTools.data.entity.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.host.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;
import org.hibernate.*;
import org.hibernate.annotations.*;
import org.hibernate.proxy.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;

/**
 * Represents a response from the Nessus API at the url path /scans/&lt;scan-id&gt;/hosts/&lt;host-id&gt;,
 * with details about a specific host that was scanned
 */
@Entity(name = "ScanHostResponse")
@Table(name = "scan_host_response")
public class ScanHostResponse extends NessusResponseGenerateTimestamp {
    /**
     * The logger for ScanHostResponse
     */
    public static final Logger logger = LogManager.getLogger(ScanHostResponse.class);
    /**
     * The dao for ScanHostResponse
     */
    public static final Dao<ScanHostResponse> dao = new Dao(ScanHostResponse.class);

    /**
     * Gets url path for the response
     *
     * @param scanId the scan id
     * @param hostId the host id
     * @return the url path
     * @throws IllegalArgumentException if the scanId or hostId are invalid
     */
    public static String getUrlPath(int scanId, int hostId) throws IllegalArgumentException {
        if (scanId <= 0 || hostId <= 0) {
            throw new IllegalArgumentException("ScanHostResponse must have a non-zero id "
                    + "for scan and scanHost to construct the URL");
        }
        return "/scans/" + scanId + "/hosts/" + hostId;
    }

    /**
     * Gets url path for a provided ScanHost
     *
     * @param host the scan host which we want to obtain more information about
     * @return the url path
     * @throws NullPointerException if host is null
     */
    public static String getUrlPath(ScanHost host) throws NullPointerException {
        return getUrlPath(host.getResponse().getId(), host.getHostId());
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
    private ScanResponse scanResponse; //for transient use by HostVulnerabilityOutput

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

    /**
     * Gets the scan response corresponding the scanHost this ScanHostResponse is related to.
     * If the scanResponse is not already set, this method will attempt to track it down
     * through the ScanHost or by using the ScanResponse dao
     *
     * @return the scanResponse related to this scanHostResponse
     */
//Transient.  For use by HostVulnerabilityOutput
    @Transient
    @JsonIgnore
    public ScanResponse getOrFetchScanResponse() {
        if (this.host instanceof HibernateProxy) {
            this.host = ScanHost.dao.unproxy(this.host);
        }

        if (this.scanResponse == null && this.host != null) {
            this.scanResponse = this.host.getResponse();
        }

        if (this.scanResponse instanceof HibernateProxy) {
            this.scanResponse = ScanResponse.dao.unproxy(this.scanResponse);
        }

        if (this.scanResponse == null || this.scanResponse instanceof HibernateProxy) {
            altGetScanResponse();
        }

        return this.scanResponse;
    }

    private void altGetScanResponse() {
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

    /**
     * Sets scan response.
     *
     * @param scanResponse the scan response
     */
    public void setScanResponse(ScanResponse scanResponse) {
        this.scanResponse = scanResponse;
    }

    /**
     * Gets host.
     *
     * @return the host
     */
    public ScanHost getHost() {
        return host;
    }

    /**
     * Sets host.
     *
     * @param host the host
     */
    public void setHost(ScanHost host) {
        this.host = host;
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
     * Gets info.
     *
     * @return the info
     */
    public ScanHostInfo getInfo() {
        return info;
    }

    /**
     * Sets info.
     *
     * @param info the info
     */
    public void setInfo(ScanHostInfo info) {
        this.info = info;
    }
}
