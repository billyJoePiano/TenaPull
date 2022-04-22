package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginFamily")
@Table(name = "plugin_family")
public class PluginFamily extends LookupPojo<PluginFamily> {
    public static final LookupDao<PluginFamily> dao
            = new LookupDao<PluginFamily>(PluginFamily.class);

}