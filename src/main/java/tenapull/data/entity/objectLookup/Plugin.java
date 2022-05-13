package tenapull.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import tenapull.util.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

/**
 * Represents a reusable "object lookup", for the plugin objects (except the scan-specific fields, represented
 * by ScanPlugin) returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "Plugin")
@Table(name = "plugin")
public class Plugin extends GeneratedIdPojo implements MapLookupPojo<Plugin> {

    /**
     * The dao for Plugin
     */
    public static final MapLookupDao<Plugin> dao = new MapLookupDao<Plugin>(Plugin.class);

    /**
     * The Severity.
     */
    Integer severity;

    /**
     * The Plugin name.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_name_id")
    @JsonProperty("pluginname")
    PluginName pluginName;

    /**
     * The Plugin attributes.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_attributes_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("pluginattributes")
    PluginAttributes pluginAttributes;

    /**
     * The Plugin family.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="plugin_family_id")
    @JsonProperty("pluginfamily")
    PluginFamily pluginFamily;

    /**
     * The Plugin id.
     */
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

    /**
     * Gets severity.
     *
     * @return the severity
     */
    public Integer getSeverity() {
        return severity;
    }

    /**
     * Sets severity.
     *
     * @param severity the severity
     */
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * Gets plugin name.
     *
     * @return the plugin name
     */
    public PluginName getPluginName() {
        return pluginName;
    }

    /**
     * Sets plugin name.
     *
     * @param pluginName the plugin name
     */
    public void setPluginName(PluginName pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Gets plugin attributes.
     *
     * @return the plugin attributes
     */
    public PluginAttributes getPluginAttributes() {
        return this.pluginAttributes;
    }

    /**
     * Sets plugin attributes.
     *
     * @param pluginAttributes the plugin attributes
     */
    public void setPluginAttributes(PluginAttributes pluginAttributes) {
        this.pluginAttributes = pluginAttributes;
    }

    /**
     * Gets plugin family.
     *
     * @return the plugin family
     */
    public PluginFamily getPluginFamily() {
        return pluginFamily;
    }

    /**
     * Sets plugin family.
     *
     * @param pluginFamily the plugin family
     */
    public void setPluginFamily(PluginFamily pluginFamily) {
        this.pluginFamily = pluginFamily;
    }

    /**
     * Gets plugin id.
     *
     * @return the plugin id
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Sets plugin id.
     *
     * @param pluginId the plugin id
     */
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
