package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.client.response.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanPlugin.Host")
@Table(name = "scan_plugin_host")
@JsonIgnoreProperties({"id"})
@JsonDeserialize(using = ObjectLookup.Deserializer.class)
public class ScanPluginHost extends ScanResponse.ChildListTemplate
        implements ObjectLookupPojo<ScanPluginHost> {

    public static final Dao<ScanPluginHost> dao = new ObjectLookupDao<ScanPluginHost>(ScanPluginHost.class);

    // TODO ManyToMany and ObjectLookup logic

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
    public void _set(ScanPluginHost objectLookup) {

    }
}
