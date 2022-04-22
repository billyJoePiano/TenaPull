package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;

import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginHost")
@Table(name = "plugin_host")
@JsonIgnoreProperties({"id"})
@JsonDeserialize(using = ObjectLookup.Deserializer.class)
public class PluginHost extends GeneratedIdPojo
        implements ObjectLookupPojo<PluginHost> {

    public static final ObjectLookupDao<PluginHost> dao = new ObjectLookupDao<PluginHost>(PluginHost.class);

    @Column(name = "host_id")
    @JsonProperty("id")
    Integer hostId;

    @Column(name = "host_fqdn")
    @JsonProperty("host_fqdn")
    String hostFqdn;

    String hostname;

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


    @Override
    public void _set(PluginHost o) {
        this.setId(o.getId());
        this.hostId = o.hostId;
        this.hostFqdn = o.hostFqdn;
        this.hostname = o.hostname;
        this.setExtraJson(o.getExtraJson());
    }
}
