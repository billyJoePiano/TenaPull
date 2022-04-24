package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;

@Entity(name = "ScanPlugin")
@Table(name = "scan_plugin")
@JsonDeserialize(using = ObjectLookup.Deserializer.class)
public class ScanPlugin extends GeneratedIdPojo
        implements LookupSearchMapProvider<ScanPlugin> {

    public static final ObjectLookupDao<ScanPlugin>
            dao = new ObjectLookupDao<ScanPlugin>(ScanPlugin.class);

    @ManyToOne
    @JoinColumn(name = "prioritization_id")
    @JsonIgnore
    ScanPrioritization scanPrioritization;

    @ManyToOne
    @JoinColumn(name = "plugin_id")
    // TODO Json flatten (other direction flatten...)
    Plugin plugin;

    @Column(name = "host_count")
    @JsonProperty("host_count")
    Integer hostCount;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_plugin_host",
            joinColumns = { @JoinColumn(name = "scan_plugin_id") },
            inverseJoinColumns = { @JoinColumn(name = "plugin_host_id") }
    )
    @OrderColumn(name = "__order_for_scan_plugin_host")
    @JsonDeserialize(contentAs = PluginHost.class)
    List<PluginHost> hosts;

    public ScanPrioritization getScanPrioritization() {
        return this.scanPrioritization;
    }

    public void setScanPrioritization(ScanPrioritization scanPrioritization) {
        this.scanPrioritization = scanPrioritization;
    }

    public List<PluginHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<PluginHost> hosts) {
        this.hosts = hosts;
    }


    @Override
    public void _set(ScanPlugin o) {
        this.setId(o.getId());
        this.scanPrioritization = o.scanPrioritization;
        this.plugin = o.plugin;
        this.hosts = o.hosts;
        this.setExtraJson(this.getExtraJson());
    }

    @Override
    public boolean _lookupMatch(ScanPlugin other) {
        if (other == null) return false;
        if (other == this) return true;
        if (this.scanPrioritization == null || other.scanPrioritization == null
            || this.plugin == null || other.plugin == null) {

            return false;
        }

        int mySPid = this.scanPrioritization.getId();
        int theirSPid = other.scanPrioritization.getId();
        int myPid = this.plugin.getId();
        int theirPid = other.plugin.getId();

        if (mySPid != 0 && theirSPid != 0) {
            if (mySPid != theirSPid) {
                return false;
            }
        } else if (!Objects.equals(this.scanPrioritization, other.scanPrioritization)) {
            return false;
        }

        if (myPid != 0 && theirPid != 0) {
            return myPid == theirPid;

        } else {
            return Objects.equals(this.plugin, other.plugin);
        }
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap() {
        return Map.of("scanPrioritization", this.scanPrioritization,
                        "plugin", this.plugin);
    }
}