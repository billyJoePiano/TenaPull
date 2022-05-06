package nessusTools.data.entity.host;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanHostInfo")
@Table(name = "scan_host_info")
public class ScanHostInfo
        extends NessusResponse.SingleChildTemplate<ScanHostInfo,
                                                    ScanHostResponse> {

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

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public HostIp getHostIp() {
        return hostIp;
    }

    public void setHostIp(HostIp hostIp) {
        this.hostIp = hostIp;
    }

    public HostFqdn getHostFqdn() {
        return hostFqdn;
    }

    public void setHostFqdn(HostFqdn hostFqdn) {
        this.hostFqdn = hostFqdn;
    }

    public HostNetbiosName getNetbiosName() {
        return netbiosName;
    }

    public void setNetbiosName(HostNetbiosName hostNetbiosName) {
        this.netbiosName = hostNetbiosName;
    }

    public String getHostStart() {
        return hostStart;
    }

    public void setHostStart(String hostStart) {
        this.hostStart = hostStart;
    }

    public String getHostEnd() {
        return hostEnd;
    }

    public void setHostEnd(String hostEnd) {
        this.hostEnd = hostEnd;
    }
}
