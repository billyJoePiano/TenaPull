package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ScanPlugin")
@Table(name = "scan_plugin")
@JsonDeserialize(using = ObjectLookup.ResponseChildLookupDeserializer.class)
public class ScanPlugin extends ScanResponse.MultiChild<ScanPlugin> {

    public static final Dao<ScanPlugin> dao = new Dao<ScanPlugin>(ScanPlugin.class);

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_plugin_host",
            joinColumns = { @JoinColumn(name = "plugin_id") },
            inverseJoinColumns = { @JoinColumn(name = "host_id") }
    )
    @OrderColumn(name = "__order_for_scan_plugin_host")
    @JsonDeserialize(contentAs = PluginHost.class)
    List<PluginHost> hosts;

    Integer severity;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_name_id")
    PluginName pluginName;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_attritubes_id")
    PluginAttributes pluginAttributes;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_family_id")
    @JsonProperty("pluginfamily")
    PluginFamily pluginFamily;

    @Column(name = "host_count")
    @JsonProperty("host_count")
    Integer hostCount;

    @Column(name = "plugin_id")
    @JsonProperty("pluginid")
    String pluginId;

    @ManyToOne
    @JoinColumn(name = "scan_id")
    @JsonIgnore
    ScanPrioritization scanPrioritization;

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

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public PluginName getPluginName() {
        return pluginName;
    }

    public void setPluginName(PluginName pluginName) {
        this.pluginName = pluginName;
    }

    public PluginAttributes getPluginAttributes() {
        return pluginAttributes;
    }

    public void setPluginAttributes(PluginAttributes pluginAttributes) {
        this.pluginAttributes = pluginAttributes;
    }

    public PluginFamily getPluginFamily() {
        return pluginFamily;
    }

    public void setPluginFamily(PluginFamily pluginFamily) {
        this.pluginFamily = pluginFamily;
    }

    public Integer getHostCount() {
        return hostCount;
    }

    public void setHostCount(Integer hostCount) {
        this.hostCount = hostCount;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
}
