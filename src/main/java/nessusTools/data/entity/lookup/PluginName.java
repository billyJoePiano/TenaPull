package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginName")
@Table(name = "plugin_name")
public class PluginName extends LookupPojo<PluginName> {
    public static final LookupDao<PluginName> dao
            = new LookupDao<PluginName>(PluginName.class);

}