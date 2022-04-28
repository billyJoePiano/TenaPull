package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginInformation")
@Table(name = "plugin_information")
public class PluginInformation extends GeneratedIdPojo
        implements ObjectLookupPojo<PluginInformation> {

    public static final ObjectLookupDao<PluginInformation> dao
            = new ObjectLookupDao<PluginInformation>(PluginInformation.class);

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
    public void _prepare() { }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginInformation o) {
        return this.equals(o);
    }


    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public Integer getPluginId() {
        return pluginId;
    }

    public void setPluginId(Integer pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getPluginPublicationDate() {
        return pluginPublicationDate;
    }

    public void setPluginPublicationDate(String pluginPublicationDate) {
        this.pluginPublicationDate = pluginPublicationDate;
    }

    public String getPluginFamily() {
        return pluginFamily;
    }

    public void setPluginFamily(String pluginFamily) {
        this.pluginFamily = pluginFamily;
    }

    public String getPluginModificationDate() {
        return pluginModificationDate;
    }

    public void setPluginModificationDate(String pluginModificationDate) {
        this.pluginModificationDate = pluginModificationDate;
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
}
