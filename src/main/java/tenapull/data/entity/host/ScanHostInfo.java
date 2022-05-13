package tenapull.data.entity.host;

import com.fasterxml.jackson.annotation.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents the "info" object returned by the Nessus API in /scans/&lt;scan-id&gt;/hosts/&lt;host-id&gt;
 */
@Entity(name = "ScanHostInfo")
@Table(name = "scan_host_info")
public class ScanHostInfo
        extends NessusResponse.SingleChildTemplate<ScanHostInfo,
                                                    ScanHostResponse> {

    /**
     * The dao for ScanHostInfo
     */
    public static final Dao<ScanHostInfo> dao = new Dao<>(ScanHostInfo.class);

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "operating_system_id")
    @JsonProperty("operating-system")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OperatingSystem operatingSystem;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "host_ip_id")
    @JsonProperty("host-ip")
    private HostIp hostIp;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "host_fqdn_id")
    @JsonProperty("host-fqdn")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HostFqdn hostFqdn;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "netbios_name_id")
    @JsonProperty("netbios-name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HostNetbiosName netbiosName;

    @Column(name = "host_start")
    @JsonProperty("host_start")
    private String hostStart;

    @Column(name = "host_end")
    @JsonProperty("host_end")
    private String hostEnd;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    /**
     * Gets operating system.
     *
     * @return the operating system
     */
    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Sets operating system.
     *
     * @param operatingSystem the operating system
     */
    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * Gets host ip.
     *
     * @return the host ip
     */
    public HostIp getHostIp() {
        return hostIp;
    }

    /**
     * Sets host ip.
     *
     * @param hostIp the host ip
     */
    public void setHostIp(HostIp hostIp) {
        this.hostIp = hostIp;
    }

    /**
     * Gets host fqdn.
     *
     * @return the host fqdn
     */
    public HostFqdn getHostFqdn() {
        return hostFqdn;
    }

    /**
     * Sets host fqdn.
     *
     * @param hostFqdn the host fqdn
     */
    public void setHostFqdn(HostFqdn hostFqdn) {
        this.hostFqdn = hostFqdn;
    }

    /**
     * Gets netbios name.
     *
     * @return the netbios name
     */
    public HostNetbiosName getNetbiosName() {
        return netbiosName;
    }

    /**
     * Sets netbios name.
     *
     * @param hostNetbiosName the host netbios name
     */
    public void setNetbiosName(HostNetbiosName hostNetbiosName) {
        this.netbiosName = hostNetbiosName;
    }

    /**
     * Gets host start.
     *
     * @return the host start
     */
    public String getHostStart() {
        return hostStart;
    }

    /**
     * Sets host start.
     *
     * @param hostStart the host start
     */
    public void setHostStart(String hostStart) {
        this.hostStart = hostStart;
    }

    /**
     * Gets host end.
     *
     * @return the host end
     */
    public String getHostEnd() {
        return hostEnd;
    }

    /**
     * Sets host end.
     *
     * @param hostEnd the host end
     */
    public void setHostEnd(String hostEnd) {
        this.hostEnd = hostEnd;
    }
}
