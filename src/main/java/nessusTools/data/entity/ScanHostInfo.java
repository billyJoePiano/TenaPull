package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;

import javax.persistence.*;

public class ScanHostInfo extends NaturalIdPojo {

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
}
