package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;

@Entity(name = "ScanPlugin")
@Table(name = "scan_plugin")
public class ScanPlugin extends ScanResponse.MultiChildLookup<ScanPlugin> {

    public static final MapLookupDao<ScanPlugin>
            dao = new MapLookupDao<ScanPlugin>(ScanPlugin.class);

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plugin_id")
    @JsonUnwrapped
    Plugin plugin;

    @Column(name = "host_count")
    @JsonProperty("host_count")
    Integer hostCount;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(
            name = "scan_plugin_host",
            joinColumns = { @JoinColumn(name = "scan_plugin_id") },
            inverseJoinColumns = { @JoinColumn(name = "plugin_host_id") }
    )
    @OrderColumn(name = "__order_for_scan_plugin_host")
    List<PluginHost> hosts;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
        this.plugin = Plugin.dao.getOrCreate(plugin);
        this.hosts = PluginHost.dao.getOrCreate(hosts);
    }

    @Override
    public void _set(ScanPlugin o) {
        this.__set(o);
        this.plugin = o.plugin;
        this.hosts = o.hosts;
    }

    @Override
    public boolean _match(ScanPlugin o) {
        if (o == this) return true;
        return this.__match(o)
                && (this.plugin != null
                        ? this.plugin._match(o.plugin)
                        : o.plugin == null);
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[]
                { "response", this.getResponse(), "plugin", this.plugin });
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
}