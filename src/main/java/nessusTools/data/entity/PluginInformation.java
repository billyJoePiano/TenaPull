package nessusTools.data.entity;

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

    @Column(name = "plugin_modifications")
    @JsonProperty("plugin_modifications")
    private String pluginModificationDate;

    @Override
    public void _set(PluginInformation o) {
        this.setId(o.getId());
        this.pluginVersion = o.pluginVersion;
        this.pluginId = o.pluginId;
        this.pluginType = o.pluginType;
        this.pluginPublicationDate = o.pluginPublicationDate;
        this.pluginFamily = o.pluginFamily;
        this.pluginModificationDate = o.pluginModificationDate;
    }
}
