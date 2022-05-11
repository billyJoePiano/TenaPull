package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;

import javax.persistence.*;
import java.util.*;

/**
 * Represents a reusable "object lookup", for an object in the hosts list within each
 * plugin returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "PluginHost")
@Table(name = "plugin_host")
public class PluginHost extends GeneratedIdPojo implements MapLookupPojo<PluginHost> {

    /**
     * The dao for PluginHost
     */
    public static final MapLookupDao<PluginHost> dao = new MapLookupDao<PluginHost>(PluginHost.class);

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "host_ip_id")
    @JsonProperty("host_ip")
    private HostIp hostIp;

    @Column(name = "host_id")
    @JsonProperty("id")
    private Integer hostId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "host_fqdn_id")
    @JsonProperty("host_fqdn")
    private HostFqdn hostFqdn;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "hostname_id")
    private Hostname hostname;

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
     * Gets host id.
     *
     * @return the host id
     */
    public Integer getHostId() {
        return hostId;
    }

    /**
     * Sets host id.
     *
     * @param hostId the host id
     */
    public void setHostId(Integer hostId) {
        this.hostId = hostId;
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
     * Gets hostname.
     *
     * @return the hostname
     */
    public Hostname getHostname() {
        return hostname;
    }

    /**
     * Sets hostname.
     *
     * @param hostname the hostname
     */
    public void setHostname(Hostname hostname) {
        this.hostname = hostname;
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(PluginHost o) {
        this.__set(o);
        this.hostIp = o.hostIp;
        this.hostId = o.hostId;
        this.hostFqdn = o.hostFqdn;
        this.hostname = o.hostname;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginHost o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.hostIp, o.hostIp)
                && Objects.equals(this.hostId, o.hostId)
                && Objects.equals(this.hostFqdn, o.hostFqdn)
                && Objects.equals(this.hostname, o.hostname)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }

    @Transient
    @JsonIgnore
    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[] {
                "hostIp", this.hostIp,
                "hostId", this.hostId,
                "hostFqdn", this.hostFqdn,
                "hostname", this.hostname,
                "extraJson", this.getExtraJson()
        });
    }
}