package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

/**
 * Represents a reusable "object lookup", for the information object included
 * in the plugin attributes returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "PluginInformation")
@Table(name = "plugin_information")
public class PluginInformation extends HashLookupTemplate<PluginInformation> {

    /**
     * The dao for PluginInformation
     */
    public static final HashLookupDao<PluginInformation> dao
            = new HashLookupDao<PluginInformation>(PluginInformation.class);

    @Column(name = "plugin_version")
    @JsonProperty("plugin_version")
    private String pluginVersion;

    @Column(name = "plugin_id")
    @JsonProperty("plugin_id")
    private Integer pluginId;

    @Column(name = "plugin_type")
    @JsonProperty("plugin_type")
    private String pluginType;

    @Column(name = "plugin_publication_date")
    @JsonProperty("plugin_publication_date")
    private String pluginPublicationDate;

    @Column(name = "plugin_family")
    @JsonProperty("plugin_family")
    private String pluginFamily;

    @Column(name = "plugin_modification_date")
    @JsonProperty("plugin_modification_date")
    private String pluginModificationDate;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(PluginInformation o) {
        this.__set(o);
        this.pluginVersion = o.pluginVersion;
        this.pluginId = o.pluginId;
        this.pluginType = o.pluginType;
        this.pluginPublicationDate = o.pluginPublicationDate;
        this.pluginFamily = o.pluginFamily;
        this.pluginModificationDate = o.pluginModificationDate;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginInformation o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.pluginVersion, o.pluginVersion)
                && Objects.equals(this.pluginId, o.pluginId)
                && Objects.equals(this.pluginType, o.pluginType)
                && Objects.equals(this.pluginPublicationDate, o.pluginPublicationDate)
                && Objects.equals(this.pluginFamily, o.pluginFamily)
                && Objects.equals(this.pluginModificationDate, o.pluginModificationDate)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }


    /**
     * Gets plugin version.
     *
     * @return the plugin version
     */
    public String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * Sets plugin version.
     *
     * @param pluginVersion the plugin version
     */
    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    /**
     * Gets plugin id.
     *
     * @return the plugin id
     */
    public Integer getPluginId() {
        return pluginId;
    }

    /**
     * Sets plugin id.
     *
     * @param pluginId the plugin id
     */
    public void setPluginId(Integer pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Gets plugin type.
     *
     * @return the plugin type
     */
    public String getPluginType() {
        return pluginType;
    }

    /**
     * Sets plugin type.
     *
     * @param pluginType the plugin type
     */
    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    /**
     * Gets plugin publication date.
     *
     * @return the plugin publication date
     */
    public String getPluginPublicationDate() {
        return pluginPublicationDate;
    }

    /**
     * Sets plugin publication date.
     *
     * @param pluginPublicationDate the plugin publication date
     */
    public void setPluginPublicationDate(String pluginPublicationDate) {
        this.pluginPublicationDate = pluginPublicationDate;
    }

    /**
     * Gets plugin family.
     *
     * @return the plugin family
     */
    public String getPluginFamily() {
        return pluginFamily;
    }

    /**
     * Sets plugin family.
     *
     * @param pluginFamily the plugin family
     */
    public void setPluginFamily(String pluginFamily) {
        this.pluginFamily = pluginFamily;
    }

    /**
     * Gets plugin modification date.
     *
     * @return the plugin modification date
     */
    public String getPluginModificationDate() {
        return pluginModificationDate;
    }

    /**
     * Sets plugin modification date.
     *
     * @param pluginModificationDate the plugin modification date
     */
    public void setPluginModificationDate(String pluginModificationDate) {
        this.pluginModificationDate = pluginModificationDate;
    }
}
