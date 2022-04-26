package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginDescription")
@Table(name = "plugin_description")
public class PluginDescription extends LookupPojo<PluginDescription> {
    public static final LookupDao<PluginDescription> dao
            = new LookupDao<PluginDescription>(PluginDescription.class);

}