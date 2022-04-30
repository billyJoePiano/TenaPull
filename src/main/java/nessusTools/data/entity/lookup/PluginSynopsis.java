package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginSynopsis")
@Table(name = "plugin_synopsis")
public class PluginSynopsis extends StringHashLookupPojo<PluginSynopsis> {
    public static final StringHashLookupDao<PluginSynopsis> dao
            = new StringHashLookupDao<PluginSynopsis>(PluginSynopsis.class);

}