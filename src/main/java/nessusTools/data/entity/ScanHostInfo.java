package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

public class ScanHostInfo extends NaturalIdPojo { //TODO ... extends HostResponse.SingleChild

    public static final Dao<ScanHostInfo> dao = new Dao<>(ScanHostInfo.class);

    @Column(name = "operating_system_id")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonProperty("operating-system")
    OperatingSystem operatingSystem;

    @Column(name = "host_ip_id")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonProperty("host-ip")
    HostIp hostIp;

    @Column(name = "host_start")
    @JsonProperty("host_start")
    String hostStart;

    @Column(name = "host_end")
    @JsonProperty("host_end")
    String hostEnd;

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
