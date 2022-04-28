package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;

import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "PluginHost")
@Table(name = "plugin_host")
public class PluginHost extends GeneratedIdPojo
        implements ObjectLookupPojo<PluginHost> {

    public static final ObjectLookupDao<PluginHost> dao = new ObjectLookupDao<PluginHost>(PluginHost.class);

    @Column(name = "host_ip")
    @JsonProperty("host_ip")
    private String hostIp;

    @Column(name = "host_id")
    @JsonProperty("id")
    Integer hostId;

    @Column(name = "host_fqdn")
    @JsonProperty("host_fqdn")
    String hostFqdn;

    String hostname;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public String getHostFqdn() {
        return hostFqdn;
    }

    public void setHostFqdn(String hostFqdn) {
        this.hostFqdn = hostFqdn;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() { }

    @Override
    public void _set(PluginHost o) {
        this.__set(o);
        this.hostId = o.hostId;
        this.hostFqdn = o.hostFqdn;
        this.hostname = o.hostname;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginHost o) {
        return o != null
                && Objects.equals(this.hostIp, o.hostIp)
                && Objects.equals(this.hostId, o.hostId)
                && Objects.equals(this.hostFqdn, o.hostFqdn)
                && Objects.equals(this.hostname, o.hostname);
    }
}
