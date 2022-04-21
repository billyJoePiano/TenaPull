package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.client.response.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ScanPlugin")
@Table(name = "scan_plugin")
public class ScanPlugin extends ScanResponse.ChildListTemplate
        implements ObjectLookupPojo<ScanPlugin> {

    public static final ObjectLookupDao<PluginAttributes> dao
            = new ObjectLookupDao<PluginAttributes>(PluginAttributes.class);

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_plugin_scan_host",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "host_id") }
    )
    @OrderColumn(name = "__order_for_scan_plugin_host")
    @JsonDeserialize(contentAs = ScanPluginHost.class, contentUsing = ObjectLookup.Deserializer.class)
    List<ScanPluginHost> hosts;

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

    @Override
    public void _set(ScanPlugin objectLookup) {
        //TODO
    }
}
