package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginSynopsis")
@Table(name = "plugin_synopsis")
public class PluginSynopsis extends LookupPojo<PluginSynopsis> {
    public static final LookupDao<PluginSynopsis> dao
            = new LookupDao<PluginSynopsis>(PluginSynopsis.class);

}