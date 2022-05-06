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
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "Plugin")
@Table(name = "plugin")
public class Plugin extends GeneratedIdPojo implements MapLookupPojo<Plugin> {

    public static final MapLookupDao<Plugin> dao = new MapLookupDao<Plugin>(Plugin.class);

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
        this.__prepare();
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
        if (o == this) return true;
        return o != null
                && Objects.equals(this.severity, o.severity)
                && Objects.equals(this.pluginName, o.pluginName)
                && (this.pluginAttributes != null
                        ? this.pluginAttributes._match(o.pluginAttributes)
                        : o.pluginAttributes == null)
                && Objects.equals(this.pluginFamily, o.pluginFamily)
                && Objects.equals(this.pluginId, o.pluginId)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) {
            return false;
        }

        return this._match((Plugin) o);
    }

    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[] {
                "severity", this.severity,
                "pluginName", this.pluginName,
                "pluginAttributes", this.pluginAttributes,
                "pluginFamily", this.pluginFamily,
                "pluginId", this.pluginId,
                "extraJson", this.getExtraJson()
        });
    }
}
