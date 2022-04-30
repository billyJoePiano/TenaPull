package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginDescription")
@Table(name = "plugin_description")
public class PluginDescription extends StringHashLookupPojo<PluginDescription> {
    public static final StringHashLookupDao<PluginDescription> dao
            = new StringHashLookupDao<PluginDescription>(PluginDescription.class);

}