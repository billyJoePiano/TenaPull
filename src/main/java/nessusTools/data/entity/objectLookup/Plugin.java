package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "Plugin")
@Table(name = "plugin")
public class Plugin extends GeneratedIdPojo
        implements ObjectLookupPojo<Plugin> {

    public static final ObjectLookupDao<Plugin> dao = new ObjectLookupDao<Plugin>(Plugin.class);

    Integer severity;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_name_id")
    @JsonProperty("pluginname")
    PluginName pluginName;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_attributes_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("pluginattributes")
    PluginAttributes pluginAttributes;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_family_id")
    @JsonProperty("pluginfamily")
    PluginFamily pluginFamily;

    @Column(name = "plugin_id")
    @JsonProperty("pluginid")
    String pluginId;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.pluginAttributes = PluginAttributes.dao.getOrCreate(pluginAttributes);
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
        return this.pluginAttributes;
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

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public void _set(Plugin o) {
        this.__set(o);
        this.severity = o.severity;
        this.pluginName = o.pluginName;
        this.pluginAttributes = o.pluginAttributes;
        this.pluginFamily = o.pluginFamily;
        this.pluginId = o.pluginId;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(Plugin o) {
        return this.equals(o);
    }
}
