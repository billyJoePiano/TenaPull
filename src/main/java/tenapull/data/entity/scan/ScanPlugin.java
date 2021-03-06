package tenapull.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import tenapull.data.deserialize.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import tenapull.util.*;
import org.hibernate.annotations.*;

import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;
import java.util.stream.*;

/**
 * Represents the scan-specific fields (host_count, and hosts) in the plugin object from the "plugins"
 * array returned by the Nessus API in /scans/&lt;scan-id&gt;.  Also serves as a join table
 * between scan_response and plugin
 */
@Entity(name = "ScanPlugin")
@Table(name = "scan_plugin")
public class ScanPlugin implements MapLookupPojo<ScanPlugin>,
        ScanResponse.ScanResponseChild<ScanPlugin> {

    public static final MapLookupDao<ScanPlugin>
            dao = new MapLookupDao<ScanPlugin>(ScanPlugin.class);

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    @JsonIgnore
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    @Access(AccessType.PROPERTY)
    @JsonIgnore
    private ScanResponse response;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plugin_id")
    @JsonUnwrapped
    private Plugin plugin;

    @Column(name = "host_count")
    @JsonProperty("host_count")
    private Integer hostCount;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(
            name = "scan_plugin_host",
            joinColumns = { @JoinColumn(name = "scan_plugin_id") },
            inverseJoinColumns = { @JoinColumn(name = "plugin_host_id") }
    )
    @OrderColumn(name = "__order_for_scan_plugin_host")
    private List<PluginHost> hosts;

    @Transient
    @JsonIgnore
    private boolean bestGuess = false;

    public ScanPlugin() { }

    public ScanPlugin(Plugin plugin) {
        this.plugin = plugin;
        this.bestGuess = true;
    }

    @Transient
    @JsonIgnore
    @Override
    public ObjectNode toJsonNode() {
        //use the cached node from plugin
        if (this.plugin == null) return new ObjectMapper().valueToTree (this);
        ObjectNode node = this.plugin.toJsonNode();
        node.put("host_count", hostCount);
        if (this.hosts != null) {
            List<ObjectNode> list = this.hosts.stream().map(PluginHost::toJsonNode).collect(Collectors.toList());
            node.set("hosts", new ObjectMapper().valueToTree(list));
        }
        return node;
    }

    @Transient
    @JsonIgnore
    @Override
    public String toJsonString() {
        return this.toJsonNode().toString();
    }

    @Transient
    @JsonIgnore
    @Override
    public String toString() {
        return this.toJsonNode().toString();
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.plugin = Plugin.dao.getOrCreate(plugin);
        this.hosts = PluginHost.dao.getOrCreate(hosts);
    }

    @Override
    public boolean _match(ScanPlugin o) {
        if (o == null) return false;
        if (o == this) return true;
        ScanResponse myRes = this.response;
        ScanResponse theirRes = o.response;
        if (myRes == null || theirRes == null) {
            return false;
        }

        if (myRes != theirRes) {
            int myId = myRes.getId();
            int theirId = theirRes.getId();

            if (myId == 0 || myId != theirId) {
                return false;
            }
        }

        Plugin myPlugin = this.plugin;
        Plugin theirPlugin = o.plugin;
        if (myPlugin == null) {
            return theirPlugin == null;

        } else if (theirPlugin == null) {
            return false;
        }

        if (myPlugin == theirPlugin) return true;

        int myPluginId = myPlugin.getId();
        int theirPluginId = theirPlugin.getId();

        return myPluginId != 0 && myPluginId == theirPluginId;
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[]
                { "response", this.response, "plugin", this.plugin });
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public Integer getHostCount() {
        return hostCount;
    }

    public void setHostCount(Integer hostCount) {
        this.hostCount = hostCount;
    }

    public List<PluginHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<PluginHost> hosts) {
        this.hosts = hosts;
    }

    @Transient
    @JsonIgnore
    public boolean isBestGuess() {
        return this.bestGuess;
    }

    @Transient
    @JsonIgnore
    public void setBestGuess(boolean bestGuess) {
        this.bestGuess = bestGuess;
    }

    // OutMixIns.ScanPlugin overrides @JsonIgnore, for purposes of output
    @Transient
    @JsonIgnore
    public String getBestGuess() {
        if (this.bestGuess) {
            return OutputMixIns.ScanPlugin.BEST_GUESS_MSG;

        } else {
            return null;
        }
    }

    @Override
    public ScanResponse getResponse() {
        return this.response;
    }

    @Override
    public void setResponse(ScanResponse response) {
        this.response = response;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) return false;

        ScanPlugin other = (ScanPlugin)o;
        ScanResponse theirs = other.response;
        if (this.response == null) {
            if (theirs != null) return false;

        } else if (theirs == null) {
            return false;

        } else if (this.response.getId() != 0  && theirs.getId() != 0
                && this.response.getId() != theirs.getId()) {
            return false;
        }

        return  (this.getId() == 0 || other.getId() == 0 || this.getId() == other.getId())
                && Objects.equals(this.toJsonNode(), other.toJsonNode());
    }

    @Transient
    @JsonIgnore
    @Override
    public void _set(ScanPlugin o) {
        this.response = o.response;
        this.plugin = o.plugin;
        this.hostCount = o.hostCount;
        this.hosts = o.hosts;
    }

    @Override
    public int hashCode() {
        return ScanPlugin.class.hashCode() ^ this.id;
    }
}