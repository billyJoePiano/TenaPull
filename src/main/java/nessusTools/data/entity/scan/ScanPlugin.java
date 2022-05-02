package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.hibernate.annotations.*;

import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;

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

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Transient
    @JsonIgnore
    @Override
    public ObjectNode toJsonNode() {
        return new ObjectMapper().valueToTree (this);
    }

    @Transient
    @JsonIgnore
    @Override
    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    @Transient
    @JsonIgnore
    @Override
    public String toString() {
        try {
            return this.toJsonString();
        } catch (JsonProcessingException e) {
            return "toString() could not convert to JSON for '"
                    + super.toString() + "' :\n"
                    + e.getMessage();
        }
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

}